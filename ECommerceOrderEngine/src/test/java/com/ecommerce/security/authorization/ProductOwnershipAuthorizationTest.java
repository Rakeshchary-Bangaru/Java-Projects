package com.ecommerce.security.authorization;

import com.ecommerce.repository.SpringDataProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductOwnershipAuthorizationTest {

    private SpringDataProductRepository productRepository;

    private ProductOwnershipAuthorization productAuth;


    @BeforeEach
    void setUp() {

        productRepository =
                mock(SpringDataProductRepository.class);

        productAuth =
                new ProductOwnershipAuthorization(
                        productRepository
                );
    }


    @Test
    void shouldAllowSellerWhoOwnsProduct() {

        JwtAuthenticationToken authentication =
                authenticationForUser(27L);

        when(
                productRepository
                        .existsByIdAndSellerUserId(
                                100L,
                                27L
                        )
        ).thenReturn(true);

        boolean result =
                productAuth.isOwner(
                        100L,
                        authentication
                );

        assertTrue(result);
    }


    @Test
    void shouldRejectSellerWhoDoesNotOwnProduct() {

        JwtAuthenticationToken authentication =
                authenticationForUser(42L);

        when(
                productRepository
                        .existsByIdAndSellerUserId(
                                100L,
                                42L
                        )
        ).thenReturn(false);

        boolean result =
                productAuth.isOwner(
                        100L,
                        authentication
                );

        assertFalse(result);
    }


    @Test
    void shouldRejectJwtWithoutUserId() {

        Instant now =
                Instant.now();

        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "HS256")
                        .subject("seller@test.com")
                        .issuedAt(now)
                        .expiresAt(
                                now.plusSeconds(900)
                        )
                        .claim(
                                "role",
                                "SELLER_ADMIN"
                        )
                        .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(jwt);

        boolean result =
                productAuth.isOwner(
                        100L,
                        authentication
                );

        assertFalse(result);

        verifyNoInteractions(
                productRepository
        );
    }


    @Test
    void shouldRejectNullProductId() {

        JwtAuthenticationToken authentication =
                authenticationForUser(27L);

        boolean result =
                productAuth.isOwner(
                        null,
                        authentication
                );

        assertFalse(result);

        verifyNoInteractions(
                productRepository
        );
    }


    private JwtAuthenticationToken authenticationForUser(
            Long userId
    ) {

        Instant now =
                Instant.now();

        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "HS256")
                        .subject("seller@test.com")
                        .issuedAt(now)
                        .expiresAt(
                                now.plusSeconds(900)
                        )
                        .claim(
                                "userId",
                                userId
                        )
                        .claim(
                                "role",
                                "SELLER_ADMIN"
                        )
                        .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_SELLER_ADMIN"
                        ),
                        new SimpleGrantedAuthority(
                                "PRODUCT_UPDATE_OWN"
                        )
                )
        );
    }
}