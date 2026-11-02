package com.ecommerce.security;

import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.service.ProductService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductUpdateMethodSecurityTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private SpringDataProductRepository productRepository;


    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void sellerShouldUpdateOwnProduct() {

        authenticate(
                27L,
                "SELLER_ADMIN",
                "PRODUCT_UPDATE_OWN"
        );

        Product product =
                new Product(
                        100,
                        "Old Product",
                        "Electronics",
                        new BigDecimal("100.00")
                );

        when(
                productRepository.existsByIdAndSellerUserId(
                        100L,
                        27L
                )
        ).thenReturn(true);

        when(
                productRepository.findById(100L)
        ).thenReturn(
                Optional.of(product)
        );

        Product updated =
                productService.replace(
                        100L,
                        "Updated Product",
                        "Electronics",
                        new BigDecimal("150.00")
                );

        assertNotNull(updated);

        verify(productRepository)
                .existsByIdAndSellerUserId(
                        100L,
                        27L
                );

        verify(productRepository)
                .findById(100L);
    }


    @Test
    void sellerShouldNotUpdateAnotherSellersProduct() {

        authenticate(
                42L,
                "SELLER_ADMIN",
                "PRODUCT_UPDATE_OWN"
        );

        when(
                productRepository.existsByIdAndSellerUserId(
                        100L,
                        42L
                )
        ).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        productService.replace(
                                100L,
                                "Illegal Update",
                                "Electronics",
                                new BigDecimal("150.00")
                        )
        );

        verify(productRepository)
                .existsByIdAndSellerUserId(
                        100L,
                        42L
                );

        verify(
                productRepository,
                never()
        ).findById(anyLong());
    }


    @Test
    void rootShouldUpdateAnyProductWithoutOwnershipCheck() {

        authenticate(
                1L,
                "ROOT",
                "SYSTEM_ADMIN"
        );

        Product product =
                new Product(
                        100,
                        "Old Product",
                        "Electronics",
                        new BigDecimal("100.00")
                );

        when(
                productRepository.findById(100L)
        ).thenReturn(
                Optional.of(product)
        );

        Product updated =
                productService.replace(
                        100L,
                        "Root Updated Product",
                        "Electronics",
                        new BigDecimal("200.00")
                );

        assertNotNull(updated);

        verify(
                productRepository,
                never()
        ).existsByIdAndSellerUserId(
                anyLong(),
                anyLong()
        );

        verify(productRepository)
                .findById(100L);
    }


    private void authenticate(
            Long userId,
            String role,
            String... permissions
    ) {

        Instant now =
                Instant.now();

        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "HS256")
                        .subject(
                                role.toLowerCase()
                                        + "@test.com"
                        )
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
                                role
                        )
                        .build();

        List<SimpleGrantedAuthority> authorities =
                new java.util.ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_" + role
                )
        );

        for (String permission : permissions) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            permission
                    )
            );
        }

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt,
                        authorities
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @Test
    void sellerShouldUpdatePriceOfOwnProduct() {

        authenticate(
                27L,
                "SELLER_ADMIN",
                "PRODUCT_UPDATE_OWN"
        );

        Product product =
                new Product(
                        100,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("100.00")
                );

        when(
                productRepository.existsByIdAndSellerUserId(
                        100L,
                        27L
                )
        ).thenReturn(true);

        when(
                productRepository.findById(100L)
        ).thenReturn(
                Optional.of(product)
        );

        Product updated =
                productService.updatePrice(
                        100L,
                        new BigDecimal("150.00")
                );

        assertEquals(
                new BigDecimal("150.00"),
                updated.getPriceAmount()
        );
    }

    @Test
    void sellerShouldNotUpdatePriceOfAnotherSellersProduct() {

        authenticate(
                42L,
                "SELLER_ADMIN",
                "PRODUCT_UPDATE_OWN"
        );

        when(
                productRepository.existsByIdAndSellerUserId(
                        100L,
                        42L
                )
        ).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        productService.updatePrice(
                                100L,
                                new BigDecimal("150.00")
                        )
        );

        verify(
                productRepository,
                never()
        ).findById(anyLong());
    }

    @Test
    void rootShouldUpdatePriceWithoutOwnershipCheck() {

        authenticate(
                1L,
                "ROOT",
                "SYSTEM_ADMIN"
        );

        Product product =
                new Product(
                        100,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("100.00")
                );

        when(
                productRepository.findById(100L)
        ).thenReturn(
                Optional.of(product)
        );

        Product updated =
                productService.updatePrice(
                        100L,
                        new BigDecimal("200.00")
                );

        assertEquals(
                new BigDecimal("200.00"),
                updated.getPriceAmount()
        );

        verify(
                productRepository,
                never()
        ).existsByIdAndSellerUserId(
                anyLong(),
                anyLong()
        );
    }

    @Test
    void sellerShouldDeleteOwnProduct() {

        authenticate(
                27L,
                "SELLER_ADMIN",
                "PRODUCT_DELETE_OWN"
        );

        when(
                productRepository.existsByIdAndSellerUserId(
                        100L,
                        27L
                )
        ).thenReturn(true);

        when(
                productRepository.existsById(100L)
        ).thenReturn(true);

        productService.deleteOrThrow(100L);

        verify(productRepository)
                .existsByIdAndSellerUserId(
                        100L,
                        27L
                );

        verify(productRepository)
                .deleteById(100L);
    }

    @Test
    void sellerShouldNotDeleteAnotherSellersProduct() {

        authenticate(
                42L,
                "SELLER_ADMIN",
                "PRODUCT_DELETE_OWN"
        );

        when(
                productRepository.existsByIdAndSellerUserId(
                        100L,
                        42L
                )
        ).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        productService.deleteOrThrow(
                                100L
                        )
        );

        verify(
                productRepository,
                never()
        ).existsById(100L);

        verify(
                productRepository,
                never()
        ).deleteById(100L);
    }

    @Test
    void rootShouldDeleteAnyProductWithoutOwnershipCheck() {

        authenticate(
                1L,
                "ROOT",
                "SYSTEM_ADMIN"
        );

        when(
                productRepository.existsById(100L)
        ).thenReturn(true);

        productService.deleteOrThrow(100L);

        verify(
                productRepository,
                never()
        ).existsByIdAndSellerUserId(
                anyLong(),
                anyLong()
        );

        verify(productRepository)
                .existsById(100L);

        verify(productRepository)
                .deleteById(100L);
    }


}