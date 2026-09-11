package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.Product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class JdbcInventoryRepositoryTest{
    @BeforeEach
    void cleanTestData() throws Exception{
        try(Connection connection = DatabaseConnection.getConnection()){
            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM inventory WHERE product_id = ?")){
                statement.setLong(1,7001);
                statement.executeUpdate();
            }

            try(PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id = ?")){
                statement.setLong(1,7001);
                statement.executeUpdate();
            }
        }
    }

    @Test
    void shouldAddStock() throws Exception{
        ProductRepository productRepository = new JdbcProductRepository();
        InventoryRepository inventoryRepository = new JdbcInventoryRepository();

        Product product = new Product(
                7001,
                "Test Laptop",
                "Electronics",
                75000
        );

        productRepository.save(product);

        inventoryRepository.addStock(7001,20);

        String sql = "SELECT quantity FROM inventory WHERE product_id = ?";

        try(Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setLong(1,7001);

            try(var resultSet = statement.executeQuery()){
                resultSet.next();
                assertEquals(20,resultSet.getInt("quantity"));
            }
        }
    }

    @Test
    void shouldIncreaseExistingStock() throws Exception {

        ProductRepository productRepository =
                new JdbcProductRepository();

        InventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        Product product = new Product(
                7001,
                "Test Laptop",
                "Electronics",
                75000
        );

        productRepository.save(product);

        inventoryRepository.addStock(7001, 20);
        inventoryRepository.addStock(7001, 5);

        String sql = """
            SELECT quantity
            FROM inventory
            WHERE product_id = ?
            """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, 7001);

            try (var resultSet =
                         statement.executeQuery()) {

                resultSet.next();

                assertEquals(
                        25,
                        resultSet.getInt("quantity")
                );
            }
        }
    }

    @Test
    void shouldGetStock() throws Exception
    {
        ProductRepository productRepository = new JdbcProductRepository();
        InventoryRepository inventoryRepository = new JdbcInventoryRepository();

        Product product = new Product(7001,"Test Laptop","Electronics",75000);

        productRepository.save(product);
        inventoryRepository.addStock(7001,30);
        int stock = inventoryRepository.getStock(7001);

        assertEquals(30,stock);
    }

    @Test
    void shouldReturnZeroWhenStockDoesNotExist() throws Exception{
        InventoryRepository inventoryRepository = new JdbcInventoryRepository();
        int stock = inventoryRepository.getStock(999999);

        assertEquals(0,stock);
    }

    @Test
    void shouldReserveStockWhenEnoughStockExists() throws Exception{
        ProductRepository productRepository = new JdbcProductRepository();
        InventoryRepository  inventoryRepository = new JdbcInventoryRepository();
        Product product = new Product(7001,"Test Laptop","Electronics",75000);

        productRepository.save(product);
        inventoryRepository.addStock(7001,20);

        boolean reserved = inventoryRepository.reserveStock(7001,5);
        assertTrue(reserved);
        assertEquals(15,inventoryRepository.getStock(7001));
    }

    @Test
    void shouldNotReserveWhenStockIsInsufficient()
            throws Exception {

        ProductRepository productRepository =
                new JdbcProductRepository();

        InventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        Product product = new Product(
                7001,
                "Test Laptop",
                "Electronics",
                75000
        );

        productRepository.save(product);

        inventoryRepository.addStock(7001, 3);

        boolean reserved =
                inventoryRepository.reserveStock(
                        7001,
                        5
                );

        assertFalse(reserved);

        assertEquals(
                3,
                inventoryRepository.getStock(7001)
        );
    }

    @Test
    void shouldReleaseStock() throws Exception{
        ProductRepository productRepository = new JdbcProductRepository();
        InventoryRepository inventoryRepository = new JdbcInventoryRepository();
        Product product = new Product(7001,"Test Laptop","Electronics",75000);

        productRepository.save(product);
        inventoryRepository.addStock(7001,20);
        inventoryRepository.reserveStock(7001,5);

        assertEquals(15,inventoryRepository.getStock(7001));
        inventoryRepository.releaseStock(7001,5);
        assertEquals(20,inventoryRepository.getStock(7001));
    }

    @Test
    void shouldPreventOversellingUnderConcurrency()
            throws Exception {

        ProductRepository productRepository =
                new JdbcProductRepository();

        InventoryRepository inventoryRepository =
                new JdbcInventoryRepository();

        Product product = new Product(
                7001,
                "Test Laptop",
                "Electronics",
                75000
        );

        productRepository.save(product);

        inventoryRepository.addStock(
                7001,
                50
        );

        int numberOfCustomers = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        CountDownLatch startGate =
                new CountDownLatch(1);

        CountDownLatch doneGate =
                new CountDownLatch(numberOfCustomers);

        AtomicInteger successfulReservations =
                new AtomicInteger();

        AtomicInteger failedReservations =
                new AtomicInteger();

        for (int i = 0; i < numberOfCustomers; i++) {

            executor.submit(() -> {

                try {

                    startGate.await();

                    boolean reserved =
                            inventoryRepository.reserveStock(
                                    7001,
                                    1
                            );

                    if (reserved) {
                        successfulReservations.incrementAndGet();
                    } else {
                        failedReservations.incrementAndGet();
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);

                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Allow all customer requests to start together.
        startGate.countDown();

        doneGate.await();

        executor.shutdown();

        assertEquals(
                50,
                successfulReservations.get()
        );

        assertEquals(
                50,
                failedReservations.get()
        );

        assertEquals(
                0,
                inventoryRepository.getStock(7001)
        );
    }
}