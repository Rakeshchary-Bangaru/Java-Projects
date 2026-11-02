package com.ecommerce.controller;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.service.CustomerService;
import com.ecommerce.service.SpringOrderService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

import com.ecommerce.config.MvcControllerTest;


@MvcControllerTest(
        controllers = CustomerOrderController.class
)
class CustomerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private CustomerService customerService;


    @MockitoBean
    private SpringOrderService orderService;


    /*
     * =========================================================
     * GET /api/customer/orders
     *
     * Authenticated CUSTOMER should receive only orders
     * belonging to the Customer profile linked to their
     * JWT UserAccount id.
     * =========================================================
     */
    @Test
    void shouldReturnOrdersForAuthenticatedCustomer()
            throws Exception {

        long userAccountId = 101L;
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


        /*
         * JWT userId is converted into the linked
         * Customer profile.
         */
        when(
                customerService
                        .findByUserAccountIdOrThrow(
                                userAccountId
                        )
        )
                .thenReturn(
                        customer
                );


        /*
         * Only this customer's id is passed to the
         * order service.
         */
        when(
                orderService
                        .findByCustomerId(
                                customerId
                        )
        )
                .thenReturn(
                        List.of(
                                order1,
                                order2
                        )
                );


        /*
         * Minimal response data required by
         * CustomerOrderController.toResponse().
         */
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


        mockMvc.perform(
                        get("/api/customer/orders")

                                .principal(
                                        customerAuthentication(
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
                                .value(2)
                )

                .andExpect(
                        jsonPath("$[0].id")
                                .value(9901001L)
                )

                .andExpect(
                        jsonPath("$[0].customerId")
                                .value(customerId)
                )

                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("Rakesh")
                )

                .andExpect(
                        jsonPath("$[1].id")
                                .value(9901002L)
                );


        verify(customerService)
                .findByUserAccountIdOrThrow(
                        userAccountId
                );


        verify(orderService)
                .findByCustomerId(
                        customerId
                );
    }


    /*
     * =========================================================
     * CUSTOMER CANNOT SELECT ANOTHER CUSTOMER
     * ---------------------------------------------------------
     * Even if somebody manually adds:
     *
     * ?customerId=9999999
     *
     * the controller must ignore it.
     *
     * Customer identity still comes exclusively from JWT.
     * =========================================================
     */
    @Test
    void shouldIgnoreClientSuppliedCustomerId()
            throws Exception {

        long userAccountId = 101L;

        long authenticatedCustomerId =
                8101L;

        long maliciousCustomerId =
                9999999L;


        Customer authenticatedCustomer =
                new Customer(
                        authenticatedCustomerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        when(
                customerService
                        .findByUserAccountIdOrThrow(
                                userAccountId
                        )
        )
                .thenReturn(
                        authenticatedCustomer
                );


        when(
                orderService
                        .findByCustomerId(
                                authenticatedCustomerId
                        )
        )
                .thenReturn(
                        List.of()
                );


        /*
         * A malicious caller attempts to choose a different
         * customer by adding customerId to the URL.
         *
         * CustomerOrderController does not define such an
         * input parameter, so it cannot influence ownership.
         */
        mockMvc.perform(
                        get(
                                "/api/customer/orders"
                                        + "?customerId="
                                        + maliciousCustomerId
                        )

                                .principal(
                                        customerAuthentication(
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


        /*
         * Customer identity came from JWT.
         */
        verify(customerService)
                .findByUserAccountIdOrThrow(
                        userAccountId
                );


        /*
         * Orders were loaded using the authenticated
         * customer's id.
         */
        verify(orderService)
                .findByCustomerId(
                        authenticatedCustomerId
                );


        /*
         * The malicious customer id was never used.
         */
        verify(
                orderService,
                never()
        )
                .findByCustomerId(
                        maliciousCustomerId
                );
    }


    /*
     * =========================================================
     * AUTHENTICATED USER WITHOUT CUSTOMER PROFILE
     * ---------------------------------------------------------
     * If the UserAccount is not linked to a Customer,
     * order lookup must stop immediately.
     * =========================================================
     */
    @Test
    void shouldReturnNotFoundWhenCustomerProfileDoesNotExist()
            throws Exception {

        long userAccountId =
                999L;


        when(
                customerService
                        .findByUserAccountIdOrThrow(
                                userAccountId
                        )
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Customer profile not found for authenticated user"
                        )
                );


        mockMvc.perform(
                        get("/api/customer/orders")

                                .principal(
                                        customerAuthentication(
                                                userAccountId
                                        )
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        verify(customerService)
                .findByUserAccountIdOrThrow(
                        userAccountId
                );


        /*
         * No order lookup should happen because there
         * is no linked Customer profile.
         */
        verifyNoInteractions(
                orderService
        );
    }


    /*
     * =========================================================
     * TEST AUTHENTICATION HELPER
     * ---------------------------------------------------------
     * Matches the approach already used successfully by
     * CheckoutControllerTest.
     *
     * @WebMvcTest does not execute our complete production
     * JWT authentication pipeline, therefore we provide
     * the authenticated JwtAuthenticationToken directly.
     * =========================================================
     */
    private JwtAuthenticationToken customerAuthentication(
            long userAccountId
    ) {

        Instant now =
                Instant.now();


        Jwt jwt =
                Jwt.withTokenValue(
                                "customer-orders-test-token"
                        )

                        .header(
                                "alg",
                                "none"
                        )

                        .subject(
                                "customer@test.com"
                        )

                        .claim(
                                "userId",
                                userAccountId
                        )

                        .claim(
                                "role",
                                "CUSTOMER"
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
                                "ROLE_CUSTOMER"
                        )
                )
        );
    }
}