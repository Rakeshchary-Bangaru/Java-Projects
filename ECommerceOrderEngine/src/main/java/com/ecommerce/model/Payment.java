package com.ecommerce.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false
    )
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "payment_type",
            nullable = false,
            length = 20
    )
    private PaymentType paymentType;

    @Column(
            name = "amount",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal amount;

    @Column(
            name = "successful",
            nullable = false
    )
    private boolean successful;

    @Column(
            name = "message",
            nullable = false,
            length = 255
    )
    private String message;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    protected Payment() {
        // Required by JPA
    }

    public Payment(
            Order order,
            PaymentType paymentType,
            double amount,
            boolean successful,
            String message
    ) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Payment type cannot be null"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment message cannot be empty"
            );
        }

        this.order = order;
        this.paymentType = paymentType;

        this.amount =
                BigDecimal.valueOf(amount)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        this.successful = successful;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public double getAmount() {
        return amount.doubleValue();
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}