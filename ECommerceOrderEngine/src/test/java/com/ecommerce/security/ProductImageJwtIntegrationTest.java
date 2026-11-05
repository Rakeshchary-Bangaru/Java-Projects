
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductImageJwtIntegrationTest {

    private static final long PRODUCT_ID = 9999999L;
    private static final long IMAGE_ID = 9999999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private UserAccount createCustomer() {

        String email =
                "v11-jwt-" +
                        UUID.randomUUID().toString()
                                .replace("-", "") +
                        "@test.com";

        RegisterRequest request = new RegisterRequest(
                "V11 JWT Customer",
                email,
                "Password123!",
                "test-recaptcha-token"
        );

        return registrationService.registerCustomer(request);
    }

    private String createToken(UserAccount user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://ecommerce-order-engine")
                .subject(user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("authVersion", user.getAuthVersion())
                .claim("jti", UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    private MockMultipartFile testImage() {

        return new MockMultipartFile(
                "file",
                "security-test.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[] {1, 2, 3}
        );
    }

    @Test
    void customerWithValidJwtCannotUploadImage()
            throws Exception {

        UserAccount customer = createCustomer();
        String jwt = createToken(customer);

        mockMvc.perform(
                multipart(
                        "/api/products/{productId}/images",
                        PRODUCT_ID
                )
                        .file(testImage())
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwt
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void customerWithValidJwtCannotSetPrimary()
            throws Exception {

        UserAccount customer = createCustomer();
        String jwt = createToken(customer);

        mockMvc.perform(
                put(
                        "/api/products/{productId}/images/{imageId}/primary",
                        PRODUCT_ID,
                        IMAGE_ID
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwt
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void customerWithValidJwtCannotDeleteImage()
            throws Exception {

        UserAccount customer = createCustomer();
        String jwt = createToken(customer);

        mockMvc.perform(
                delete(
                        "/api/products/{productId}/images/{imageId}",
                        PRODUCT_ID,
                        IMAGE_ID
                )
                        .header(
                                HttpHeaders.AUTHORIZATION,
                                "Bearer " + jwt
                        )
        ).andExpect(status().isForbidden());
    }

    @Test
    void invalidatedJwtCannotModifyProductImages()
            throws Exception {

        UserAccount customer = createCustomer();

        String oldJwt = createToken(customer);

        // Invalidate the JWT after it has been issued.
        customer.invalidateExistingSessions();

        userAccountRepository.saveAndFlush(customer);

        mockMvc.perform(
                        multipart(
                                "/api/products/{productId}/images",
                                PRODUCT_ID
                        )
                                .file(testImage())
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + oldJwt
                                )
                ).andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.error").value(
                                "Authentication is no longer valid. Please sign in again."
                        )
                );
    }
}
