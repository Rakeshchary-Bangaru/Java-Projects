package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;

/**
 * Represents the result of a payment attempt.
 *
 * <p>The result contains whether the payment was successful,
 * the payment type used, the amount processed, and a descriptive message.</p>
 *
 * <p>All fields are immutable once the object is created.</p>
 */
public final class PaymentResult {

    private final boolean successful;
    private final PaymentType paymentType;
    private final double amount;
    private final String message;

    /**
     * Creates a payment result.
     *
     * @param successful whether the payment was successful
     * @param paymentType payment method used
     * @param amount amount processed
     * @param message descriptive payment result message
     * @throws IllegalArgumentException if the payment type is null,
     *                                  amount is not greater than zero,
     *                                  or message is null or blank
     */
    public PaymentResult(
            boolean successful,
            PaymentType paymentType,
            double amount,
            String message) {

        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Payment type cannot be null"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "Message cannot be empty"
            );
        }

        this.successful = successful;
        this.paymentType = paymentType;
        this.amount = amount;
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public double getAmount() {
        return amount;
    }

    public String getMessage() {
        return message;
    }
}