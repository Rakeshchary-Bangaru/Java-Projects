package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Customer;
import com.ecommerce.repository.SpringDataCustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final SpringDataCustomerRepository customerRepository;

    public CustomerService(
            SpringDataCustomerRepository customerRepository) {

        this.customerRepository = customerRepository;
    }

    public Customer save(Customer customer) {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer findByIdOrThrow(
            long id) {

        return customerRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + id
                        )
                );
    }

    public List<Customer> findAll() {

        return customerRepository.findAll();
    }

    public Optional<Customer> findByEmail(String email) {

        return customerRepository.findByEmail(email);
    }
    @Transactional
    public Customer updateEmail(
            long id,
            String newEmail) {

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found: " + id
                                )
                        );

        customer.updateEmail(newEmail);

        return customer;
    }

    @Transactional
    public Customer replace(
            long id,
            String name,
            String email) {

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found: " + id
                                )
                        );

        customer.updateDetails(
                name,
                email
        );

        return customer;
    }

    @Transactional
    public void deleteOrThrow(
            long id) {

        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Customer not found: " + id
            );
        }

        customerRepository.deleteById(id);
    }
}