package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.SpringDataOrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SpringOrderServiceTest {

    @Mock
    private SpringDataOrderRepository orderRepository;


    @InjectMocks
    private SpringOrderService orderService;


    /*
     * save()
     */
    @Test
    void shouldSaveOrder() {

        Order order =
                mock(Order.class);


        when(
                orderRepository.save(order)
        )
                .thenReturn(order);


        Order result =
                orderService.save(order);


        assertSame(
                order,
                result
        );


        verify(orderRepository)
                .save(order);
    }


    /*
     * save(null)
     */
    @Test
    void shouldThrowExceptionWhenSavingNullOrder() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                orderService.save(null)
                );


        assertEquals(
                "Order cannot be null",
                exception.getMessage()
        );


        verifyNoInteractions(
                orderRepository
        );
    }


    /*
     * findByIdOrThrow()
     */
    @Test
    void shouldReturnOrderWhenOrderExists() {

        long orderId = 9901001L;


        Order order =
                mock(Order.class);


        when(
                orderRepository.findDetailedById(
                        orderId
                )
        )
                .thenReturn(
                        Optional.of(order)
                );


        Order result =
                orderService.findByIdOrThrow(
                        orderId
                );


        assertSame(
                order,
                result
        );


        verify(orderRepository)
                .findDetailedById(
                        orderId
                );
    }


    /*
     * findByIdOrThrow() missing order
     */
    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        long orderId = 9999999L;


        when(
                orderRepository.findDetailedById(
                        orderId
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                orderService
                                        .findByIdOrThrow(
                                                orderId
                                        )
                );


        assertEquals(
                "Order not found: 9999999",
                exception.getMessage()
        );


        verify(orderRepository)
                .findDetailedById(
                        orderId
                );
    }


    /*
     * findAll()
     */
    @Test
    void shouldReturnAllOrders() {

        Order order1 =
                mock(Order.class);

        Order order2 =
                mock(Order.class);


        List<Order> orders =
                List.of(
                        order1,
                        order2
                );


        when(
                orderRepository.findAllDetailed()
        )
                .thenReturn(orders);


        List<Order> result =
                orderService.findAll();


        assertEquals(
                2,
                result.size()
        );


        assertSame(
                order1,
                result.get(0)
        );


        assertSame(
                order2,
                result.get(1)
        );


        verify(orderRepository)
                .findAllDetailed();
    }


    /*
     * updateStatus()
     */
    @Test
    void shouldUpdateOrderStatus() {

        long orderId = 9901001L;


        Order order =
                mock(Order.class);


        /*
         * We use an actual enum value from your OrderStatus
         * without assuming a specific enum constant.
         */
        OrderStatus newStatus =
                OrderStatus.values()[0];


        when(
                orderRepository.findById(
                        orderId
                )
        )
                .thenReturn(
                        Optional.of(order)
                );


        Order result =
                orderService.updateStatus(
                        orderId,
                        newStatus
                );


        assertSame(
                order,
                result
        );


        verify(orderRepository)
                .findById(
                        orderId
                );


        verify(order)
                .updateStatus(
                        newStatus
                );
    }


    /*
     * updateStatus(null)
     */
    @Test
    void shouldThrowExceptionWhenStatusIsNull() {

        long orderId = 9901001L;


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                orderService.updateStatus(
                                        orderId,
                                        null
                                )
                );


        assertEquals(
                "Order status cannot be null",
                exception.getMessage()
        );


        verifyNoInteractions(
                orderRepository
        );
    }


    /*
     * updateStatus() missing order
     */
    @Test
    void shouldThrowExceptionWhenUpdatingStatusOfMissingOrder() {

        long orderId = 9999999L;


        OrderStatus newStatus =
                OrderStatus.values()[0];


        when(
                orderRepository.findById(
                        orderId
                )
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                orderService.updateStatus(
                                        orderId,
                                        newStatus
                                )
                );


        assertEquals(
                "Order not found: 9999999",
                exception.getMessage()
        );


        verify(orderRepository)
                .findById(
                        orderId
                );
    }


    /*
     * deleteById()
     *
     * Existing order should be deleted
     * and method should return true.
     */
    @Test
    void shouldDeleteExistingOrder() {

        long orderId = 9901001L;


        when(
                orderRepository.existsById(
                        orderId
                )
        )
                .thenReturn(true);


        boolean result =
                orderService.deleteById(
                        orderId
                );


        assertTrue(result);


        verify(orderRepository)
                .existsById(
                        orderId
                );


        verify(orderRepository)
                .deleteById(
                        orderId
                );
    }


    /*
     * deleteById()
     *
     * Missing order should return false
     * and deleteById() must not be called.
     */
    @Test
    void shouldReturnFalseWhenDeletingMissingOrder() {

        long orderId = 9999999L;


        when(
                orderRepository.existsById(
                        orderId
                )
        )
                .thenReturn(false);


        boolean result =
                orderService.deleteById(
                        orderId
                );


        assertFalse(result);


        verify(orderRepository)
                .existsById(
                        orderId
                );


        verify(
                orderRepository,
                never()
        )
                .deleteById(
                        orderId
                );
    }
}