package com.ecommerce.service;

import com.ecommerce.model.CustomerIdSequence;
import com.ecommerce.repository.CustomerIdSequenceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class CustomerIdGeneratorServiceTest {

    private CustomerIdSequenceRepository repository;

    private CustomerIdGeneratorService generatorService;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        CustomerIdSequenceRepository.class
                );


        generatorService =
                new CustomerIdGeneratorService(
                        repository
                );
    }


    /*
     * =========================================================
     * generateCustomerId()
     *
     * Successful database-generated customer id.
     * =========================================================
     */
    @Test
    void shouldGenerateCustomerId() {

        CustomerIdSequence savedSequence =
                mock(
                        CustomerIdSequence.class
                );


        when(
                savedSequence.getId()
        )
                .thenReturn(
                        1000000L
                );


        when(
                repository.save(
                        any(CustomerIdSequence.class)
                )
        )
                .thenReturn(
                        savedSequence
                );


        long generatedId =
                generatorService
                        .generateCustomerId();


        assertEquals(
                1000000L,
                generatedId
        );


        verify(repository)
                .save(
                        any(CustomerIdSequence.class)
                );
    }


    /*
     * =========================================================
     * generateCustomerId()
     *
     * Defensive check:
     * a generated sequence must always contain an id.
     * =========================================================
     */
    @Test
    void shouldThrowExceptionWhenGeneratedIdIsNull() {

        CustomerIdSequence savedSequence =
                mock(
                        CustomerIdSequence.class
                );


        when(
                savedSequence.getId()
        )
                .thenReturn(
                        null
                );


        when(
                repository.save(
                        any(CustomerIdSequence.class)
                )
        )
                .thenReturn(
                        savedSequence
                );


        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                generatorService
                                        .generateCustomerId()
                );


        assertEquals(
                "Customer id could not be generated",
                exception.getMessage()
        );


        verify(repository)
                .save(
                        any(CustomerIdSequence.class)
                );
    }
}