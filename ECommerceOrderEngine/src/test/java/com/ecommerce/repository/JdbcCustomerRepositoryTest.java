package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.Customer;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class JdbcCustomerRepositoryTest {

    @BeforeEach
    void cleanTestCustomers() throws Exception{
        String sql = "DELETE FROM customers WHERE id IN (8001, 8002, 8003, 8004)";

        try(Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.executeUpdate();
        }
    }

    @Test
    void shouldSaveCustomer() throws Exception {

        CustomerRepository repository =
                new JdbcCustomerRepository();

        Customer customer =
                new Customer(
                        8001,
                        "Rakesh",
                        "rakesh.test@example.com"

                );

        repository.save(customer);

        String sql =
                "SELECT COUNT(*) " +
                        "FROM customers " +
                        "WHERE id = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    customer.getId()
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
    void shouldFindCustomerById() throws Exception {
        CustomerRepository repository = new JdbcCustomerRepository();

        Customer customer = new Customer(
                8002,
                "Alice",
                "alice.test@example.com"
        );

        repository.save(customer);
        Optional<Customer> result = repository.findById(8002);
        assertTrue(result.isPresent());

        Customer foundCustomer = result.get();

        assertEquals(8002,foundCustomer.getId());
        assertEquals("Alice",foundCustomer.getName());
        assertEquals("alice.test@example.com",foundCustomer.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenCustomerDoesNotExist() throws  Exception{
        CustomerRepository repository = new JdbcCustomerRepository();

        Optional<Customer> result = repository.findById(999999);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindAllCustomers() throws Exception{
        CustomerRepository repository = new JdbcCustomerRepository();
        Customer customer1 = new Customer(
                8003,
                "Bob",
                "bob.test@example.com"
        );

        Customer customer2 = new Customer(
                8004,
                "Charlie",
                "charlie.test@example.com"
        );

        repository.save(customer1);
        repository.save(customer2);

        List<Customer> customers = repository.findAll();

        assertTrue(customers.stream()
                .anyMatch(customer -> customer.getId() == 8003));
        assertTrue(customers.stream()
                .anyMatch(customer -> customer.getId()==8004));
    }

    @Test
    void shouldUpdateCustomer() throws Exception{
        CustomerRepository repository = new JdbcCustomerRepository();
        Customer customer = new Customer(
                8002,
                "Alice",
                "alice.test@example.com"
        );

        repository.save(customer);

        customer.updateEmail("alice.updated@example.com");

        repository.update(customer);

        Optional<Customer> result = repository.findById(8002);
        assertTrue(result.isPresent());
        Customer foundCustomer = result.get();
        assertEquals("alice.updated@example.com" , foundCustomer.getEmail());
        assertEquals("Alice" , foundCustomer.getName());
    }

    @Test
    void shouldDeleteCustomerById() throws Exception{
        CustomerRepository repository = new JdbcCustomerRepository();
        Customer customer = new Customer(
                8004,
                "Charlie",
                "charlie.test@example.com"
        );
        repository.save(customer);
        boolean deleted = repository.deleteById(8004);

        assertTrue(deleted);

        Optional<Customer> result = repository.findById(8004);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingMissingCustomer()
            throws Exception {

        CustomerRepository repository =
                new JdbcCustomerRepository();

        boolean deleted =
                repository.deleteById(999999);

        assertFalse(deleted);
    }


}