package com.ecommerce.security;

import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;

import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.model.SellerApprovalStatus;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
class ProductCreationMethodSecurityTest {

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


    /*
     * APPROVED SELLER_ADMIN
     *
     * Expected:
     * - PRODUCT_CREATE permission accepted
     * - Seller found using JWT userId
     * - Seller is APPROVED
     * - Product automatically assigned to Seller
     * - Product saved
     */
    @Test
    void approvedSellerShouldCreateOwnProduct() {

        JwtAuthenticationToken authentication =
                sellerAuthentication(
                        27L
                );

        setAuthentication(
                authentication
        );


        Seller seller =
                mock(Seller.class);

        when(
                seller.getApprovalStatus()
        ).thenReturn(
                SellerApprovalStatus.APPROVED
        );


        when(
                sellerRepository.findByUserId(
                        27L
                )
        ).thenReturn(
                Optional.of(seller)
        );


        Product product =
                new Product(
                        100L,
                        "Gaming Laptop",
                        "Electronics",
                        new BigDecimal("120000.00")
                );


        when(
                productRepository.save(product)
        ).thenReturn(product);


        Product result =
                productService.save(
                        product,
                        authentication
                );


        assertSame(
                product,
                result
        );

        assertSame(
                seller,
                result.getSeller()
        );


        verify(sellerRepository)
                .findByUserId(
                        27L
                );

        verify(productRepository)
                .save(product);
    }


    /*
     * PENDING SELLER_ADMIN
     *
     * Permission is valid,
     * but business approval is not.
     */
    @Test
    void pendingSellerShouldNotCreateProduct() {

        JwtAuthenticationToken authentication =
                sellerAuthentication(
                        27L
                );

        setAuthentication(
                authentication
        );


        Seller seller =
                mock(Seller.class);

        when(
                seller.getApprovalStatus()
        ).thenReturn(
                SellerApprovalStatus.PENDING
        );


        when(
                sellerRepository.findByUserId(
                        27L
                )
        ).thenReturn(
                Optional.of(seller)
        );


        Product product =
                new Product(
                        100L,
                        "Gaming Laptop",
                        "Electronics",
                        new BigDecimal("120000.00")
                );


        AccessDeniedException exception =
                assertThrows(
                        AccessDeniedException.class,
                        () ->
                                productService.save(
                                        product,
                                        authentication
                                )
                );


        assertEquals(
                "Seller is not approved",
                exception.getMessage()
        );


        verify(sellerRepository)
                .findByUserId(
                        27L
                );

        verify(
                productRepository,
                never()
        ).save(
                any(Product.class)
        );
    }


    /*
     * CUSTOMER
     *
     * Should be rejected by @PreAuthorize
     * before ProductService business logic executes.
     */
    @Test
    void customerShouldNotCreateProduct() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
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


        setAuthentication(
                authentication
        );


        Product product =
                new Product(
                        100L,
                        "Gaming Laptop",
                        "Electronics",
                        new BigDecimal("120000.00")
                );


        assertThrows(
                AccessDeniedException.class,
                () ->
                        productService.save(
                                product,
                                authentication
                        )
        );


        verifyNoInteractions(
                sellerRepository
        );

        verifyNoInteractions(
                productRepository
        );
    }


    /*
     * ROOT
     *
     * ROOT can create catalogue/system products
     * without having a Seller profile.
     */
    @Test
    void rootShouldCreateProductWithoutSellerProfile() {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "root@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_ROOT"
                                ),
                                new SimpleGrantedAuthority(
                                        "SYSTEM_ADMIN"
                                ),
                                new SimpleGrantedAuthority(
                                        "PRODUCT_CREATE"
                                )
                        )
                );


        setAuthentication(
                authentication
        );


        Product product =
                new Product(
                        100L,
                        "Platform Product",
                        "Electronics",
                        new BigDecimal("50000.00")
                );


        when(
                productRepository.save(product)
        ).thenReturn(product);


        Product result =
                productService.save(
                        product,
                        authentication
                );


        assertSame(
                product,
                result
        );

        assertNull(
                result.getSeller()
        );


        verifyNoInteractions(
                sellerRepository
        );

        verify(productRepository)
                .save(product);
    }


    /*
     * Builds a real JWT authentication
     * representing SELLER_ADMIN.
     */
    private JwtAuthenticationToken sellerAuthentication(
            Long userId
    ) {

        Instant now =
                Instant.now();


        Jwt jwt =
                Jwt.withTokenValue(
                                "seller-test-token"
                        )
                        .header(
                                "alg",
                                "HS256"
                        )
                        .subject(
                                "seller@test.com"
                        )
                        .issuedAt(now)
                        .expiresAt(
                                now.plusSeconds(
                                        900
                                )
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
                                "PRODUCT_CREATE"
                        )
                )
        );
    }


    private void setAuthentication(
            Authentication authentication
    ) {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }
}