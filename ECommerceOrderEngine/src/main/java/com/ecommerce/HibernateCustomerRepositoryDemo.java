package com.ecommerce;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Customer;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.HibernateCustomerRepository;

public class HibernateCustomerRepositoryDemo {

    public static void main(String[] args) throws Exception {

        CustomerRepository repository =
                new HibernateCustomerRepository();

        long customerId = 920001L;

        // 1. SAVE
        Customer customer =
                new Customer(
                        customerId,
                        "Hibernate Customer",
                        "hibernate.customer@example.com"
                );

        repository.save(customer);

        System.out.println("SAVE completed");


        // 2. FIND BY ID
        repository.findById(customerId)
                .ifPresent(found ->
                        System.out.println(
                                "FOUND: "
                                        + found.getName()
                                        + " - "
                                        + found.getEmail()
                        )
                );


        // 3. FIND ALL
        System.out.println(
                "Total customers: "
                        + repository.findAll().size()
        );


        // 4. UPDATE EMAIL
        Customer existing =
                repository.findById(customerId)
                        .orElseThrow();

        existing.updateEmail(
                "updated.hibernate@example.com"
        );

        repository.update(existing);

        Customer updated =
                repository.findById(customerId)
                        .orElseThrow();

        System.out.println(
                "UPDATED EMAIL: "
                        + updated.getEmail()
        );


        // 5. DELETE
        boolean deleted =
                repository.deleteById(customerId);

        System.out.println(
                "DELETED: " + deleted
        );


        // Verify deletion
        System.out.println(
                "EXISTS AFTER DELETE: "
                        + repository
                        .findById(customerId)
                        .isPresent()
        );

        HibernateUtil.shutdown();
    }
}