package com.ecommerce.analytics;

import com.ecommerce.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsObserverTest {

    @Test
    void shouldStoreReceivedOrder() {

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

        Order order =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.CARD
                );

        order.updateStatus(OrderStatus.PAID);

        AnalyticsObserver observer =
                new AnalyticsObserver();

        observer.update(order);

        assertAll(
                () -> assertEquals(
                        1,
                        observer.getOrders().size()
                ),

                () -> assertSame(
                        order,
                        observer.getOrders().get(0)
                )
        );
    }

    @Test
    void shouldRejectNullOrder() {

        AnalyticsObserver observer =
                new AnalyticsObserver();

        assertThrows(
                IllegalArgumentException.class,
                () -> observer.update(null)
        );
    }
}