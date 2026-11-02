package com.ecommerce.model;

import jakarta.persistence.*;

import java.time.Instant;


/*
 * =========================================================
 * CUSTOMER ID SEQUENCE
 * ---------------------------------------------------------
 * Used only for generating new Customer IDs for V7
 * authenticated customer registrations.
 *
 * Existing legacy Customer IDs remain untouched.
 * =========================================================
 */
@Entity
@Table(name = "customer_id_sequence")
public class CustomerIdSequence {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;


    protected CustomerIdSequence() {
        // Required by JPA
    }


    public CustomerIdSequence(
            Instant createdAt) {

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Created time cannot be null"
            );
        }

        this.createdAt = createdAt;
    }


    public Long getId() {
        return id;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }
}