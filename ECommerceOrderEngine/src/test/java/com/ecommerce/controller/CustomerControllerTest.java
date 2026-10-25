package com.ecommerce.controller;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Customer;
import com.ecommerce.service.CustomerService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private CustomerService customerService;


    /*
     * GET /api/customers
     */
    @Test
    void shouldReturnAllCustomers() throws Exception {

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


        when(customerService.findAll())
                .thenReturn(
                        List.of(
                                customer1,
                                customer2
                        )
                );


        mockMvc.perform(
                        get("/api/customers")
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$[0].id")
                                .value(8101)
                )
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Rakesh")
                )
                .andExpect(
                        jsonPath("$[0].email")
                                .value("rakesh@email.com")
                )
                .andExpect(
                        jsonPath("$[1].id")
                                .value(8102)
                )
                .andExpect(
                        jsonPath("$[1].name")
                                .value("Alice")
                )
                .andExpect(
                        jsonPath("$[1].email")
                                .value("alice@email.com")
                );


        verify(customerService)
                .findAll();
    }


    /*
     * GET /api/customers/{id}
     */
    @Test
    void shouldReturnCustomerById() throws Exception {

        long customerId = 8101L;


        Customer customer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "rakesh@email.com"
                );


        when(
                customerService.findByIdOrThrow(
                        customerId
                )
        )
                .thenReturn(customer);


        mockMvc.perform(
                        get(
                                "/api/customers/{id}",
                                customerId
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8101)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Rakesh")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("rakesh@email.com")
                );


        verify(customerService)
                .findByIdOrThrow(
                        customerId
                );
    }


    /*
     * GET missing customer -> 404
     */
    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist()
            throws Exception {

        long customerId = 9999L;


        when(
                customerService.findByIdOrThrow(
                        customerId
                )
        )
                .thenThrow(
                        new ResourceNotFoundException(
                                "Customer not found"
                        )
                );


        mockMvc.perform(
                        get(
                                "/api/customers/{id}",
                                customerId
                        )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(customerService)
                .findByIdOrThrow(
                        customerId
                );
    }


    /*
     * POST /api/customers
     */
    @Test
    void shouldCreateCustomer() throws Exception {

        Customer savedCustomer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        when(
                customerService.save(
                        any(Customer.class)
                )
        )
                .thenReturn(savedCustomer);


        String requestBody =
                """
                {
                  "id": 8101,
                  "name": "Rakesh",
                  "email": "rakesh@email.com"
                }
                """;


        mockMvc.perform(
                        post("/api/customers")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8101)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Rakesh")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("rakesh@email.com")
                );


        verify(customerService)
                .save(
                        any(Customer.class)
                );
    }


    /*
     * POST invalid customer -> 400
     */
    @Test
    void shouldReturnBadRequestWhenCreatingInvalidCustomer()
            throws Exception {

        String requestBody =
                """
                {
                  "id": 8101,
                  "name": "",
                  "email": ""
                }
                """;


        mockMvc.perform(
                        post("/api/customers")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );


        verify(
                customerService,
                never()
        )
                .save(
                        any(Customer.class)
                );
    }


    /*
     * PUT /api/customers/{id}
     */
    @Test
    void shouldReplaceCustomer() throws Exception {

        long customerId = 8101L;


        Customer updatedCustomer =
                new Customer(
                        customerId,
                        "Rakesh Bangaru",
                        "rakeshbangaru@email.com"
                );


        when(
                customerService.replace(
                        customerId,
                        "Rakesh Bangaru",
                        "rakeshbangaru@email.com"
                )
        )
                .thenReturn(
                        updatedCustomer
                );


        String requestBody =
                """
                {
                  "id": 8101,
                  "name": "Rakesh Bangaru",
                  "email": "rakeshbangaru@email.com"
                }
                """;


        mockMvc.perform(
                        put(
                                "/api/customers/{id}",
                                customerId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8101)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Rakesh Bangaru")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "rakeshbangaru@email.com"
                                )
                );


        verify(customerService)
                .replace(
                        customerId,
                        "Rakesh Bangaru",
                        "rakeshbangaru@email.com"
                );
    }


    /*
     * PATCH /api/customers/{id}/email
     */
    @Test
    void shouldUpdateCustomerEmail() throws Exception {

        long customerId = 8101L;


        Customer updatedCustomer =
                new Customer(
                        customerId,
                        "Rakesh",
                        "newemail@email.com"
                );


        when(
                customerService.updateEmail(
                        customerId,
                        "newemail@email.com"
                )
        )
                .thenReturn(
                        updatedCustomer
                );


        String requestBody =
                """
                {
                  "email": "newemail@email.com"
                }
                """;


        mockMvc.perform(
                        patch(
                                "/api/customers/{id}/email",
                                customerId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.id")
                                .value(8101)
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Rakesh")
                )
                .andExpect(
                        jsonPath("$.email")
                                .value(
                                        "newemail@email.com"
                                )
                );


        verify(customerService)
                .updateEmail(
                        customerId,
                        "newemail@email.com"
                );
    }


    /*
     * PATCH invalid email -> 400
     */
    @Test
    void shouldReturnBadRequestWhenUpdatingInvalidEmail()
            throws Exception {

        String requestBody =
                """
                {
                  "email": ""
                }
                """;


        mockMvc.perform(
                        patch(
                                "/api/customers/{id}/email",
                                8101L
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );


        verify(
                customerService,
                never()
        )
                .updateEmail(
                        anyLong(),
                        any(String.class)
                );
    }


    /*
     * DELETE /api/customers/{id}
     */
    @Test
    void shouldDeleteCustomer() throws Exception {

        long customerId = 8101L;


        mockMvc.perform(
                        delete(
                                "/api/customers/{id}",
                                customerId
                        )
                )
                .andExpect(
                        status().isNoContent()
                );


        verify(customerService)
                .deleteOrThrow(
                        customerId
                );
    }


    /*
     * DELETE missing customer -> 404
     */
    @Test
    void shouldReturnNotFoundWhenDeletingMissingCustomer()
            throws Exception {

        long customerId = 9999L;


        doThrow(
                new ResourceNotFoundException(
                        "Customer not found"
                )
        )
                .when(customerService)
                .deleteOrThrow(
                        customerId
                );


        mockMvc.perform(
                        delete(
                                "/api/customers/{id}",
                                customerId
                        )
                )
                .andExpect(
                        status().isNotFound()
                );


        verify(customerService)
                .deleteOrThrow(
                        customerId
                );
    }
}