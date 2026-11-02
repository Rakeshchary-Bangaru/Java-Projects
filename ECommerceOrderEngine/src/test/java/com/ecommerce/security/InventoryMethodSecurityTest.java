package com.ecommerce.security;

import com.ecommerce.controller.InventoryController;
import com.ecommerce.dto.InventoryQuantityRequest;
import com.ecommerce.dto.InventoryResponse;
import com.ecommerce.dto.ProductAvailabilityResponse;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.service.SpringInventoryService;

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

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class InventoryMethodSecurityTest {

    @Autowired
    private InventoryController inventoryController;

    @MockitoBean
    private SpringInventoryService inventoryService;

    @MockitoBean
    private SpringDataProductRepository productRepository;


    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void rootCanReadAnyInventory() {

        authenticate(rootAuthentication());

        when(inventoryService.getStock(101L))
                .thenReturn(25);

        InventoryResponse response =
                inventoryController
                        .getStock(101L)
                        .getBody();

        assertNotNull(response);
        assertEquals(25, response.getQuantity());

        verify(inventoryService)
                .getStock(101L);
    }


    @Test
    void sellerCanReadOwnInventory() {

        authenticate(sellerAuthentication(27L));

        when(productRepository
                .existsByIdAndSellerUserId(
                        101L,
                        27L
                ))
                .thenReturn(true);

        when(inventoryService.getStock(101L))
                .thenReturn(15);

        InventoryResponse response =
                inventoryController
                        .getStock(101L)
                        .getBody();

        assertNotNull(response);
        assertEquals(15, response.getQuantity());

        verify(inventoryService)
                .getStock(101L);
    }


    @Test
    void sellerCannotReadAnotherSellersInventory() {

        authenticate(sellerAuthentication(27L));

        when(productRepository
                .existsByIdAndSellerUserId(
                        101L,
                        27L
                ))
                .thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        inventoryController
                                .getStock(101L)
        );

        verifyNoInteractions(inventoryService);
    }


    @Test
    void customerCannotReadExactInventory() {

        authenticate(customerAuthentication());

        assertThrows(
                AccessDeniedException.class,
                () ->
                        inventoryController
                                .getStock(101L)
        );

        verifyNoInteractions(inventoryService);
        verifyNoInteractions(productRepository);
    }


    @Test
    void itSupportCannotReadExactInventory() {

        authenticate(itSupportAuthentication());

        assertThrows(
                AccessDeniedException.class,
                () ->
                        inventoryController
                                .getStock(101L)
        );

        verifyNoInteractions(inventoryService);
        verifyNoInteractions(productRepository);
    }


    @Test
    void publicUserCanReadAvailabilityWithoutExactQuantity() {

        SecurityContextHolder.clearContext();

        when(inventoryService.getStock(101L))
                .thenReturn(8);

        ProductAvailabilityResponse response =
                inventoryController
                        .getAvailability(101L)
                        .getBody();

        assertNotNull(response);

        assertEquals(
                101L,
                response.productId()
        );

        assertTrue(
                response.inStock()
        );

        assertEquals(
                "IN_STOCK",
                response.availability()
        );
    }


    @Test
    void sellerCanSetStockForOwnProduct() {

        authenticate(sellerAuthentication(27L));

        when(productRepository
                .existsByIdAndSellerUserId(
                        101L,
                        27L
                ))
                .thenReturn(true);

        when(inventoryService.getStock(101L))
                .thenReturn(40);

        InventoryQuantityRequest request =
                new InventoryQuantityRequest();

        request.setQuantity(40);

        InventoryResponse response =
                inventoryController
                        .setStock(
                                101L,
                                request
                        )
                        .getBody();

        assertNotNull(response);
        assertEquals(40, response.getQuantity());

        verify(inventoryService)
                .setStock(
                        101L,
                        40
                );
    }


    @Test
    void sellerCannotSetStockForAnotherSellersProduct() {

        authenticate(sellerAuthentication(27L));

        when(productRepository
                .existsByIdAndSellerUserId(
                        101L,
                        27L
                ))
                .thenReturn(false);

        InventoryQuantityRequest request =
                new InventoryQuantityRequest();

        request.setQuantity(50);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        inventoryController
                                .setStock(
                                        101L,
                                        request
                                )
        );

        verifyNoInteractions(inventoryService);
    }


    @Test
    void rootCanAddStockToAnyProduct() {

        authenticate(rootAuthentication());

        when(inventoryService.getStock(101L))
                .thenReturn(35);

        InventoryQuantityRequest request =
                new InventoryQuantityRequest();

        request.setQuantity(10);

        InventoryResponse response =
                inventoryController
                        .addStock(
                                101L,
                                request
                        )
                        .getBody();

        assertNotNull(response);
        assertEquals(35, response.getQuantity());

        verify(inventoryService)
                .addStock(
                        101L,
                        10
                );

        verifyNoInteractions(productRepository);
    }


    @Test
    void sellerCanAddStockToOwnProduct() {

        authenticate(sellerAuthentication(27L));

        when(productRepository
                .existsByIdAndSellerUserId(
                        101L,
                        27L
                ))
                .thenReturn(true);

        when(inventoryService.getStock(101L))
                .thenReturn(30);

        InventoryQuantityRequest request =
                new InventoryQuantityRequest();

        request.setQuantity(5);

        InventoryResponse response =
                inventoryController
                        .addStock(
                                101L,
                                request
                        )
                        .getBody();

        assertNotNull(response);
        assertEquals(30, response.getQuantity());

        verify(inventoryService)
                .addStock(
                        101L,
                        5
                );
    }


    @Test
    void sellerCannotAddStockToAnotherSellersProduct() {

        authenticate(sellerAuthentication(27L));

        when(productRepository
                .existsByIdAndSellerUserId(
                        101L,
                        27L
                ))
                .thenReturn(false);

        InventoryQuantityRequest request =
                new InventoryQuantityRequest();

        request.setQuantity(5);

        assertThrows(
                AccessDeniedException.class,
                () ->
                        inventoryController
                                .addStock(
                                        101L,
                                        request
                                )
        );

        verifyNoInteractions(inventoryService);
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


    private Authentication rootAuthentication() {

        return new UsernamePasswordAuthenticationToken(
                "root@test.com",
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_ROOT"
                        ),
                        new SimpleGrantedAuthority(
                                "SYSTEM_ADMIN"
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


    private Authentication sellerAuthentication(
            long userId
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
                                "INVENTORY_UPDATE_OWN"
                        )
                )
        );
    }
}