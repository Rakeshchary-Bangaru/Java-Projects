package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Customer;
import com.ecommerce.repository.SpringDataCustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private SpringDataCustomerRepository customerRepository;


    @InjectMocks
    private CustomerService customerService;


    /*
     * save()
     */
    @Test
    void shouldSaveCustomer() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        when(
                customerRepository.save(customer)
        )
                .thenReturn(customer);


        Customer result =
                customerService.save(customer);


        assertSame(
                customer,
                result
        );


        verify(customerRepository)
                .save(customer);
    }


    /*
     * save(null)
     */
    @Test
    void shouldThrowExceptionWhenSavingNullCustomer() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                customerService.save(null)
                );


        assertEquals(
                "Customer cannot be null",
                exception.getMessage()
        );


        verify(
                customerRepository,
                never()
        )
                .save(any(Customer.class));
    }


    /*
     * findByIdOrThrow()
     */
    @Test
    void shouldReturnCustomerWhenCustomerExists() {

        long customerId = 8101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        when(
                customerRepository.findById(customerId)
        )
                .thenReturn(
                        Optional.of(customer)
                );


        Customer result =
                customerService.findByIdOrThrow(
                        customerId
                );


        assertSame(
                customer,
                result
        );


        assertEquals(
                8101L,
                result.getId()
        );


        assertEquals(
                "Rakesh",
                result.getName()
        );


        assertEquals(
                "rakesh@email.com",
                result.getEmail()
        );


        verify(customerRepository)
                .findById(customerId);
    }


    /*
     * findByIdOrThrow() missing customer
     */
    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        long customerId = 9999L;


        when(
                customerRepository.findById(customerId)
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                customerService
                                        .findByIdOrThrow(
                                                customerId
                                        )
                );


        assertEquals(
                "Customer not found: 9999",
                exception.getMessage()
        );


        verify(customerRepository)
                .findById(customerId);
    }


    /*
     * findAll()
     */
    @Test
    void shouldReturnAllCustomers() {

        Customer customer1 =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Customer customer2 =
                new Customer(
                        8102L,
                        "Alice",
                        "alice@email.com"
                );


        List<Customer> customers =
                List.of(
                        customer1,
                        customer2
                );


        when(
                customerRepository.findAll()
        )
                .thenReturn(customers);


        List<Customer> result =
                customerService.findAll();


        assertEquals(
                2,
                result.size()
        );


        assertEquals(
                "Rakesh",
                result.get(0).getName()
        );


        assertEquals(
                "Alice",
                result.get(1).getName()
        );


        verify(customerRepository)
                .findAll();
    }


    /*
     * findByEmail()
     */
    @Test
    void shouldReturnCustomerWhenEmailExists() {

        String email =
                "rakesh@email.com";


        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        email
                );


        when(
                customerRepository.findByEmail(email)
        )
                .thenReturn(
                        Optional.of(customer)
                );


        Optional<Customer> result =
                customerService.findByEmail(email);


        assertTrue(
                result.isPresent()
        );


        assertSame(
                customer,
                result.get()
        );


        assertEquals(
                email,
                result.get().getEmail()
        );


        verify(customerRepository)
                .findByEmail(email);
    }


    /*
     * findByEmail() no matching customer
     */
    @Test
    void shouldReturnEmptyOptionalWhenEmailDoesNotExist() {

        String email =
                "missing@email.com";


        when(
                customerRepository.findByEmail(email)
        )
                .thenReturn(
                        Optional.empty()
                );


        Optional<Customer> result =
                customerService.findByEmail(email);


        assertFalse(
                result.isPresent()
        );


        verify(customerRepository)
                .findByEmail(email);
    }


    /*
     * updateEmail()
     */
    @Test
    void shouldUpdateCustomerEmail() {

        long customerId = 8101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "old@email.com"
                );


        when(
                customerRepository.findById(customerId)
        )
                .thenReturn(
                        Optional.of(customer)
                );


        Customer result =
                customerService.updateEmail(
                        customerId,
                        "new@email.com"
                );


        assertSame(
                customer,
                result
        );


        assertEquals(
                "new@email.com",
                result.getEmail()
        );


        verify(customerRepository)
                .findById(customerId);
    }


    /*
     * updateEmail() missing customer
     */
    @Test
    void shouldThrowExceptionWhenUpdatingEmailOfMissingCustomer() {

        long customerId = 9999L;


        when(
                customerRepository.findById(customerId)
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                customerService.updateEmail(
                                        customerId,
                                        "new@email.com"
                                )
                );


        assertEquals(
                "Customer not found: 9999",
                exception.getMessage()
        );


        verify(customerRepository)
                .findById(customerId);
    }


    /*
     * replace()
     */
    @Test
    void shouldReplaceCustomerDetails() {

        long customerId = 8101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "old@email.com"
                );


        when(
                customerRepository.findById(customerId)
        )
                .thenReturn(
                        Optional.of(customer)
                );


        Customer result =
                customerService.replace(
                        customerId,
                        "Rakesh Bangaru",
                        "rakeshbangaru@email.com"
                );


        assertSame(
                customer,
                result
        );


        assertEquals(
                "Rakesh Bangaru",
                result.getName()
        );


        assertEquals(
                "rakeshbangaru@email.com",
                result.getEmail()
        );


        verify(customerRepository)
                .findById(customerId);
    }


    /*
     * replace() missing customer
     */
    @Test
    void shouldThrowExceptionWhenReplacingMissingCustomer() {

        long customerId = 9999L;


        when(
                customerRepository.findById(customerId)
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                customerService.replace(
                                        customerId,
                                        "Rakesh",
                                        "rakesh@email.com"
                                )
                );


        assertEquals(
                "Customer not found: 9999",
                exception.getMessage()
        );


        verify(customerRepository)
                .findById(customerId);
    }


    /*
     * deleteOrThrow()
     */
    @Test
    void shouldDeleteExistingCustomer() {

        long customerId = 8101L;


        when(
                customerRepository.existsById(customerId)
        )
                .thenReturn(true);


        customerService.deleteOrThrow(
                customerId
        );


        verify(customerRepository)
                .existsById(customerId);


        verify(customerRepository)
                .deleteById(customerId);
    }


    /*
     * deleteOrThrow() missing customer
     */
    @Test
    void shouldThrowExceptionWhenDeletingMissingCustomer() {

        long customerId = 9999L;


        when(
                customerRepository.existsById(customerId)
        )
                .thenReturn(false);


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                customerService.deleteOrThrow(
                                        customerId
                                )
                );


        assertEquals(
                "Customer not found: 9999",
                exception.getMessage()
        );


        verify(customerRepository)
                .existsById(customerId);


        verify(
                customerRepository,
                never()
        )
                .deleteById(customerId);
    }
}