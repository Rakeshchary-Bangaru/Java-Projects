package com.ecommerce.security;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;
import com.ecommerce.security.service.RegistrationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.context.annotation.Import;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(RoleAuthorizationIntegrationTest.TestEndpoints.class)
class RoleAuthorizationIntegrationTest {


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
     * ROOT
     * =========================================================
     */
    @Test
    void rootShouldAccessRootArea()
            throws Exception {


        UserAccount root =
                createUserWithRole(
                        Role.ROOT
                );


        mockMvc.perform(
                        get(
                                "/api/root/test"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(
                                                root
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    /*
     * =========================================================
     * CUSTOMER
     * ---------------------------------------------------------
     * CUSTOMER is authenticated, but does not have ROOT access.
     * =========================================================
     */
    @Test
    void customerShouldNotAccessRootArea()
            throws Exception {


        UserAccount customer =
                createUserWithRole(
                        Role.CUSTOMER
                );


        mockMvc.perform(
                        get(
                                "/api/root/test"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(
                                                customer
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    /*
     * =========================================================
     * SELLER ADMIN
     * =========================================================
     */
    @Test
    void sellerAdminShouldAccessSellerArea()
            throws Exception {


        UserAccount seller =
                createUserWithRole(
                        Role.SELLER_ADMIN
                );


        mockMvc.perform(
                        get(
                                "/api/seller/test"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(
                                                seller
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    /*
     * =========================================================
     * IT SUPPORT CANNOT ACCESS SELLER MANAGEMENT
     * =========================================================
     */
    @Test
    void itSupportShouldNotAccessSellerArea()
            throws Exception {


        UserAccount support =
                createUserWithRole(
                        Role.IT_SUPPORT
                );


        mockMvc.perform(
                        get(
                                "/api/seller/test"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(
                                                support
                                        )
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    /*
     * =========================================================
     * IT SUPPORT CAN ACCESS SUPPORT AREA
     * =========================================================
     */
    @Test
    void itSupportShouldAccessSupportArea()
            throws Exception {


        UserAccount support =
                createUserWithRole(
                        Role.IT_SUPPORT
                );


        mockMvc.perform(
                        get(
                                "/api/support/test"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        bearerToken(
                                                support
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    /*
     * =========================================================
     * ROOT HAS GLOBAL MANAGEMENT ACCESS
     * =========================================================
     */
    @Test
    void rootShouldAccessSupportAndSellerAreas()
            throws Exception {


        UserAccount root =
                createUserWithRole(
                        Role.ROOT
                );


        String rootToken =
                bearerToken(
                        root
                );


        mockMvc.perform(
                        get(
                                "/api/support/test"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        rootToken
                                )
                )
                .andExpect(
                        status().isOk()
                );


        mockMvc.perform(
                        get(
                                "/api/seller/test"
                        )
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        rootToken
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    /*
     * =========================================================
     * CREATE REAL DATABASE USER
     * ---------------------------------------------------------
     * RegistrationService creates:
     *
     * - UserAccount
     * - Customer shopping profile
     *
     * Every V7 account is also a shopper.
     *
     * The role is then changed for the authorization test.
     *
     * @Transactional on this test class rolls everything back
     * after each test.
     * =========================================================
     */
    private UserAccount createUserWithRole(
            Role role
    ) {


        String uniquePart =
                UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        );


        String email =
                role.name()
                        .toLowerCase()
                        + "-"
                        + uniquePart
                        + "@test.com";


        RegisterRequest request =
                new RegisterRequest(
                        role.name()
                                + " Test User",
                        email,
                        "Password123!",
                        "test-recaptcha-token"
                );


        UserAccount user =
                registrationService
                        .registerCustomer(
                                request
                        );


        /*
         * Registration always starts as CUSTOMER.
         *
         * Promote only when the test requires another role.
         */
        if (user.getRole() != role) {

            user.changeRole(
                    role
            );


            user =
                    userAccountRepository
                            .saveAndFlush(
                                    user
                            );
        }


        return user;
    }


    /*
     * =========================================================
     * CREATE JWT FOR REAL USER ACCOUNT
     * ---------------------------------------------------------
     * Unlike the old test, the claims now correspond to the
     * actual persisted account.
     *
     * JwtAccountStateFilter can therefore validate:
     *
     * userId
     * role
     * authVersion
     * ACTIVE status
     * =========================================================
     */
    private String bearerToken(
            UserAccount user
    ) {


        Instant issuedAt =
                Instant.now();


        Instant expiresAt =
                issuedAt.plusSeconds(
                        900
                );


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
                         * Required by V7 account-state
                         * validation.
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


        String token =
                jwtEncoder
                        .encode(
                                JwtEncoderParameters.from(
                                        header,
                                        claims
                                )
                        )
                        .getTokenValue();


        return "Bearer " + token;
    }


    /*
     * =========================================================
     * TEST-ONLY ENDPOINTS
     * =========================================================
     */
    @TestConfiguration
    static class TestEndpoints {


        @RestController
        static class ProbeController {


            @GetMapping("/api/root/test")
            ResponseEntity<Void> root() {

                return ResponseEntity
                        .ok()
                        .build();
            }


            @GetMapping("/api/seller/test")
            ResponseEntity<Void> seller() {

                return ResponseEntity
                        .ok()
                        .build();
            }


            @GetMapping("/api/support/test")
            ResponseEntity<Void> support() {

                return ResponseEntity
                        .ok()
                        .build();
            }


            @GetMapping("/api/customer/test")
            ResponseEntity<Void> customer() {

                return ResponseEntity
                        .ok()
                        .build();
            }
        }
    }
}