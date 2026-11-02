package com.ecommerce.controller;

import com.ecommerce.checkout.SpringCheckoutService;

import com.ecommerce.exception.ResourceNotFoundException;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.ecommerce.service.CustomerService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SpringOrderService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.boot.security.oauth2.client.autoconfigure.servlet
        .OAuth2ClientWebSecurityAutoConfiguration;


@WebMvcTest(
        controllers = CheckoutController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private SpringCheckoutService checkoutService;


    @MockitoBean
    private CustomerService customerService;


    @MockitoBean
    private ProductService productService;


    @MockitoBean
    private SpringOrderService orderService;


    /*
     * =========================================================
     * POST /api/checkout
     *
     * Successful customer checkout.
     *
     * V7 SECURITY:
     * Customer identity is resolved from the authenticated
     * JWT userId claim instead of a customerId supplied in
     * the request body.
     * =========================================================
     */
    @Test
    void shouldCheckoutSuccessfully()
            throws Exception {

        long orderId = 9901001L;

        /*
         * Customer id belongs to the domain/customer table.
         * It is NOT supplied by the browser anymore.
         */
        long customerId = 8101L;

        /*
         * UserAccount id represents the authenticated identity
         * contained inside the JWT.
         */
        long userAccountId = 101L;

        long productId = 7001L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product product =
                new Product(
                        productId,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Order createdOrder =
                org.mockito.Mockito.mock(
                        Order.class
                );


        Order detailedOrder =
                org.mockito.Mockito.mock(
                        Order.class
                );


        /*
         * V7:
         * Resolve Customer using authenticated UserAccount id.
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


        when(
                productService.findByIdOrThrow(
                        productId
                )
        )
                .thenReturn(
                        product
                );


        when(
                checkoutService.checkout(
                        eq(orderId),
                        eq(customer),
                        any(Cart.class),
                        any()
                )
        )
                .thenReturn(
                        createdOrder
                );


        when(
                createdOrder.getId()
        )
                .thenReturn(
                        orderId
                );


        when(
                orderService.findByIdOrThrow(
                        orderId
                )
        )
                .thenReturn(
                        detailedOrder
                );


        when(
                detailedOrder.getId()
        )
                .thenReturn(
                        orderId
                );


        when(
                detailedOrder.getCustomer()
        )
                .thenReturn(
                        customer
                );


        when(
                detailedOrder.getItems()
        )
                .thenReturn(
                        List.of()
                );


        /*
         * customerId is intentionally absent.
         *
         * Customer identity must come from the JWT.
         */
        String requestBody =
                """
                {
                  "orderId": 9901001,
                  "paymentType": "CARD",
                  "items": [
                    {
                      "productId": 7001,
                      "quantity": 1
                    }
                  ]
                }
                """;


        mockMvc.perform(
                        post("/api/checkout")

                                /*
                                 * Simulate an authenticated CUSTOMER.
                                 *
                                 * The userId claim is the id of the
                                 * authenticated UserAccount.
                                 */
                                .principal(
                                        customerAuthentication(
                                                userAccountId
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(orderId)
                )

                /*
                 * Returning customerId in the response is safe.
                 *
                 * The important security rule is that customerId
                 * cannot be selected by the incoming request.
                 */
                .andExpect(
                        jsonPath("$.customerId")
                                .value(customerId)
                )

                .andExpect(
                        jsonPath("$.customerName")
                                .value("Rakesh")
                )

                .andExpect(
                        jsonPath("$.items")
                                .isArray()
                );


        verify(customerService)
                .findByUserAccountIdOrThrow(
                        userAccountId
                );


        verify(productService)
                .findByIdOrThrow(
                        productId
                );


        verify(checkoutService)
                .checkout(
                        eq(orderId),
                        eq(customer),
                        any(Cart.class),
                        any()
                );


        verify(orderService)
                .findByIdOrThrow(
                        orderId
                );
    }


    /*
     * =========================================================
     * Invalid checkout request.
     *
     * @Valid should reject the request before checkout
     * business services are called.
     * =========================================================
     */
    @Test
    void shouldReturnBadRequestWhenCheckoutRequestIsInvalid()
            throws Exception {

        String requestBody =
                """
                {
                }
                """;


        mockMvc.perform(
                        post("/api/checkout")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )

                .andExpect(
                        status().isBadRequest()
                );


        verifyNoInteractions(
                customerService,
                productService,
                checkoutService,
                orderService
        );
    }


    /*
     * =========================================================
     * Authenticated user has no linked Customer profile.
     *
     * V7:
     * We no longer test a client-supplied missing customerId.
     *
     * Instead, we verify that the authenticated UserAccount
     * must have a corresponding Customer profile.
     * =========================================================
     */
    @Test
    void shouldReturnNotFoundWhenAuthenticatedUserHasNoCustomerProfile()
            throws Exception {

        long userAccountId = 999L;


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


        String requestBody =
                """
                {
                  "orderId": 9901001,
                  "paymentType": "CARD",
                  "items": [
                    {
                      "productId": 7001,
                      "quantity": 1
                    }
                  ]
                }
                """;


        mockMvc.perform(
                        post("/api/checkout")

                                .principal(
                                        customerAuthentication(
                                                userAccountId
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        requestBody
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
         * Checkout must stop immediately because there
         * is no Customer profile for this UserAccount.
         */
        verifyNoInteractions(
                productService,
                checkoutService,
                orderService
        );
    }


    /*
     * =========================================================
     * Product does not exist.
     *
     * Customer is first resolved securely from JWT.
     * Product lookup then fails with 404.
     * =========================================================
     */
    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist()
            throws Exception {

        long customerId = 8101L;
        long userAccountId = 101L;
        long productId = 9999L;


        Customer customer =
                new Customer(
                        customerId,
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
                        customer
                );


        when(
                productService.findByIdOrThrow(
                        productId
                )
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Product not found"
                        )
                );


        String requestBody =
                """
                {
                  "orderId": 9901001,
                  "paymentType": "CARD",
                  "items": [
                    {
                      "productId": 9999,
                      "quantity": 1
                    }
                  ]
                }
                """;


        mockMvc.perform(
                        post("/api/checkout")

                                .principal(
                                        customerAuthentication(
                                                userAccountId
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        requestBody
                                )
                )

                .andExpect(
                        status().isNotFound()
                );


        verify(customerService)
                .findByUserAccountIdOrThrow(
                        userAccountId
                );


        verify(productService)
                .findByIdOrThrow(
                        productId
                );


        /*
         * Checkout must not start when one of the
         * requested products cannot be resolved.
         */
        verify(
                checkoutService,
                never()
        )
                .checkout(
                        anyLong(),
                        any(Customer.class),
                        any(Cart.class),
                        any()
                );


        verifyNoInteractions(
                orderService
        );
    }


    /*
     * =========================================================
     * Checkout with multiple products.
     *
     * Verifies that JWT-based customer resolution continues
     * to work when the Cart contains multiple products.
     * =========================================================
     */
    @Test
    void shouldCheckoutWithMultipleProducts()
            throws Exception {

        long orderId = 9901002L;
        long customerId = 8101L;
        long userAccountId = 101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product laptop =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Product mouse =
                new Product(
                        7401L,
                        "Wireless Mouse",
                        "Electronics",
                        2500
                );


        Order createdOrder =
                org.mockito.Mockito.mock(
                        Order.class
                );


        Order detailedOrder =
                org.mockito.Mockito.mock(
                        Order.class
                );


        when(
                customerService
                        .findByUserAccountIdOrThrow(
                                userAccountId
                        )
        )
                .thenReturn(
                        customer
                );


        when(
                productService.findByIdOrThrow(
                        7001L
                )
        )
                .thenReturn(
                        laptop
                );


        when(
                productService.findByIdOrThrow(
                        7401L
                )
        )
                .thenReturn(
                        mouse
                );


        when(
                checkoutService.checkout(
                        eq(orderId),
                        eq(customer),
                        any(Cart.class),
                        any()
                )
        )
                .thenReturn(
                        createdOrder
                );


        when(
                createdOrder.getId()
        )
                .thenReturn(
                        orderId
                );


        when(
                orderService.findByIdOrThrow(
                        orderId
                )
        )
                .thenReturn(
                        detailedOrder
                );


        when(
                detailedOrder.getId()
        )
                .thenReturn(
                        orderId
                );


        when(
                detailedOrder.getCustomer()
        )
                .thenReturn(
                        customer
                );


        when(
                detailedOrder.getItems()
        )
                .thenReturn(
                        List.of()
                );


        /*
         * No customerId in the request.
         */
        String requestBody =
                """
                {
                  "orderId": 9901002,
                  "paymentType": "UPI",
                  "items": [
                    {
                      "productId": 7001,
                      "quantity": 1
                    },
                    {
                      "productId": 7401,
                      "quantity": 2
                    }
                  ]
                }
                """;


        mockMvc.perform(
                        post("/api/checkout")

                                .principal(
                                        customerAuthentication(
                                                userAccountId
                                        )
                                )

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        requestBody
                                )
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.id")
                                .value(orderId)
                )

                .andExpect(
                        jsonPath("$.customerId")
                                .value(customerId)
                );


        verify(customerService)
                .findByUserAccountIdOrThrow(
                        userAccountId
                );


        verify(productService)
                .findByIdOrThrow(
                        7001L
                );


        verify(productService)
                .findByIdOrThrow(
                        7401L
                );


        verify(checkoutService)
                .checkout(
                        eq(orderId),
                        eq(customer),
                        any(Cart.class),
                        any()
                );


        verify(orderService)
                .findByIdOrThrow(
                        orderId
                );
    }

    /*
     * =========================================================
     * TEST AUTHENTICATION HELPER
     * ---------------------------------------------------------
     * @WebMvcTest does not run our complete production JWT
     * authentication pipeline.
     *
     * Therefore we provide the authenticated servlet principal
     * directly to MockMvc.
     *
     * This principal has the same important information that
     * the real application JWT contains:
     *
     * - userId
     * - role
     * - ROLE_CUSTOMER authority
     * =========================================================
     */
    private JwtAuthenticationToken customerAuthentication(
            long userAccountId) {

        Instant now =
                Instant.now();

        Jwt jwt =
                Jwt.withTokenValue(
                                "checkout-test-token"
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