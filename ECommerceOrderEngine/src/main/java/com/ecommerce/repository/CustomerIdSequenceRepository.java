package com.ecommerce.repository;

import com.ecommerce.model.CustomerIdSequence;

import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerIdSequenceRepository
        extends JpaRepository<CustomerIdSequence, Long> {
}