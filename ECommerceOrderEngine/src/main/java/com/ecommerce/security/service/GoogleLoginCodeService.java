
package com.ecommerce.security.service;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class GoogleLoginCodeService {

    private static final String SESSION_KEY =
            GoogleLoginCodeService.class.getName() + ".PENDING";

    private static final long VALIDITY_SECONDS = 60;

    private final SecureRandom secureRandom = new SecureRandom();

    private record PendingLogin(
            byte[] codeHash,
            Long userId,
            Instant expiresAt
    ) {}

    public String createCode(
            HttpSession session,
            Long userId
    ) {
        if (session == null || userId == null) {
            throw new IllegalArgumentException(
                    "Session and user ID are required"
            );
        }

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String code = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        synchronized (session) {
            session.setAttribute(
                    SESSION_KEY,
                    new PendingLogin(
                            sha256(code),
                            userId,
                            Instant.now().plusSeconds(
                                    VALIDITY_SECONDS
                            )
                    )
            );
        }

        return code;
    }

    public Long consumeCode(
            HttpSession session,
            String code
    ) {
        if (
                session == null ||
                        code == null ||
                        code.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Invalid Google login code"
            );
        }

        synchronized (session) {

            Object value = session.getAttribute(
                    SESSION_KEY
            );

            if (!(value instanceof PendingLogin pending)) {
                throw new IllegalArgumentException(
                        "Google login code is invalid or expired"
                );
            }

            /*
             * Consume immediately, including on an invalid
             * attempt. This prevents replay.
             */
            session.removeAttribute(SESSION_KEY);

            if (!Instant.now().isBefore(
                    pending.expiresAt()
            )) {
                throw new IllegalArgumentException(
                        "Google login code is invalid or expired"
                );
            }

            boolean matches = MessageDigest.isEqual(
                    pending.codeHash(),
                    sha256(code)
            );

            if (!matches) {
                throw new IllegalArgumentException(
                        "Google login code is invalid or expired"
                );
            }

            return pending.userId();
        }
    }

    private byte[] sha256(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            return digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is unavailable",
                    ex
            );
        }
    }
}
