package com.ecommerce.service;

import com.ecommerce.model.Customer;

import com.ecommerce.repository.SpringDataCustomerRepository;

import com.ecommerce.security.model.UserAccount;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class CustomerProfileService {

    private final SpringDataCustomerRepository
            customerRepository;

    private final CustomerIdGeneratorService
            customerIdGeneratorService;


    public CustomerProfileService(
            SpringDataCustomerRepository customerRepository,
            CustomerIdGeneratorService customerIdGeneratorService
    ) {

        this.customerRepository =
                customerRepository;

        this.customerIdGeneratorService =
                customerIdGeneratorService;
    }


    /*
     * =========================================================
     * ENSURE CUSTOMER SHOPPING PROFILE
     * ---------------------------------------------------------
     * Every authenticated UserAccount may act as a shopper.
     *
     * Therefore every account should have exactly one Customer
     * commerce profile.
     *
     * This method is intentionally role-independent.
     * =========================================================
     */
    @Transactional
    public Customer ensureCustomerProfile(
            UserAccount user,
            String name
    ) {

        if (user == null) {

            throw new IllegalArgumentException(
                    "User account cannot be null"
            );
        }


        if (user.getId() == null ||
                user.getId() <= 0) {

            throw new IllegalArgumentException(
                    "User account must be persisted before creating customer profile"
            );
        }


        if (name == null ||
                name.isBlank()) {

            throw new IllegalArgumentException(
                    "Customer name cannot be empty"
            );
        }


        /*
         * =====================================================
         * ALREADY LINKED
         * =====================================================
         */
        var existingLinkedCustomer =
                customerRepository
                        .findByUserAccount_Id(
                                user.getId()
                        );


        if (existingLinkedCustomer.isPresent()) {

            return existingLinkedCustomer.get();
        }


        /*
         * =====================================================
         * LEGACY CUSTOMER WITH SAME EMAIL
         * -----------------------------------------------------
         * A legacy customer may already exist but not yet have
         * user_account_id populated.
         *
         * Reuse it rather than creating another customer with
         * the same email.
         * =====================================================
         */
        var existingEmailCustomer =
                customerRepository
                        .findByEmail(
                                user.getEmail()
                        );


        if (existingEmailCustomer.isPresent()) {

            Customer customer =
                    existingEmailCustomer.get();


            /*
             * Existing customer is not linked yet.
             */
            if (customer.getUserAccount() == null) {

                customer.assignUserAccount(
                        user
                );


                return customerRepository.save(
                        customer
                );
            }


            /*
             * Same email must never belong to a different
             * authenticated account.
             */
            if (!customer
                    .getUserAccount()
                    .getId()
                    .equals(user.getId())) {

                throw new IllegalStateException(
                        "Customer email is already linked to another user account"
                );
            }


            return customer;
        }


        /*
         * =====================================================
         * CREATE NEW CUSTOMER PROFILE
         * =====================================================
         */
        long customerId =
                customerIdGeneratorService
                        .generateCustomerId();


        Customer customer =
                new Customer(
                        customerId,
                        name.trim(),
                        user.getEmail()
                );


        customer.assignUserAccount(
                user
        );


        return customerRepository.save(
                customer
        );
    }
}