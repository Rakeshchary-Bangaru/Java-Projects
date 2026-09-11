package com.ecommerce.repository;

import com.ecommerce.payment.PaymentResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    void save(
            long orderId,
            PaymentResult paymentResult
    ) throws SQLException;

    List<PaymentResult> findByOrderId(long orderId)
            throws SQLException;

    Optional<PaymentResult> findLatestByOrderId(long orderId)
            throws SQLException;
}