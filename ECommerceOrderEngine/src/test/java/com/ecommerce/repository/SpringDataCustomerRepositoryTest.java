package com.ecommerce.repository;

import com.ecommerce.EcommerceApplication;
import com.ecommerce.model.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommerceApplication.class)
@Transactional
class SpringDataCustomerRepositoryTest {

    @Autowired
    private SpringDataCustomerRepository customerRepository;

    @Test
    void shouldPerformCustomerCrudAndFindByEmail() {

        long customerId = 990002L;

        Customer customer =
                new Customer(
                        customerId,
                        "Spring Data Customer",
                        "springdata.customer@test.com"
                );


        // CREATE
        Customer saved =
                customerRepository.save(customer);

        assertNotNull(saved);
        assertEquals(
                customerId,
                saved.getId()
        );


        // READ BY ID
        Optional<Customer> found =
                customerRepository.findById(
                        customerId
                );

        assertTrue(found.isPresent());

        assertEquals(
                "Spring Data Customer",
                found.get().getName()
        );


        // DERIVED QUERY
        Optional<Customer> foundByEmail =
                customerRepository.findByEmail(
                        "springdata.customer@test.com"
                );

        assertTrue(foundByEmail.isPresent());

        assertEquals(
                customerId,
                foundByEmail.get().getId()
        );


        // READ ALL
        List<Customer> customers =
                customerRepository.findAll();

        assertTrue(
                customers.stream()
                        .anyMatch(
                                c -> c.getId()
                                        == customerId
                        )
        );


        // UPDATE
        Customer existing =
                found.orElseThrow();

        existing.updateEmail(
                "updated.customer@test.com"
        );

        customerRepository.save(existing);

        Customer updated =
                customerRepository
                        .findById(customerId)
                        .orElseThrow();

        assertEquals(
                "updated.customer@test.com",
                updated.getEmail()
        );


        // DELETE
        customerRepository.deleteById(
                customerId
        );

        assertFalse(
                customerRepository.existsById(
                        customerId
                )
        );
    }
}