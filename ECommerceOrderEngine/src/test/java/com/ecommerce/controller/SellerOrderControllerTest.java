package com.ecommerce.controller;

import com.ecommerce.config.MvcControllerTest;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;

import com.ecommerce.seller.service.SellerOrderService;
import com.ecommerce.seller.service.SellerOrderView;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;


@MvcControllerTest(
        controllers = SellerOrderController.class
)
class SellerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private SellerOrderService sellerOrderService;


    /*
     * =========================================================
     * GET /api/seller/orders
     * ---------------------------------------------------------
     * SELLER_ADMIN receives only the seller-owned items
     * provided by SellerOrderService.
     * =========================================================
     */
    @Test
    void shouldReturnSellerOwnedOrders()
            throws Exception {

        long userAccountId =
                201L;


        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Order order =
                org.mockito.Mockito.mock(
                        Order.class
                );


        Product laptop =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        80000
                );


        Product mouse =
                new Product(
                        7002L,
                        "Mouse",
                        "Electronics",
                        2000
                );


        OrderItem laptopItem =
                org.mockito.Mockito.mock(
                        OrderItem.class
                );


        OrderItem mouseItem =
                org.mockito.Mockito.mock(
                        OrderItem.class
                );


        when(order.getId())
                .thenReturn(
                        9901001L
                );

        when(order.getCustomer())
                .thenReturn(
                        customer
                );


        when(laptopItem.getProduct())
                .thenReturn(
                        laptop
                );

        when(laptopItem.getQuantity())
                .thenReturn(
                        1
                );

        when(laptopItem.getSubtotal())
                .thenReturn(
                        80000.0
                );


        when(mouseItem.getProduct())
                .thenReturn(
                        mouse
                );

        when(mouseItem.getQuantity())
                .thenReturn(
                        1
                );

        when(mouseItem.getSubtotal())
                .thenReturn(
                        2000.0
                );


        SellerOrderView sellerOrder =
                new SellerOrderView(
                        order,
                        List.of(
                                laptopItem,
                                mouseItem
                        ),
                        new BigDecimal(
                                "82000.00"
                        )
                );


        when(
                sellerOrderService
                        .findOrdersForSellerUser(
                                userAccountId
                        )
        )
                .thenReturn(
                        List.of(
                                sellerOrder
                        )
                );


        mockMvc.perform(
                        get("/api/seller/orders")

                                .principal(
                                        sellerAuthentication(
                                                userAccountId
                                        )
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$")
                                .isArray()
                )

                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )

                .andExpect(
                        jsonPath("$[0].orderId")
                                .value(9901001L)
                )

                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("Rakesh")
                )

                .andExpect(
                        jsonPath("$[0].sellerSubtotal")
                                .value(82000.00)
                )

                .andExpect(
                        jsonPath("$[0].items.length()")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$[0].items[0].productId")
                                .value(7001L)
                )

                .andExpect(
                        jsonPath("$[0].items[0].productName")
                                .value("Laptop")
                )

                .andExpect(
                        jsonPath("$[0].items[1].productId")
                                .value(7002L)
                )

                .andExpect(
                        jsonPath("$[0].items[1].productName")
                                .value("Mouse")
                );


        verify(sellerOrderService)
                .findOrdersForSellerUser(
                        userAccountId
                );
    }


    /*
     * =========================================================
     * SELLER ID CANNOT BE CHOSEN BY CLIENT
     * ---------------------------------------------------------
     * Even if somebody manually adds:
     *
     * ?sellerId=999
     *
     * the controller still uses JWT userId.
     * =========================================================
     */
    @Test
    void shouldIgnoreClientSuppliedSellerId()
            throws Exception {

        long authenticatedUserAccountId =
                201L;

        long maliciousSellerId =
                999L;


        when(
                sellerOrderService
                        .findOrdersForSellerUser(
                                authenticatedUserAccountId
                        )
        )
                .thenReturn(
                        List.of()
                );


        mockMvc.perform(
                        get(
                                "/api/seller/orders"
                                        + "?sellerId="
                                        + maliciousSellerId
                        )

                                .principal(
                                        sellerAuthentication(
                                                authenticatedUserAccountId
                                        )
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$")
                                .isArray()
                )

                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );


        verify(sellerOrderService)
                .findOrdersForSellerUser(
                        authenticatedUserAccountId
                );


        /*
         * The malicious value must never be treated as
         * the authenticated identity.
         */
        verify(
                sellerOrderService,
                never()
        )
                .findOrdersForSellerUser(
                        maliciousSellerId
                );
    }


    /*
     * =========================================================
     * SELLER WITH NO ORDERS
     * =========================================================
     */
    @Test
    void shouldReturnEmptyListWhenSellerHasNoOrders()
            throws Exception {

        long userAccountId =
                201L;


        when(
                sellerOrderService
                        .findOrdersForSellerUser(
                                userAccountId
                        )
        )
                .thenReturn(
                        List.of()
                );


        mockMvc.perform(
                        get("/api/seller/orders")

                                .principal(
                                        sellerAuthentication(
                                                userAccountId
                                        )
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$")
                                .isArray()
                )

                .andExpect(
                        jsonPath("$.length()")
                                .value(0)
                );


        verify(sellerOrderService)
                .findOrdersForSellerUser(
                        userAccountId
                );
    }


    /*
     * =========================================================
     * SELLER TEST AUTHENTICATION
     * ---------------------------------------------------------
     * Same JWT principal approach used by the other
     * controller slice tests.
     * =========================================================
     */
    private JwtAuthenticationToken sellerAuthentication(
            long userAccountId
    ) {

        Instant now =
                Instant.now();


        Jwt jwt =
                Jwt.withTokenValue(
                                "seller-order-test-token"
                        )

                        .header(
                                "alg",
                                "none"
                        )

                        .subject(
                                "seller@test.com"
                        )

                        .claim(
                                "userId",
                                userAccountId
                        )

                        .claim(
                                "role",
                                "SELLER_ADMIN"
                        )

                        .issuedAt(
                                now
                        )

                        .expiresAt(
                                now.plusSeconds(900)
                        )

                        .build();


        return new JwtAuthenticationToken(
                jwt,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_SELLER_ADMIN"
                        )
                )
        );
    }
}