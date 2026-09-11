package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;
import com.ecommerce.payment.PaymentResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcPaymentRepositoryTest {

    @BeforeEach
    void cleanTestData() throws Exception {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            // Deleting the order also deletes its payments
            // and order_items because of ON DELETE CASCADE.
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM orders WHERE id = ?"
                         )) {

                statement.setLong(1, 6001);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM inventory WHERE product_id = ?"
                         )) {

                statement.setLong(1, 7101);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM customers WHERE id = ?"
                         )) {

                statement.setLong(1, 8101);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM products WHERE id = ?"
                         )) {

                statement.setLong(1, 7101);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void shouldSavePaymentResult() throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        PaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        Customer customer = new Customer(
                8101,
                "Rakesh",
                "rakesh.payment@example.com"
        );

        Product product = new Product(
                7101,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(product);

        Order order =
                new Order.Builder()
                        .id(6001)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(product, 1)
                                )
                        )
                        .paymentType(PaymentType.CARD)
                        .build();

        orderRepository.save(order);

        PaymentResult paymentResult =
                new PaymentResult(
                        true,
                        PaymentType.CARD,
                        75000,
                        "Payment successful"
                );

        paymentRepository.save(
                6001,
                paymentResult
        );

        String sql = """
                SELECT COUNT(*)
                FROM payments
                WHERE order_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, 6001);

            try (var resultSet =
                         statement.executeQuery()) {

                resultSet.next();

                assertEquals(
                        1,
                        resultSet.getInt(1)
                );
            }
        }
    }

    @Test
    void shouldFindPaymentsByOrderId() throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        PaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        Customer customer = new Customer(
                8101,
                "Rakesh",
                "rakesh.payment@example.com"
        );

        Product product = new Product(
                7101,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(product);

        Order order =
                new Order.Builder()
                        .id(6001)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(product, 1)
                                )
                        )
                        .paymentType(PaymentType.CARD)
                        .build();

        orderRepository.save(order);

        PaymentResult failed =
                new PaymentResult(
                        false,
                        PaymentType.CARD,
                        75000,
                        "Payment failed"
                );

        PaymentResult successful =
                new PaymentResult(
                        true,
                        PaymentType.CARD,
                        75000,
                        "Payment successful"
                );

        paymentRepository.save(
                6001,
                failed
        );

        paymentRepository.save(
                6001,
                successful
        );

        List<PaymentResult> payments =
                paymentRepository.findByOrderId(6001);

        assertEquals(
                2,
                payments.size()
        );

        assertFalse(
                payments.get(0).isSuccessful()
        );

        assertTrue(
                payments.get(1).isSuccessful()
        );
    }

    @Test
    void shouldFindLatestPaymentByOrderId() throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        PaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        Customer customer = new Customer(
                8101,
                "Rakesh",
                "rakesh.payment@example.com"
        );

        Product product = new Product(
                7101,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(product);

        Order order =
                new Order.Builder()
                        .id(6001)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(product, 1)
                                )
                        )
                        .paymentType(PaymentType.CARD)
                        .build();

        orderRepository.save(order);

        paymentRepository.save(
                6001,
                new PaymentResult(
                        false,
                        PaymentType.CARD,
                        75000,
                        "First attempt failed"
                )
        );

        paymentRepository.save(
                6001,
                new PaymentResult(
                        true,
                        PaymentType.CARD,
                        75000,
                        "Second attempt successful"
                )
        );

        Optional<PaymentResult> result =
                paymentRepository.findLatestByOrderId(6001);

        assertTrue(result.isPresent());

        PaymentResult latest =
                result.get();

        assertTrue(
                latest.isSuccessful()
        );

        assertEquals(
                "Second attempt successful",
                latest.getMessage()
        );
    }

    @Test
    void shouldReturnEmptyWhenNoPaymentExists()
            throws Exception {

        PaymentRepository paymentRepository =
                new JdbcPaymentRepository();

        Optional<PaymentResult> result =
                paymentRepository.findLatestByOrderId(999999);

        assertTrue(result.isEmpty());
    }
}