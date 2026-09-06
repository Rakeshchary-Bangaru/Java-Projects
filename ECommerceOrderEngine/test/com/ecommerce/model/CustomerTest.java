package com.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static  org.junit.jupiter.api.Assertions.*;

class CustomerTest{
    private Customer customer;

    @BeforeEach
    void setUp(){
        customer = new Customer(1,"Rakesh","rakesh@gmail.com");
    }

    @Test
    void shouldCreateValidCustomer(){
        assertAll(
                () -> assertEquals(1,customer.getId()),
                () -> assertEquals("Rakesh",customer.getName()),
                () -> assertEquals("rakesh@gmail.com",customer.getEmail())
        );
    }

    @Test
    void shouldUpdateEMail(){
        customer.updateEmail("newemail@gmail.com");
        assertEquals("newemail@gmail.com",customer.getEmail());
    }

    @ParameterizedTest
    @ValueSource(longs = {0,-1,-10})
    void shouldRejectInvalidCustomerId(long invalidId){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Customer(invalidId,"Rakesh","rakesh@gmail.com")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"" , " " , "  "})
    void shouldRejectInvalidCustomerName(String invalidName){
        assertThrows(
                IllegalArgumentException.class,
                () -> new Customer(1,invalidName,"rakesh@gmail.com")
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"" , " " , "  "})
    void shouldRejectInvalidEmail(String invalidEmail){
        assertThrows(
                IllegalArgumentException.class,
                ()->new Customer(1,"Rakesh",invalidEmail)
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"" , " " , "  "})
    void shouldRejectInvalidEmailUpdate(String invalidEmail){
        assertThrows(
                IllegalArgumentException.class,
                () -> customer.updateEmail(invalidEmail)
        );
    }
}