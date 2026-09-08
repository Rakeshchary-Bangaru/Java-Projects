package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;

/**
 * Payment strategy that processes payments using UPI.
 *
 * <p>This class is a concrete implementation of the
 * {@link PaymentStrategy} interface.</p>
 */
public final class UpiPaymentStrategy implements PaymentStrategy {

    /**
     * Processes a UPI payment for the specified amount.
     *
     * @param amount amount to be paid
     * @return successful payment result for a UPI payment
     * @throws IllegalArgumentException if the amount is not greater than zero
     */
    @Override
    public PaymentResult pay(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        return new PaymentResult(
                true,
                PaymentType.UPI,
                amount,
                "UPI payment successful"
        );
    }
}