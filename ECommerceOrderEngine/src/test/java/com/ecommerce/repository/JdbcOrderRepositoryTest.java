package com.ecommerce.repository;

import com.ecommerce.model.*;
import com.ecommerce.database.DatabaseConnection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JdbcOrderRepositoryTest{
    @BeforeEach
    void cleanTestData() throws Exception{
        try(Connection connection = DatabaseConnection.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM orders WHERE id IN (?, ?)")){
                statement.setLong(1,5001);
                statement.setLong(2, 5002);
                statement.executeUpdate();
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "DELETE FROM inventory WHERE product_id IN (?, ?)"
                         )) {

                statement.setLong(1, 7001);
                statement.setLong(2, 7002);
                statement.executeUpdate();
            }

            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM customers WHERE id = ?")){
                statement.setLong(1,8001);
                statement.executeUpdate();
            }

            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id IN ( ?, ?)")){
                statement.setLong(1,7001);
                statement.setLong(2,7002);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void shouldSaveOrderAndOrderItems() throws Exception{
        CustomerRepository customerRepository = new JdbcCustomerRepository();
        ProductRepository productRepository = new JdbcProductRepository();
        OrderRepository orderRepository = new JdbcOrderRepository();

        Customer customer = new Customer(8001,"Rakesh","rakesh.order@example.com");

        Product laptop = new Product(7001,"Laptop","Electronics",75000);

        Product mouse = new Product(7002,"Mouse","Electronics",1500);

        customerRepository.save(customer);
        productRepository.save(laptop);
        productRepository.save(mouse);

        OrderItem item1   = new OrderItem(laptop,1);
        OrderItem item2 = new OrderItem(mouse,2);

        Order order = new Order.Builder()
                .id(5001)
                .customer(customer)
                .items(List.of(item1,item2))
                .paymentType(PaymentType.CARD)
                .build();
        orderRepository.save(order);

        try(Connection connection = DatabaseConnection.getConnection()){
            String orderSql = "SELECT COUNT(*) FROM orders WHERE id = ?";

            try(PreparedStatement statement = connection.prepareStatement(orderSql)){
                statement.setLong(1,5001);
                try(var resultSet = statement.executeQuery()){
                    resultSet.next();
                    assertEquals(1,resultSet.getInt(1));
                }
            }

            String itemSql = "SELECT COUNT(*) FROM order_items WHERE order_id = ?";

            try(PreparedStatement statement = connection.prepareStatement(itemSql)){
                statement.setLong(1,5001);

                try(var resultSet = statement.executeQuery()){
                    resultSet.next();

                    assertEquals(2,resultSet.getInt(1));
                }
            }
        }
    }

    @Test
    void shouldRollbackOrderWhenOrderItemInsertFails()
            throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        Customer customer = new Customer(
                8001,
                "Rakesh",
                "rakesh.order@example.com"
        );

        Product validProduct = new Product(
                7001,
                "Laptop",
                "Electronics",
                75000
        );

        Product missingProduct = new Product(
                7999,
                "Missing Product",
                "Electronics",
                1000
        );

        customerRepository.save(customer);

        // Save only the valid product.
        productRepository.save(validProduct);

        OrderItem validItem =
                new OrderItem(
                        validProduct,
                        1
                );

        OrderItem invalidItem =
                new OrderItem(
                        missingProduct,
                        1
                );

        Order order =
                new Order.Builder()
                        .id(5002)
                        .customer(customer)
                        .items(
                                List.of(
                                        validItem,
                                        invalidItem
                                )
                        )
                        .paymentType(
                                PaymentType.CARD
                        )
                        .build();

        assertThrows(
                SQLException.class,
                () -> orderRepository.save(order)
        );

        String sql = """
            SELECT COUNT(*)
            FROM orders
            WHERE id = ?
            """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, 5002);

            try (var resultSet =
                         statement.executeQuery()) {

                resultSet.next();

                assertEquals(
                        0,
                        resultSet.getInt(1)
                );
            }
        }
    }

    @Test
    void shouldFindOrderById() throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        Customer customer = new Customer(
                8001,
                "Rakesh",
                "rakesh.order@example.com"
        );

        Product laptop = new Product(
                7001,
                "Laptop",
                "Electronics",
                75000
        );

        Product mouse = new Product(
                7002,
                "Mouse",
                "Electronics",
                1500
        );

        customerRepository.save(customer);
        productRepository.save(laptop);
        productRepository.save(mouse);

        Order order =
                new Order.Builder()
                        .id(5001)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(laptop, 1),
                                        new OrderItem(mouse, 2)
                                )
                        )
                        .paymentType(PaymentType.CARD)
                        .build();

        orderRepository.save(order);

        Optional<Order> result =
                orderRepository.findById(5001);

        assertTrue(result.isPresent());

        Order foundOrder =
                result.get();

        assertEquals(
                5001,
                foundOrder.getId()
        );

        assertEquals(
                8001,
                foundOrder.getCustomer().getId()
        );

        assertEquals(
                "Rakesh",
                foundOrder.getCustomer().getName()
        );

        assertEquals(
                PaymentType.CARD,
                foundOrder.getPaymentType()
        );

        assertEquals(
                OrderStatus.CREATED,
                foundOrder.getStatus()
        );

        assertEquals(
                2,
                foundOrder.getItems().size()
        );
    }

    @Test
    void shouldReturnEmptyWhenOrderDoesNotExist()
            throws Exception {

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        Optional<Order> result =
                orderRepository.findById(999999);

        assertTrue(
                result.isEmpty()
        );
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        Customer customer = new Customer(
                8001,
                "Rakesh",
                "rakesh.order@example.com"
        );

        Product product = new Product(
                7001,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(product);

        Order order =
                new Order.Builder()
                        .id(5001)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(product, 1)
                                )
                        )
                        .paymentType(PaymentType.CARD)
                        .build();

        orderRepository.save(order);

        orderRepository.updateStatus(
                5001,
                OrderStatus.SHIPPED
        );

        Optional<Order> result =
                orderRepository.findById(5001);

        assertTrue(result.isPresent());

        assertEquals(
                OrderStatus.SHIPPED,
                result.get().getStatus()
        );
    }

    @Test
    void shouldDeleteOrderAndItsItems() throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        Customer customer = new Customer(
                8001,
                "Rakesh",
                "rakesh.order@example.com"
        );

        Product product = new Product(
                7001,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(product);

        Order order =
                new Order.Builder()
                        .id(5001)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(product, 1)
                                )
                        )
                        .paymentType(PaymentType.CARD)
                        .build();

        orderRepository.save(order);

        boolean deleted =
                orderRepository.deleteById(5001);

        assertTrue(deleted);

        Optional<Order> result =
                orderRepository.findById(5001);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingOrder()
            throws Exception {

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        boolean deleted =
                orderRepository.deleteById(999999);

        assertFalse(deleted);
    }

    @Test
    void shouldFindAllOrders() throws Exception {

        CustomerRepository customerRepository =
                new JdbcCustomerRepository();

        ProductRepository productRepository =
                new JdbcProductRepository();

        OrderRepository orderRepository =
                new JdbcOrderRepository();

        Customer customer = new Customer(
                8001,
                "Rakesh",
                "rakesh.order@example.com"
        );

        Product product = new Product(
                7001,
                "Laptop",
                "Electronics",
                75000
        );

        customerRepository.save(customer);
        productRepository.save(product);

        Order order1 =
                new Order.Builder()
                        .id(5001)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(product, 1)
                                )
                        )
                        .paymentType(PaymentType.CARD)
                        .build();

        Order order2 =
                new Order.Builder()
                        .id(5002)
                        .customer(customer)
                        .items(
                                List.of(
                                        new OrderItem(product, 2)
                                )
                        )
                        .paymentType(PaymentType.UPI)
                        .build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        List<Order> orders =
                orderRepository.findAll();

        assertTrue(
                orders.stream()
                        .anyMatch(
                                order -> order.getId() == 5001
                        )
        );

        assertTrue(
                orders.stream()
                        .anyMatch(
                                order -> order.getId() == 5002
                        )
        );
    }
}