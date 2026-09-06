package com.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Customer customer;
    private Product iphone;
    private Product laptop;
    private List<OrderItem> items;

    @BeforeEach
    void setUp() {

        customer = new Customer(
                1,
                "Rakesh",
                "rakesh@gmail.com"
        );

        iphone = new Product(
                1,
                "iPhone",
                "Electronics",
                80000
        );

        laptop = new Product(
                2,
                "MacBook",
                "Electronics",
                120000
        );

        items = new ArrayList<>();

        items.add(new OrderItem(iphone, 2));
        items.add(new OrderItem(laptop, 1));
    }

    @Test
    void shouldCreateValidOrder() {

        Order order = new Order(
                1,
                customer,
                items,
                PaymentType.CARD
        );

        assertAll(
                () -> assertEquals(1, order.getId()),
                () -> assertSame(customer, order.getCustomer()),
                () -> assertEquals(2, order.getItems().size()),
                () -> assertEquals(
                        PaymentType.CARD,
                        order.getPaymentType()
                ),
                () -> assertEquals(
                        OrderStatus.CREATED,
                        order.getStatus()
                ),
                () -> assertNotNull(order.getCreatedAt())
        );
    }

    @Test
    void shouldCalculateOrderTotal() {

        Order order = new Order(
                1,
                customer,
                items,
                PaymentType.CARD
        );

        // iPhone: 80000 × 2 = 160000
        // MacBook: 120000 × 1 = 120000
        // Total = 280000

        assertEquals(
                280000,
                order.calculateTotal()
        );
    }

    @Test
    void shouldUpdateOrderStatus() {

        Order order = new Order(
                1,
                customer,
                items,
                PaymentType.UPI
        );

        order.updateStatus(OrderStatus.PAID);

        assertEquals(
                OrderStatus.PAID,
                order.getStatus()
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -10})
    void shouldRejectInvalidOrderId(long invalidId) {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(
                        invalidId,
                        customer,
                        items,
                        PaymentType.CARD
                )
        );
    }

    @Test
    void shouldRejectNullCustomer() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(
                        1,
                        null,
                        items,
                        PaymentType.CARD
                )
        );
    }

    @Test
    void shouldRejectNullItems() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(
                        1,
                        customer,
                        null,
                        PaymentType.CARD
                )
        );
    }

    @Test
    void shouldRejectEmptyItems() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(
                        1,
                        customer,
                        List.of(),
                        PaymentType.CARD
                )
        );
    }

    @Test
    void shouldRejectNullPaymentType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order(
                        1,
                        customer,
                        items,
                        null
                )
        );
    }

    @Test
    void shouldRejectNullStatusUpdate() {

        Order order = new Order(
                1,
                customer,
                items,
                PaymentType.CARD
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> order.updateStatus(null)
        );
    }

    @Test
    void shouldProtectOrderItemsFromModification() {

        Order order = new Order(
                1,
                customer,
                items,
                PaymentType.CARD
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> order.getItems()
                        .add(new OrderItem(iphone, 1))
        );
    }

    @Test
    void shouldKeepOrderItemsWhenOriginalListChanges() {

        Order order = new Order(
                1,
                customer,
                items,
                PaymentType.CARD
        );

        items.clear();

        assertEquals(
                2,
                order.getItems().size()
        );
    }

    @Test
    void shouldBuildValidOrder() {

        Product product =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        10000
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        List<OrderItem> items =
                List.of(
                        new OrderItem(product, 2)
                );

        Order order =
                new Order.Builder()
                        .id(1001)
                        .customer(customer)
                        .items(items)
                        .paymentType(PaymentType.CARD)
                        .build();

        assertAll(
                () -> assertEquals(1001, order.getId()),
                () -> assertSame(customer, order.getCustomer()),
                () -> assertEquals(1, order.getItems().size()),
                () -> assertEquals(
                        PaymentType.CARD,
                        order.getPaymentType()
                ),
                () -> assertEquals(
                        OrderStatus.CREATED,
                        order.getStatus()
                ),
                () -> assertEquals(
                        20000,
                        order.calculateTotal()
                )
        );
    }

    @Test
    void shouldRejectBuilderWithoutCustomer() {

        Product product =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        10000
                );

        List<OrderItem> items =
                List.of(
                        new OrderItem(product, 1)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order.Builder()
                        .id(1001)
                        .items(items)
                        .paymentType(PaymentType.CARD)
                        .build()
        );
    }
}