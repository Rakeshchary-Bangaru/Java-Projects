package com.ecommerce.security;

import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.seller.repository.SellerRepository;
import com.ecommerce.service.ProductService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
class SellerProductListingSecurityTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private SpringDataProductRepository productRepository;

    @MockitoBean
    private SellerRepository sellerRepository;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }


    @Test
    void sellerAdminCanReadOnlyOwnProducts() {

        Authentication authentication =
                sellerAuthentication(27L);

        authenticate(authentication);


        Product product1 =
                new Product(
                        101L,
                        "Laptop",
                        "Electronics",
                        new BigDecimal("89999.00")
                );

        Product product2 =
                new Product(
                        102L,
                        "Mouse",
                        "Electronics",
                        new BigDecimal("1999.00")
                );


        when(
                productRepository
                        .findAllBySellerUserId(27L)
        )
                .thenReturn(
                        List.of(
                                product1,
                                product2
                        )
                );


        List<Product> products =
                productService
                        .findAllForSeller(
                                authentication
                        );


        assertEquals(
                2,
                products.size()
        );

        assertEquals(
                101L,
                products.get(0).getId()
        );

        assertEquals(
                102L,
                products.get(1).getId()
        );


        verify(productRepository)
                .findAllBySellerUserId(
                        27L
                );

        verify(
                productRepository,
                never()
        ).findAll();
    }


    @Test
    void customerCannotReadSellerProductList() {

        Authentication authentication =
                customerAuthentication();

        authenticate(authentication);


        assertThrows(
                AccessDeniedException.class,
                () ->
                        productService
                                .findAllForSeller(
                                        authentication
                                )
        );


        verifyNoInteractions(
                productRepository
        );

        verifyNoInteractions(
                sellerRepository
        );
    }


    @Test
    void itSupportCannotReadSellerProductList() {

        Authentication authentication =
                itSupportAuthentication();

        authenticate(authentication);


        assertThrows(
                AccessDeniedException.class,
                () ->
                        productService
                                .findAllForSeller(
                                        authentication
                                )
        );


        verifyNoInteractions(
                productRepository
        );

        verifyNoInteractions(
                sellerRepository
        );
    }


    private void authenticate(
            Authentication authentication
    ) {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }


    private Authentication sellerAuthentication(
            long userId
    ) {

        Instant now =
                Instant.now();


        Jwt jwt =
                Jwt.withTokenValue(
                                "seller-product-list-token"
                        )
                        .header(
                                "alg",
                                "HS256"
                        )
                        .claim(
                                "userId",
                                userId
                        )
                        .claim(
                                "role",
                                "SELLER_ADMIN"
                        )
                        .claim(
                                "email",
                                "seller@test.com"
                        )
                        .issuedAt(now)
                        .expiresAt(
                                now.plusSeconds(900)
                        )
                        .build();


        return new JwtAuthenticationToken(
                jwt,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_SELLER_ADMIN"
                        ),
                        new SimpleGrantedAuthority(
                                "PRODUCT_CREATE"
                        ),
                        new SimpleGrantedAuthority(
                                "PRODUCT_UPDATE_OWN"
                        ),
                        new SimpleGrantedAuthority(
                                "PRODUCT_DELETE_OWN"
                        )
                )
        );
    }


    private Authentication customerAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                "customer@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_CUSTOMER"
                        ),
                        new SimpleGrantedAuthority(
                                "ORDER_READ_OWN"
                        )
                )
        );
    }


    private Authentication itSupportAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                "support@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_IT_SUPPORT"
                        ),
                        new SimpleGrantedAuthority(
                                "ROLE_MANAGE"
                        )
                )
        );
    }
}