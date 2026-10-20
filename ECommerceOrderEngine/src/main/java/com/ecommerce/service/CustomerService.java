package com.ecommerce.service;

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

    public Optional<Customer> findById(long id) {

        return customerRepository.findById(id);
    }

    public List<Customer> findAll() {

        return customerRepository.findAll();
    }

    public Optional<Customer> findByEmail(String email) {

        return customerRepository.findByEmail(email);
    }

    @Transactional
    public Customer updateEmail(
            long customerId,
            String newEmail) {

        Customer customer =
                customerRepository
                        .findById(customerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found: "
                                                + customerId
                                )
                        );

        customer.updateEmail(newEmail);

        return customer;
    }

    public void deleteById(long id) {

        customerRepository.deleteById(id);
    }
}