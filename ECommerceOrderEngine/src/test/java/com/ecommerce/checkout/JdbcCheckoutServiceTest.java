package com.ecommerce.checkout;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.model.*;
import com.ecommerce.payment.PaymentFactory;
import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentResult;
import com.ecommerce.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class JdbcCheckoutServiceTest {

    @BeforeEach
    void cleanTestData() throws Exception {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            // Payments and order_items are deleted automatically
            // because they use ON DELETE CASCADE.
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM orders WHERE customer_id = ?"
                         )) {

                statement.setLong(1, 8201);
                statement.executeUpdate();
            }

            // Inventory references products, so delete inventory first.
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM inventory WHERE product_id IN (?, ?)"
                         )) {

                statement.setLong(1, 7201);
                statement.setLong(2, 7202);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM customers WHERE id = ?"
                         )) {

                statement.setLong(1, 8201);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM products WHERE id IN (?, ?)"
                         )) {

                statement.setLong(1, 7201);
                statement.setLong(2, 7202);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void shouldCompleteCheckoutTransaction()
            throws Exception {

        JdbcInventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        JdbcOrderRepository orderRepository =
                new JdbcOrderRepository();

        JdbcPaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(
                                PaymentType.CARD
                        )
                );

        JdbcCheckoutService checkoutService =
                new JdbcCheckoutService(
                        inventoryRepository,
                        orderRepository,
                        paymentRepository

                );

        Customer customer = new Customer(
                8201,
                "Rakesh",
                "rakesh.checkout@example.com"
        );

        Product laptop = new Product(
                7201,
                "Laptop",
                "Electronics",
                75000
        );

        // Parent rows must exist before checkout.
        customerRepository.save(customer);
        productRepository.save(laptop);

        inventoryRepository.addStock(
                7201,
                10
        );

        Cart cart = new Cart();

        cart.addProduct(
                laptop,
                2
        );

        Order order =
                checkoutService.checkout(
                        6501,
                        customer,
                        cart,
                        PaymentType.CARD
                );

        assertEquals(
                OrderStatus.PAID,
                order.getStatus()
        );

        assertEquals(
                8,
                inventoryRepository.getStock(7201)
        );

        Optional<Order> savedOrder =
                orderRepository.findById(6501);

        assertTrue(savedOrder.isPresent());

        assertEquals(
                OrderStatus.PAID,
                savedOrder.get().getStatus()
        );

        assertEquals(
                2,
                savedOrder.get()
                        .getItems()
                        .getFirst()
                        .getQuantity()
        );

        assertTrue(
                paymentRepository
                        .findLatestByOrderId(6501)
                        .isPresent()
        );

        assertTrue(
                paymentRepository
                        .findLatestByOrderId(6501)
                        .get()
                        .isSuccessful()
        );
    }

    @Test
    void shouldRollbackCheckoutWhenPaymentFails()
            throws Exception {

        JdbcInventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        JdbcOrderRepository orderRepository =
                new JdbcOrderRepository();

        JdbcPaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        // Simulates a payment gateway failure.
        PaymentProcessor failingPaymentProcessor =
                new PaymentProcessor(
                        amount ->
                                new PaymentResult(
                                        false,
                                        PaymentType.CARD,
                                        amount,
                                        "Payment declined"
                                )
                );

        JdbcCheckoutService checkoutService =
                new JdbcCheckoutService(
                        inventoryRepository,
                        orderRepository,
                        paymentRepository,
                        paymentType ->
                                new PaymentProcessor(
                                        amount ->
                                                new PaymentResult(
                                                        false,
                                                        paymentType,
                                                        amount,
                                                        "Payment declined"
                                                )
                                )

                );

        Customer customer = new Customer(
                8201,
                "Rakesh",
                "rakesh.checkout@example.com"
        );

        Product laptop = new Product(
                7201,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(laptop);

        inventoryRepository.addStock(
                7201,
                10
        );

        Cart cart = new Cart();

        cart.addProduct(
                laptop,
                2
        );

        assertThrows(
                PaymentFailedException.class,
                () ->
                        checkoutService.checkout(
                                6501,
                                customer,
                                cart,
                                PaymentType.CARD
                        )
        );

        // Stock was temporarily 10 → 8,
        // but rollback should restore it to 10.
        assertEquals(
                10,
                inventoryRepository.getStock(7201)
        );

        // Order must not remain in the database.
        assertTrue(
                orderRepository
                        .findById(6501)
                        .isEmpty()
        );

        // No successful checkout/payment record should remain.
        assertTrue(
                paymentRepository
                        .findLatestByOrderId(6501)
                        .isEmpty()
        );
    }

    @Test
    void shouldRollbackCheckoutWhenStockIsInsufficient()
            throws Exception {

        JdbcInventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        JdbcOrderRepository orderRepository =
                new JdbcOrderRepository();

        JdbcPaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(
                                PaymentType.CARD
                        )
                );

        JdbcCheckoutService checkoutService =
                new JdbcCheckoutService(
                        inventoryRepository,
                        orderRepository,
                        paymentRepository

                );

        Customer customer = new Customer(
                8201,
                "Rakesh",
                "rakesh.checkout@example.com"
        );

        Product laptop = new Product(
                7201,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(laptop);

        inventoryRepository.addStock(
                7201,
                1
        );

        Cart cart = new Cart();

        cart.addProduct(
                laptop,
                2
        );

        assertThrows(
                InsufficientStockException.class,
                () ->
                        checkoutService.checkout(
                                6501,
                                customer,
                                cart,
                                PaymentType.CARD
                        )
        );

        assertEquals(
                1,
                inventoryRepository.getStock(7201)
        );

        assertTrue(
                orderRepository
                        .findById(6501)
                        .isEmpty()
        );

        assertTrue(
                paymentRepository
                        .findLatestByOrderId(6501)
                        .isEmpty()
        );
    }

    @Test
    void shouldRollbackPreviouslyReservedItemsWhenLaterItemFails()
            throws Exception {

        JdbcInventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        JdbcOrderRepository orderRepository =
                new JdbcOrderRepository();

        JdbcPaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(
                                PaymentType.CARD
                        )
                );

        JdbcCheckoutService checkoutService =
                new JdbcCheckoutService(
                        inventoryRepository,
                        orderRepository,
                        paymentRepository

                );

        Customer customer = new Customer(
                8201,
                "Rakesh",
                "rakesh.checkout@example.com"
        );

        Product laptop = new Product(
                7201,
                "Laptop",
                "Electronics",
                75000
        );

        Product mouse = new Product(
                7202,
                "Mouse",
                "Electronics",
                1500
        );

        customerRepository.save(customer);

        productRepository.save(laptop);
        productRepository.save(mouse);

        inventoryRepository.addStock(
                7201,
                5
        );

        inventoryRepository.addStock(
                7202,
                1
        );

        Cart cart = new Cart();

        cart.addProduct(
                laptop,
                2
        );

        cart.addProduct(
                mouse,
                2
        );

        assertThrows(
                InsufficientStockException.class,
                () ->
                        checkoutService.checkout(
                                6501,
                                customer,
                                cart,
                                PaymentType.CARD
                        )
        );

        assertEquals(
                5,
                inventoryRepository.getStock(7201)
        );

        assertEquals(
                1,
                inventoryRepository.getStock(7202)
        );

        assertTrue(
                orderRepository
                        .findById(6501)
                        .isEmpty()
        );

        assertTrue(
                paymentRepository
                        .findLatestByOrderId(6501)
                        .isEmpty()
        );
    }

    @Test
    void shouldPreventOversellingDuringConcurrentCheckout()
            throws Exception {

        JdbcInventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        JdbcOrderRepository orderRepository =
                new JdbcOrderRepository();

        JdbcPaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(
                                PaymentType.CARD
                        )
                );

        JdbcCheckoutService checkoutService =
                new JdbcCheckoutService(
                        inventoryRepository,
                        orderRepository,
                        paymentRepository

                );

        Customer customer = new Customer(
                8201,
                "Rakesh",
                "rakesh.checkout@example.com"
        );

        Product laptop = new Product(
                7201,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(laptop);

        inventoryRepository.addStock(
                7201,
                50
        );

        int numberOfCustomers = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        CountDownLatch startGate =
                new CountDownLatch(1);

        CountDownLatch doneGate =
                new CountDownLatch(numberOfCustomers);

        AtomicInteger successfulCheckouts =
                new AtomicInteger();

        AtomicInteger failedCheckouts =
                new AtomicInteger();

        AtomicInteger unexpectedErrors =
                new AtomicInteger();

        for (int i = 0; i < numberOfCustomers; i++) {

            final long orderId = 6600 + i;

            executor.submit(() -> {

                try {

                    Cart cart = new Cart();

                    cart.addProduct(
                            laptop,
                            1
                    );

                    startGate.await();

                    checkoutService.checkout(
                            orderId,
                            customer,
                            cart,
                            PaymentType.CARD
                    );

                    successfulCheckouts.incrementAndGet();

                } catch (InsufficientStockException e) {

                    failedCheckouts.incrementAndGet();

                } catch (Exception e) {

                    unexpectedErrors.incrementAndGet();

                } finally {

                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();

        doneGate.await();

        executor.shutdown();

        assertEquals(
                50,
                successfulCheckouts.get()
        );

        assertEquals(
                50,
                failedCheckouts.get()
        );

        assertEquals(
                0,
                unexpectedErrors.get()
        );

        assertEquals(
                0,
                inventoryRepository.getStock(7201)
        );

        String orderCountSql = """
        SELECT COUNT(*)
        FROM orders
        WHERE customer_id = ?
        """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(orderCountSql)
        ) {

            statement.setLong(1, 8201);

            try (var resultSet =
                         statement.executeQuery()) {

                resultSet.next();

                assertEquals(
                        50,
                        resultSet.getInt(1)
                );
            }
        }

        String paymentCountSql = """
        SELECT COUNT(*)
        FROM payments p
        JOIN orders o
            ON p.order_id = o.id
        WHERE o.customer_id = ?
        """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(paymentCountSql)
        ) {

            statement.setLong(1, 8201);

            try (var resultSet =
                         statement.executeQuery()) {

                resultSet.next();

                assertEquals(
                        50,
                        resultSet.getInt(1)
                );
            }
        }
    }
}