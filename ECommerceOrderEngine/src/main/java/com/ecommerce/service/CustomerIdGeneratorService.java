package com.ecommerce.service;

import com.ecommerce.model.CustomerIdSequence;
import com.ecommerce.repository.CustomerIdSequenceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


@Service
public class CustomerIdGeneratorService {

    private final CustomerIdSequenceRepository repository;


    public CustomerIdGeneratorService(
            CustomerIdSequenceRepository repository) {

        this.repository = repository;
    }


    /*
     * =========================================================
     * GENERATE CUSTOMER ID
     * ---------------------------------------------------------
     * Inserts one row into customer_id_sequence.
     *
     * MySQL generates the AUTO_INCREMENT id safely.
     *
     * This avoids unsafe approaches such as:
     *
     * SELECT MAX(id) + 1
     *
     * which can fail under concurrent registrations.
     * =========================================================
     */
    @Transactional
    public long generateCustomerId() {

        CustomerIdSequence sequence =
                new CustomerIdSequence(
                        Instant.now()
                );


        CustomerIdSequence saved =
                repository.save(sequence);


        if (saved.getId() == null) {
            throw new IllegalStateException(
                    "Customer id could not be generated"
            );
        }


        return saved.getId();
    }
}