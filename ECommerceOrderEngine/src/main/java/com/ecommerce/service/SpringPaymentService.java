package com.ecommerce.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.payment.PaymentResult;
import com.ecommerce.repository.SpringDataOrderRepository;
import com.ecommerce.repository.SpringDataPaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SpringPaymentService {

    private final SpringDataPaymentRepository paymentRepository;
    private final SpringDataOrderRepository orderRepository;

    public SpringPaymentService(
            SpringDataPaymentRepository paymentRepository,
            SpringDataOrderRepository orderRepository) {

        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }


    @Transactional
    public Payment save(
            long orderId,
            PaymentResult paymentResult) {

        if (paymentResult == null) {
            throw new IllegalArgumentException(
                    "Payment result cannot be null"
            );
        }

        /*
         * Similar to your old:
         *
         * session.getReference(Order.class, orderId)
         *
         * Spring Data JPA delegates this to JPA.
         */
        Order order =
                orderRepository.getReferenceById(
                        orderId
                );

        Payment payment =
                new Payment(
                        order,
                        paymentResult.getPaymentType(),
                        paymentResult.getAmount(),
                        paymentResult.isSuccessful(),
                        paymentResult.getMessage()
                );

        return paymentRepository.save(payment);
    }


    @Transactional(readOnly = true)
    public List<PaymentResult> findByOrderId(
            long orderId) {

        return paymentRepository
                .findByOrder_IdOrderByIdAsc(
                        orderId
                )
                .stream()
                .map(this::toPaymentResult)
                .toList();
    }


    @Transactional(readOnly = true)
    public Optional<PaymentResult> findLatestByOrderId(
            long orderId) {

        return paymentRepository
                .findTopByOrder_IdOrderByIdDesc(
                        orderId
                )
                .map(this::toPaymentResult);
    }


    private PaymentResult toPaymentResult(
            Payment payment) {

        return new PaymentResult(
                payment.isSuccessful(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getMessage()
        );
    }
}