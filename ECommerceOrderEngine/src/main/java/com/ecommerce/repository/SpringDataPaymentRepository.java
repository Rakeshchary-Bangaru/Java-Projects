package com.ecommerce.repository;

import com.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataPaymentRepository
        extends JpaRepository<Payment, Long> {

    List<Payment> findByOrder_IdOrderByIdAsc(
            long orderId
    );

    Optional<Payment> findTopByOrder_IdOrderByIdDesc(
            long orderId
    );
}