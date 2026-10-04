package com.ecommerce.repository;

import com.ecommerce.model.Customer;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HibernateCustomerRepositoryTest {

    @Test
    void shouldSaveFindUpdateAndDeleteCustomer()
            throws Exception {

        CustomerRepository repository =
                new HibernateCustomerRepository();

        long customerId = 980101L;

        /*
         * Cleanup in case a previous interrupted
         * test left the customer behind.
         */
        repository.deleteById(customerId);

        Customer customer =
                new Customer(
                        customerId,
                        "Hibernate Test Customer",
                        "hibernate.customer@example.com"
                );

        // SAVE
        repository.save(customer);

        Optional<Customer> saved =
                repository.findById(customerId);

        assertTrue(saved.isPresent());

        assertEquals(
                "Hibernate Test Customer",
                saved.get().getName()
        );

        assertEquals(
                "hibernate.customer@example.com",
                saved.get().getEmail()
        );


        // UPDATE EMAIL
        Customer updatedCustomer =
                new Customer(
                        customerId,
                        "Hibernate Test Customer",
                        "updated.hibernate@example.com"
                );

        repository.update(
                updatedCustomer
        );

        Customer updated =
                repository.findById(customerId)
                        .orElseThrow();

        assertEquals(
                "updated.hibernate@example.com",
                updated.getEmail()
        );


        // DELETE
        repository.deleteById(
                customerId
        );

        assertTrue(
                repository.findById(customerId)
                        .isEmpty()
        );
    }
}