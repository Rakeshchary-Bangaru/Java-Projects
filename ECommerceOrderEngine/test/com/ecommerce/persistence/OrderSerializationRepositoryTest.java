package com.ecommerce.persistence;

import com.ecommerce.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderSerializationRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSerializeAndDeserializeOrder()
            throws Exception {

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
                        1001,
                        customer,
                        List.of(
                                new OrderItem(product, 2)
                        ),
                        PaymentType.CARD
                );

        order.updateStatus(OrderStatus.PAID);

        Path file =
                tempDir.resolve("order.dat");

        OrderSerializationRepository repository =
                new OrderSerializationRepository();

        repository.saveOrder(file, order);

        Order restoredOrder =
                repository.readOrder(file);

        assertAll(
                () -> assertEquals(
                        1001,
                        restoredOrder.getId()
                ),

                () -> assertEquals(
                        OrderStatus.PAID,
                        restoredOrder.getStatus()
                ),

                () -> assertEquals(
                        PaymentType.CARD,
                        restoredOrder.getPaymentType()
                ),

                () -> assertEquals(
                        20000,
                        restoredOrder.calculateTotal()
                ),

                () -> assertEquals(
                        "Rakesh",
                        restoredOrder.getCustomer().getName()
                )
        );
    }
    @Test
    void shouldRejectNullPathWhenSaving() {

        OrderSerializationRepository repository =
                new OrderSerializationRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.saveOrder(null, createOrder())
        );
    }
    @Test
    void shouldRejectNullOrderWhenSaving() {

        OrderSerializationRepository repository =
                new OrderSerializationRepository();

        Path file =
                tempDir.resolve("order.dat");

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.saveOrder(file, null)
        );
    }
    @Test
    void shouldRejectNullPathWhenReading() {

        OrderSerializationRepository repository =
                new OrderSerializationRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.readOrder(null)
        );
    }

    private Order createOrder() {

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
                        1001,
                        customer,
                        List.of(
                                new OrderItem(product, 2)
                        ),
                        PaymentType.CARD
                );

        order.updateStatus(OrderStatus.PAID);

        return order;
    }

    @Test
    void shouldSerializeAndDeserializeMultipleOrders()
            throws Exception {

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

        Order order1 =
                new Order(
                        1001,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        1002,
                        customer,
                        List.of(
                                new OrderItem(product, 2)
                        ),
                        PaymentType.UPI
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);

        Path file =
                tempDir.resolve("orders.dat");

        OrderSerializationRepository repository =
                new OrderSerializationRepository();

        repository.saveOrders(
                file,
                List.of(order1, order2)
        );

        List<Order> restoredOrders =
                repository.readOrders(file);

        assertAll(
                () -> assertEquals(
                        2,
                        restoredOrders.size()
                ),

                () -> assertEquals(
                        1001,
                        restoredOrders.get(0).getId()
                ),

                () -> assertEquals(
                        1002,
                        restoredOrders.get(1).getId()
                ),

                () -> assertEquals(
                        10000,
                        restoredOrders.get(0).calculateTotal()
                ),

                () -> assertEquals(
                        20000,
                        restoredOrders.get(1).calculateTotal()
                )
        );
    }
}