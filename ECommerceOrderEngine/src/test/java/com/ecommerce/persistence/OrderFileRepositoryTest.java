package com.ecommerce.persistence;

import com.ecommerce.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderFileRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSaveOrdersToCsvFile() throws Exception {

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
                tempDir.resolve("orders.csv");

        OrderFileRepository repository =
                new OrderFileRepository();

        repository.saveOrders(
                file,
                List.of(order)
        );

        List<String> lines =
                Files.readAllLines(file);

        assertAll(
                () -> assertTrue(
                        Files.exists(file)
                ),

                () -> assertEquals(
                        2,
                        lines.size()
                ),

                () -> assertEquals(
                        "orderId,status,paymentType,total",
                        lines.get(0)
                ),

                () -> assertEquals(
                        "1001,PAID,CARD,20000.0",
                        lines.get(1)
                )
        );
    }

    @Test
    void shouldReadOrdersFromCsvFile() throws Exception {

        Path file =
                tempDir.resolve("orders.csv");

        Files.write(
                file,
                List.of(
                        "orderId,status,paymentType,total",
                        "1001,PAID,CARD,20000.0",
                        "1002,PAID,UPI,30000.0"
                )
        );

        OrderFileRepository repository =
                new OrderFileRepository();

        List<OrderSummary> orders =
                repository.readOrders(file);

        assertAll(
                () -> assertEquals(2, orders.size()),

                () -> assertEquals(
                        1001,
                        orders.get(0).getOrderId()
                ),

                () -> assertEquals(
                        OrderStatus.PAID,
                        orders.get(0).getStatus()
                ),

                () -> assertEquals(
                        PaymentType.CARD,
                        orders.get(0).getPaymentType()
                ),

                () -> assertEquals(
                        20000.0,
                        orders.get(0).getTotal()
                )
        );
    }

    @Test
    void shouldAppendOrderWithoutOverwritingExistingOrders()
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
                tempDir.resolve("orders.csv");

        OrderFileRepository repository =
                new OrderFileRepository();

        repository.appendOrder(file, order1);
        repository.appendOrder(file, order2);

        List<String> lines =
                Files.readAllLines(file);

        assertAll(
                () -> assertEquals(3, lines.size()),

                () -> assertEquals(
                        "orderId,status,paymentType,total",
                        lines.get(0)
                ),

                () -> assertEquals(
                        "1001,PAID,CARD,10000.0",
                        lines.get(1)
                ),

                () -> assertEquals(
                        "1002,PAID,UPI,20000.0",
                        lines.get(2)
                )
        );
    }
    @Test
    void shouldRejectNullFilePathWhenSaving() {

        OrderFileRepository repository =
                new OrderFileRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.saveOrders(
                        null,
                        List.of()
                )
        );
    }
    @Test
    void shouldRejectNullOrdersWhenSaving() {

        OrderFileRepository repository =
                new OrderFileRepository();

        Path file =
                tempDir.resolve("orders.csv");

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.saveOrders(
                        file,
                        null
                )
        );
    }

    @Test
    void shouldRejectNullOrderWhenAppending() {

        OrderFileRepository repository =
                new OrderFileRepository();

        Path file =
                tempDir.resolve("orders.csv");

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.appendOrder(
                        file,
                        null
                )
        );
    }

    @Test
    void shouldRejectNullFilePathWhenReading() {

        OrderFileRepository repository =
                new OrderFileRepository();

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.readOrders(null)
        );
    }
}