package com.ecommerce.controller;

import com.ecommerce.checkout.SpringCheckoutService;

import com.ecommerce.exception.ResourceNotFoundException;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;

import com.ecommerce.service.CustomerService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SpringOrderService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CheckoutController.class)
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
     * POST /api/checkout
     *
     * Successful checkout
     */
    @Test
    void shouldCheckoutSuccessfully()
            throws Exception {

        long orderId = 9901001L;
        long customerId = 8101L;
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


        when(
                customerService.findByIdOrThrow(
                        customerId
                )
        )
                .thenReturn(customer);


        when(
                productService.findByIdOrThrow(
                        productId
                )
        )
                .thenReturn(product);


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


        String requestBody =
                """
                {
                  "orderId": 9901001,
                  "customerId": 8101,
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
                                .value(9901001)
                )
                .andExpect(
                        jsonPath("$.customerId")
                                .value(8101)
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
                .findByIdOrThrow(
                        customerId
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
     * Invalid checkout request
     *
     * @Valid should reject the request
     * before any service is called.
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
     * Customer does not exist
     */
    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist()
            throws Exception {

        long customerId = 9999L;


        when(
                customerService.findByIdOrThrow(
                        customerId
                )
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Customer not found"
                        )
                );


        String requestBody =
                """
                {
                  "orderId": 9901001,
                  "customerId": 9999,
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
                .findByIdOrThrow(
                        customerId
                );


        verifyNoInteractions(
                productService,
                checkoutService,
                orderService
        );
    }


    /*
     * Product does not exist
     */
    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist()
            throws Exception {

        long customerId = 8101L;
        long productId = 9999L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        when(
                customerService.findByIdOrThrow(
                        customerId
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
                  "customerId": 8101,
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
                .findByIdOrThrow(
                        customerId
                );


        verify(productService)
                .findByIdOrThrow(
                        productId
                );


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
     * Checkout with multiple products
     */
    @Test
    void shouldCheckoutWithMultipleProducts()
            throws Exception {

        long orderId = 9901002L;
        long customerId = 8101L;


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
                customerService.findByIdOrThrow(
                        customerId
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


        String requestBody =
                """
                {
                  "orderId": 9901002,
                  "customerId": 8101,
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
                                .value(9901002)
                )
                .andExpect(
                        jsonPath("$.customerId")
                                .value(8101)
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
}