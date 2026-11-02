package com.ecommerce.controller;

import com.ecommerce.config.MvcControllerTest;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;

import com.ecommerce.service.SpringOrderService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;


@MvcControllerTest(
        controllers = RootOrderController.class
)
class RootOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private SpringOrderService orderService;


    /*
     * =========================================================
     * GET /api/root/orders
     * ---------------------------------------------------------
     * ROOT can retrieve the global order list.
     * =========================================================
     */
    @Test
    void shouldReturnAllOrdersForRoot()
            throws Exception {

        long customerId = 8101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Order order1 =
                org.mockito.Mockito.mock(
                        Order.class
                );


        Order order2 =
                org.mockito.Mockito.mock(
                        Order.class
                );


        when(order1.getId())
                .thenReturn(9901001L);

        when(order1.getCustomer())
                .thenReturn(customer);

        when(order1.getItems())
                .thenReturn(List.of());


        when(order2.getId())
                .thenReturn(9901002L);

        when(order2.getCustomer())
                .thenReturn(customer);

        when(order2.getItems())
                .thenReturn(List.of());


        when(
                orderService.findAll()
        )
                .thenReturn(
                        List.of(
                                order1,
                                order2
                        )
                );


        mockMvc.perform(
                        get("/api/root/orders")

                                .principal(
                                        rootAuthentication()
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
                                .value(2)
                )

                .andExpect(
                        jsonPath("$[0].id")
                                .value(9901001L)
                )

                .andExpect(
                        jsonPath("$[1].id")
                                .value(9901002L)
                );


        verify(orderService)
                .findAll();
    }


    /*
     * =========================================================
     * GET /api/root/orders/{id}
     * ---------------------------------------------------------
     * ROOT can inspect any individual order.
     * =========================================================
     */
    @Test
    void shouldReturnOrderByIdForRoot()
            throws Exception {

        long orderId = 9901001L;

        long customerId = 8101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Order order =
                org.mockito.Mockito.mock(
                        Order.class
                );


        when(
                orderService
                        .findByIdOrThrow(
                                orderId
                        )
        )
                .thenReturn(
                        order
                );


        when(order.getId())
                .thenReturn(orderId);

        when(order.getCustomer())
                .thenReturn(customer);

        when(order.getItems())
                .thenReturn(List.of());


        mockMvc.perform(
                        get(
                                "/api/root/orders/"
                                        + orderId
                        )

                                .principal(
                                        rootAuthentication()
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(orderId)
                )

                .andExpect(
                        jsonPath("$.customerId")
                                .value(customerId)
                )

                .andExpect(
                        jsonPath("$.customerName")
                                .value("Rakesh")
                );


        verify(orderService)
                .findByIdOrThrow(
                        orderId
                );
    }


    /*
     * =========================================================
     * PATCH /api/root/orders/{id}/status
     * ---------------------------------------------------------
     * ROOT can update the global order status.
     * =========================================================
     */
    @Test
    void shouldUpdateOrderStatusForRoot()
            throws Exception {

        long orderId = 9901001L;

        long customerId = 8101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        /*
         * Use an actual value from the project's enum rather
         * than assuming a particular status constant.
         */
        OrderStatus newStatus =
                OrderStatus.values()[0];


        Order order =
                org.mockito.Mockito.mock(
                        Order.class
                );


        when(
                orderService.updateStatus(
                        orderId,
                        newStatus
                )
        )
                .thenReturn(
                        order
                );


        when(order.getId())
                .thenReturn(orderId);

        when(order.getCustomer())
                .thenReturn(customer);

        when(order.getStatus())
                .thenReturn(newStatus);

        when(order.getItems())
                .thenReturn(List.of());


        String requestBody =
                """
                {
                  "status": "%s"
                }
                """.formatted(
                        newStatus.name()
                );


        mockMvc.perform(
                        patch(
                                "/api/root/orders/"
                                        + orderId
                                        + "/status"
                        )

                                .principal(
                                        rootAuthentication()
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        requestBody
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(orderId)
                )

                .andExpect(
                        jsonPath("$.status")
                                .value(
                                        newStatus.name()
                                )
                );


        verify(orderService)
                .updateStatus(
                        orderId,
                        newStatus
                );
    }


    /*
     * =========================================================
     * ROOT TEST AUTHENTICATION
     * ---------------------------------------------------------
     * Same approach used by CheckoutControllerTest and
     * CustomerOrderControllerTest.
     *
     * The production security layer creates this principal
     * from the signed JWT. In @WebMvcTest we supply it
     * directly.
     * =========================================================
     */
    private JwtAuthenticationToken rootAuthentication() {

        Instant now =
                Instant.now();


        Jwt jwt =
                Jwt.withTokenValue(
                                "root-order-test-token"
                        )

                        .header(
                                "alg",
                                "none"
                        )

                        .subject(
                                "root@test.com"
                        )

                        .claim(
                                "userId",
                                1L
                        )

                        .claim(
                                "role",
                                "ROOT"
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
                                "ROLE_ROOT"
                        )
                )
        );
    }
}