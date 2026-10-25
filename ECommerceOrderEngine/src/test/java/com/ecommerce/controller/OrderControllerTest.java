package com.ecommerce.controller;

import com.ecommerce.exception.ResourceNotFoundException;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;

import com.ecommerce.service.SpringOrderService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private SpringOrderService orderService;


    /*
     * GET /api/orders
     */
    @Test
    void shouldReturnAllOrders()
            throws Exception {

        Customer customer1 =
                mock(Customer.class);

        Customer customer2 =
                mock(Customer.class);


        Order order1 =
                mock(Order.class);

        Order order2 =
                mock(Order.class);


        when(customer1.getId())
                .thenReturn(8101L);

        when(customer1.getName())
                .thenReturn("Rakesh");


        when(customer2.getId())
                .thenReturn(8102L);

        when(customer2.getName())
                .thenReturn("Alice");


        when(order1.getId())
                .thenReturn(9901001L);

        when(order1.getCustomer())
                .thenReturn(customer1);

        when(order1.getItems())
                .thenReturn(List.of());


        when(order2.getId())
                .thenReturn(9901002L);

        when(order2.getCustomer())
                .thenReturn(customer2);

        when(order2.getItems())
                .thenReturn(List.of());


        when(orderService.findAll())
                .thenReturn(
                        List.of(
                                order1,
                                order2
                        )
                );


        mockMvc.perform(
                        get("/api/orders")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(9901001)
                )
                .andExpect(
                        jsonPath("$[0].customerId")
                                .value(8101)
                )
                .andExpect(
                        jsonPath("$[0].customerName")
                                .value("Rakesh")
                )
                .andExpect(
                        jsonPath("$[0].items")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(9901002)
                )
                .andExpect(
                        jsonPath("$[1].customerName")
                                .value("Alice")
                );


        verify(orderService)
                .findAll();
    }


    /*
     * GET /api/orders/{id}
     */
    @Test
    void shouldReturnOrderById()
            throws Exception {

        long orderId =
                9901001L;


        Customer customer =
                mock(Customer.class);

        Order order =
                mock(Order.class);


        when(customer.getId())
                .thenReturn(8101L);

        when(customer.getName())
                .thenReturn("Rakesh");


        when(order.getId())
                .thenReturn(orderId);

        when(order.getCustomer())
                .thenReturn(customer);

        when(order.getItems())
                .thenReturn(List.of());


        when(
                orderService.findByIdOrThrow(
                        orderId
                )
        )
                .thenReturn(order);


        mockMvc.perform(
                        get(
                                "/api/orders/{id}",
                                orderId
                        )
                )
                .andExpect(
                        status().isOk()
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


        verify(orderService)
                .findByIdOrThrow(
                        orderId
                );
    }


    /*
     * GET missing order -> 404
     */
    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist()
            throws Exception {

        long orderId =
                9999999L;


        when(
                orderService.findByIdOrThrow(
                        orderId
                )
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Order not found"
                        )
                );


        mockMvc.perform(
                        get(
                                "/api/orders/{id}",
                                orderId
                        )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(orderService)
                .findByIdOrThrow(
                        orderId
                );
    }


    /*
     * PATCH /api/orders/{id}/status
     */
    @Test
    void shouldUpdateOrderStatus()
            throws Exception {

        long orderId =
                9901001L;


        Customer customer =
                mock(Customer.class);

        Order updatedOrder =
                mock(Order.class);


        when(customer.getId())
                .thenReturn(8101L);

        when(customer.getName())
                .thenReturn("Rakesh");


        when(updatedOrder.getId())
                .thenReturn(orderId);

        when(updatedOrder.getCustomer())
                .thenReturn(customer);

        when(updatedOrder.getItems())
                .thenReturn(List.of());


        when(
                orderService.updateStatus(
                        eq(orderId),
                        any()
                )
        )
                .thenReturn(
                        updatedOrder
                );


        String requestBody =
                """
                {
                  "status": "PAID"
                }
                """;


        mockMvc.perform(
                        patch(
                                "/api/orders/{id}/status",
                                orderId
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
                                .value(9901001)
                )
                .andExpect(
                        jsonPath("$.customerId")
                                .value(8101)
                )
                .andExpect(
                        jsonPath("$.customerName")
                                .value("Rakesh")
                );


        verify(orderService)
                .updateStatus(
                        eq(orderId),
                        any()
                );
    }


    /*
     * PATCH invalid request -> 400
     */
    @Test
    void shouldReturnBadRequestWhenStatusIsMissing()
            throws Exception {

        long orderId =
                9901001L;


        String requestBody =
                """
                {
                }
                """;


        mockMvc.perform(
                        patch(
                                "/api/orders/{id}/status",
                                orderId
                        )
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
    }


    /*
     * PATCH missing order -> 404
     */
    @Test
    void shouldReturnNotFoundWhenUpdatingMissingOrder()
            throws Exception {

        long orderId =
                9999999L;


        when(
                orderService.updateStatus(
                        eq(orderId),
                        any()
                )
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Order not found"
                        )
                );


        String requestBody =
                """
                {
                  "status": "PAID"
                }
                """;


        mockMvc.perform(
                        patch(
                                "/api/orders/{id}/status",
                                orderId
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


        verify(orderService)
                .updateStatus(
                        eq(orderId),
                        any()
                );
    }
}