
package com.ecommerce.security.controller;

import com.ecommerce.security.jwt.IssuedToken;
import com.ecommerce.security.jwt.JwtService;
import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;
import com.ecommerce.security.service.GoogleLoginCodeService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth2")
public class GoogleOAuth2ExchangeController {

    private final GoogleLoginCodeService loginCodeService;
    private final UserAccountRepository userRepository;
    private final JwtService jwtService;

    public GoogleOAuth2ExchangeController(
            GoogleLoginCodeService loginCodeService,
            UserAccountRepository userRepository,
            JwtService jwtService
    ) {
        this.loginCodeService = loginCodeService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public record ExchangeRequest(String code) {}

    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(
            @RequestBody ExchangeRequest body,
            HttpServletRequest request
    ) {
        if (body == null || body.code() == null) {
            return invalidExchange();
        }

        try {
            HttpSession session =
                    request.getSession(false);

            Long userId = loginCodeService.consumeCode(
                    session,
                    body.code()
            );

            /*
             * Re-read the account to ensure roles, status,
             * and authVersion are current at token issuance.
             */
            UserAccount user = userRepository.findById(
                    userId
            ).orElseThrow(() ->
                    new IllegalStateException(
                            "Account unavailable"
                    )
            );

            if (user.getStatus() != AccountStatus.ACTIVE) {
                return invalidExchange();
            }

            IssuedToken issued =
                    jwtService.generateAccessToken(user);

            /*
             * Match these accessors to your IssuedToken
             * record if its names differ.
             */
            Map<String, Object> responseBody = Map.of(
                    "accessToken", issued.tokenValue(),
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "role", user.getRole().name(),
                    "issuedAt", issued.issuedAt(),
                    "expiresAt", issued.expiresAt()
            );

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(responseBody);

        } catch (IllegalArgumentException
                 | IllegalStateException ex) {
            return invalidExchange();
        }
    }

    private ResponseEntity<?> invalidExchange() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "message",
                        "Google authentication could not be completed"
                ));
    }
}
