package com.ecommerce.repository;

import com.ecommerce.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCustomerRepository
        extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    /*
     * Finds the customer profile linked to the authenticated
     * UserAccount primary key.
     *
     * The underscore explicitly tells Spring Data to navigate:
     *
     * Customer.userAccount.id
     */
    Optional<Customer> findByUserAccount_Id(
            Long userAccountId
    );
}