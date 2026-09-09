package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.Product;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class JdbcProductRepositoryTest {

    @Test
    void shouldSaveProduct() throws Exception {

        ProductRepository repository =
                new JdbcProductRepository();

        Product product =
                new Product(
                        9001,
                        "Test iPhone",
                        "Electronics",
                        80000
                );

        repository.save(product);

        String sql =
                "SELECT COUNT(*) " +
                        "FROM products " +
                        "WHERE id = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    product.getId()
            );

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
    void shouldFindProductById() throws Exception {

        ProductRepository repository =
                new JdbcProductRepository();

        Product product =
                new Product(
                        9002,
                        "MacBook Pro",
                        "Electronics",
                        150000
                );

        repository.save(product);

        Optional<Product> result =
                repository.findById(9002);

        assertTrue( result.isPresent());

        Product foundProduct =
                result.get();

        assertEquals(9002, foundProduct.getId());
        assertEquals("MacBook Pro", foundProduct.getName());
        assertEquals("Electronics", foundProduct.getCategory());
        assertEquals(150000, foundProduct.getPrice());
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist()
            throws Exception {

        ProductRepository repository =
                new JdbcProductRepository();

        Optional<Product> result =
                repository.findById(999999);

        assertTrue(result.isEmpty());
    }

    @BeforeEach
    void cleanTestProducts() throws Exception {

        String sql =
                "DELETE FROM products WHERE id IN (9001, 9002, 9003, 9004)";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.executeUpdate();
        }
    }

    @Test
    void shouldFindAllProducts() throws Exception {

        ProductRepository repository =
                new JdbcProductRepository();

        Product product1 =
                new Product(
                        9003,
                        "Keyboard",
                        "Electronics",
                        3000
                );

        Product product2 =
                new Product(
                        9004,
                        "Mouse",
                        "Electronics",
                        1500
                );

        repository.save(product1);
        repository.save(product2);

        List<Product> products =
                repository.findAll();

        assertTrue(
                products.stream()
                        .anyMatch(
                                product ->
                                        product.getId() == 9003
                        )
        );

        assertTrue(
                products.stream()
                        .anyMatch(
                                product ->
                                        product.getId() == 9004
                        )
        );
    }

    @Test
    void shouldUpdateProduct() throws Exception {

        ProductRepository repository =
                new JdbcProductRepository();

        Product product =
                new Product(
                        9003,
                        "Keyboard",
                        "Electronics",
                        3000
                );

        repository.save(product);

        Product updatedProduct =
                new Product(
                        9003,
                        "Mechanical Keyboard",
                        "Accessories",
                        5000
                );

        repository.update(updatedProduct);

        Optional<Product> result =
                repository.findById(9003);

        assertTrue(result.isPresent());

        Product foundProduct =
                result.get();

        assertEquals(
                "Mechanical Keyboard",
                foundProduct.getName()
        );

        assertEquals(
                "Accessories",
                foundProduct.getCategory()
        );

        assertEquals(
                5000,
                foundProduct.getPrice()
        );
    }

    @Test
    void shouldDeleteProductById() throws Exception {

        ProductRepository repository =
                new JdbcProductRepository();

        Product product =
                new Product(
                        9004,
                        "Mouse",
                        "Electronics",
                        1500
                );

        repository.save(product);

        boolean deleted =
                repository.deleteById(9004);

        assertTrue(deleted);

        Optional<Product> result =
                repository.findById(9004);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingProduct()
            throws Exception {

        ProductRepository repository =
                new JdbcProductRepository();

        boolean deleted =
                repository.deleteById(999999);

        assertFalse(deleted);
    }



}