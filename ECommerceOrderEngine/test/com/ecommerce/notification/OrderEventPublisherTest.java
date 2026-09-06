package com.ecommerce.notification;

import com.ecommerce.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class OrderEventPublisherTest {

    @Test
    void shouldNotifyRegisteredObserver() {

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        AtomicBoolean notified =
                new AtomicBoolean(false);

        OrderObserver observer = order ->
                notified.set(true);

        publisher.addObserver(observer);

        Order order = createOrder();

        publisher.notifyObservers(order);

        assertTrue(notified.get());
    }

    @Test
    void shouldNotifyMultipleObservers() {

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        AtomicBoolean firstNotified =
                new AtomicBoolean(false);

        AtomicBoolean secondNotified =
                new AtomicBoolean(false);

        publisher.addObserver(
                order -> firstNotified.set(true)
        );

        publisher.addObserver(
                order -> secondNotified.set(true)
        );

        publisher.notifyObservers(createOrder());

        assertAll(
                () -> assertTrue(firstNotified.get()),
                () -> assertTrue(secondNotified.get())
        );
    }

    @Test
    void shouldRejectNullObserver() {

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        assertThrows(
                IllegalArgumentException.class,
                () -> publisher.addObserver(null)
        );
    }

    @Test
    void shouldRejectNullOrder() {

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        assertThrows(
                IllegalArgumentException.class,
                () -> publisher.notifyObservers(null)
        );
    }

    private Order createOrder() {

        Product product =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        80000
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        OrderItem orderItem =
                new OrderItem(product, 1);

        Order order =
                new Order(
                        1001,
                        customer,
                        List.of(orderItem),
                        PaymentType.CARD
                );

        order.updateStatus(OrderStatus.PAID);

        return order;
    }
}