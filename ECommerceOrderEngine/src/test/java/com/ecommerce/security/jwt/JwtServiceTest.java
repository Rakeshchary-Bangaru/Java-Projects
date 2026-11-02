package com.ecommerce.security.jwt;

import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {

        String testSecret =
                "01234567890123456789012345678901";

        SecretKey secretKey =
                new SecretKeySpec(
                        testSecret.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                );

        JwtEncoder jwtEncoder =
                NimbusJwtEncoder
                        .withSecretKey(secretKey)
                        .algorithm(MacAlgorithm.HS256)
                        .build();

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        "https://ecommerce-order-engine"
                )
        );

        this.jwtDecoder = decoder;

        this.jwtService =
                new JwtService(
                        jwtEncoder,
                        "https://ecommerce-order-engine",
                        900
                );
    }

    @Test
    void shouldGenerateAndValidateJwt() {

        UserAccount user = mock(UserAccount.class);

        when(user.getId()).thenReturn(10L);
        when(user.getEmail()).thenReturn("user@email.com");
        when(user.getRole()).thenReturn(Role.CUSTOMER);
        if (user.getId() == null) {
            throw new IllegalArgumentException(
                    "Cannot generate JWT for a user without an id"
            );
        }
        IssuedToken issuedToken =
                jwtService.generateAccessToken(user);

        assertNotNull(issuedToken.tokenValue());
        assertNotNull(issuedToken.issuedAt());
        assertNotNull(issuedToken.expiresAt());

        Jwt jwt =
                jwtDecoder.decode(
                        issuedToken.tokenValue()
                );

        assertEquals(
                "user@email.com",
                jwt.getSubject()
        );

        assertEquals(
                "CUSTOMER",
                jwt.getClaimAsString("role")
        );

        assertEquals(
                "https://ecommerce-order-engine",
                jwt.getIssuer().toString()
        );

        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());
        assertNotNull(jwt.getClaimAsString("jti"));
    }

    @Test
    void shouldCreateTokenWithFifteenMinuteLifetime() {

        UserAccount user = mock(UserAccount.class);

        when(user.getId()).thenReturn(10L);
        when(user.getEmail()).thenReturn("user@email.com");
        when(user.getRole()).thenReturn(Role.CUSTOMER);
        if (user.getId() == null) {
            throw new IllegalArgumentException(
                    "Cannot generate JWT for a user without an id"
            );
        }
        IssuedToken issuedToken =
                jwtService.generateAccessToken(user);

        Duration duration =
                Duration.between(
                        issuedToken.issuedAt(),
                        issuedToken.expiresAt()
                );

        assertEquals(
                900,
                duration.getSeconds()
        );
    }
}