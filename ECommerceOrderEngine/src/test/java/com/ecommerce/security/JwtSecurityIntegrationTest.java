package com.ecommerce.security;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;
import com.ecommerce.security.service.RegistrationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.HttpHeaders;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtSecurityIntegrationTest {


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private JwtEncoder jwtEncoder;


    @Autowired
    private RegistrationService registrationService;


    @Autowired
    private UserAccountRepository userAccountRepository;


    /*
     * =========================================================
     * VALID JWT
     * ---------------------------------------------------------
     * A valid JWT must:
     *
     * - have a valid signature
     * - not be expired
     * - contain a real UserAccount ID
     * - contain the correct role
     * - contain the current authVersion
     * - belong to an ACTIVE account
     * =========================================================
     */
    @Test
    void shouldAllowValidBearerToken()
            throws Exception {


        UserAccount user =
                createCustomer();


        Instant issuedAt =
                Instant.now();


        Instant expiresAt =
                issuedAt.plusSeconds(
                        900
                );


        String token =
                createToken(
                        user,
                        issuedAt,
                        expiresAt
                );


        mockMvc.perform(
                        get(
                                "/api/auth/me"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath(
                                "$.userId"
                        )
                                .value(
                                        user.getId()
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.email"
                        )
                                .value(
                                        user.getEmail()
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.role"
                        )
                                .value(
                                        "CUSTOMER"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.issuedAt"
                        )
                                .exists()
                )

                .andExpect(
                        jsonPath(
                                "$.expiresAt"
                        )
                                .exists()
                );
    }


    /*
     * =========================================================
     * NO JWT
     * =========================================================
     */
    @Test
    void shouldRejectRequestWithoutBearerToken()
            throws Exception {


        mockMvc.perform(
                        get(
                                "/api/auth/me"
                        )
                )

                .andExpect(
                        status().isUnauthorized()
                );
    }


    /*
     * =========================================================
     * EXPIRED JWT
     * ---------------------------------------------------------
     * Spring Security should reject the token before the
     * account-state filter allows access.
     * =========================================================
     */
    @Test
    void shouldRejectExpiredBearerToken()
            throws Exception {


        UserAccount user =
                createCustomer();


        Instant issuedAt =
                Instant.now()
                        .minusSeconds(
                                1800
                        );


        Instant expiresAt =
                Instant.now()
                        .minusSeconds(
                                900
                        );


        String token =
                createToken(
                        user,
                        issuedAt,
                        expiresAt
                );


        mockMvc.perform(
                        get(
                                "/api/auth/me"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )

                .andExpect(
                        status().isUnauthorized()
                );
    }


    /*
     * =========================================================
     * STALE JWT AFTER AUTH VERSION CHANGE
     * ---------------------------------------------------------
     * This is the key V7 security test.
     *
     * 1. User receives token with authVersion = 1.
     * 2. Security-sensitive account change increments DB
     *    authVersion to 2.
     * 3. Old token still contains version 1.
     * 4. JwtAccountStateFilter rejects the old session.
     * =========================================================
     */
    @Test
    void shouldRejectBearerTokenAfterAuthVersionChanges()
            throws Exception {


        UserAccount user =
                createCustomer();


        Instant issuedAt =
                Instant.now();


        Instant expiresAt =
                issuedAt.plusSeconds(
                        900
                );


        /*
         * Token contains the CURRENT authVersion.
         */
        String oldToken =
                createToken(
                        user,
                        issuedAt,
                        expiresAt
                );


        /*
         * Confirm that the token works before invalidation.
         */
        mockMvc.perform(
                        get(
                                "/api/auth/me"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + oldToken
                                )
                )

                .andExpect(
                        status().isOk()
                );


        /*
         * Simulate a security-sensitive account change such as:
         *
         * - role change
         * - account suspension
         * - account reactivation
         *
         * Old sessions must become invalid.
         */
        user.invalidateExistingSessions();


        userAccountRepository
                .saveAndFlush(
                        user
                );


        /*
         * The same JWT now contains an outdated authVersion.
         */
        mockMvc.perform(
                        get(
                                "/api/auth/me"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + oldToken
                                )
                )

                .andExpect(
                        status().isUnauthorized()
                )

                .andExpect(
                        jsonPath(
                                "$.error"
                        )
                                .value(
                                        "Authentication is no longer valid. Please sign in again."
                                )
                );
    }


    /*
     * =========================================================
     * CREATE REAL CUSTOMER ACCOUNT
     * ---------------------------------------------------------
     * RegistrationService creates:
     *
     * UserAccount
     *      +
     * Customer shopping profile
     *
     * Therefore the JWT references an actual persisted account
     * instead of the old hard-coded userId = 10.
     * =========================================================
     */
    private UserAccount createCustomer() {


        String email =
                "jwt-test-"
                        + UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        )
                        + "@test.com";


        RegisterRequest request =
                new RegisterRequest(
                        "JWT Test User",
                        email,
                        "Password123!",
                        "test-recaptcha-token"
                );


        return registrationService
                .registerCustomer(
                        request
                );
    }


    /*
     * =========================================================
     * JWT CREATION
     * ---------------------------------------------------------
     * Claims must represent the real persisted UserAccount.
     * =========================================================
     */
    private String createToken(
            UserAccount user,
            Instant issuedAt,
            Instant expiresAt
    ) {


        JwtClaimsSet claims =
                JwtClaimsSet.builder()

                        .issuer(
                                "https://ecommerce-order-engine"
                        )

                        .subject(
                                user.getEmail()
                        )

                        .issuedAt(
                                issuedAt
                        )

                        .expiresAt(
                                expiresAt
                        )

                        .claim(
                                "userId",
                                user.getId()
                        )

                        .claim(
                                "role",
                                user.getRole()
                                        .name()
                        )

                        /*
                         * Required by JwtAccountStateFilter.
                         */
                        .claim(
                                "authVersion",
                                user.getAuthVersion()
                        )

                        .claim(
                                "jti",
                                UUID.randomUUID()
                                        .toString()
                        )

                        .build();


        JwsHeader header =
                JwsHeader
                        .with(
                                MacAlgorithm.HS256
                        )
                        .type(
                                "JWT"
                        )
                        .build();


        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                header,
                                claims
                        )
                )
                .getTokenValue();
    }
}