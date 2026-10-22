package com.ecommerce.controller;

import com.ecommerce.dto.CustomerEmailUpdateRequest;
import com.ecommerce.dto.CustomerRequest;
import com.ecommerce.dto.CustomerResponse;

import com.ecommerce.model.Customer;
import com.ecommerce.service.CustomerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService) {

        this.customerService = customerService;
    }


    @GetMapping
    public List<CustomerResponse> getAllCustomers() {

        return customerService
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse>
    getCustomerById(
            @PathVariable long id) {

        Customer customer =
                customerService.findByIdOrThrow(id);

        return ResponseEntity.ok(
                toResponse(customer)
        );
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(
            @Valid
            @RequestBody CustomerRequest request) {

        Customer customer =
                new Customer(
                        request.getId(),
                        request.getName(),
                        request.getEmail()
                );

        Customer savedCustomer =
                customerService.save(customer);

        return toResponse(savedCustomer);
    }


    @PatchMapping("/{id}/email")
    public ResponseEntity<CustomerResponse>
    updateCustomerEmail(
            @PathVariable long id,
            @Valid
            @RequestBody
            CustomerEmailUpdateRequest request) {

        Customer customer =
                customerService.updateEmail(
                        id,
                        request.getEmail()
                );

        return ResponseEntity.ok(
                toResponse(customer)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse>
    replaceCustomer(
            @PathVariable long id,
            @Valid
            @RequestBody CustomerRequest request) {

        Customer customer =
                customerService.replace(
                        id,
                        request.getName(),
                        request.getEmail()
                );

        return ResponseEntity.ok(
                toResponse(customer)
        );
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable long id) {

        customerService.deleteOrThrow(id);

        return ResponseEntity
                .noContent()
                .build();
    }


    private CustomerResponse toResponse(
            Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }
}