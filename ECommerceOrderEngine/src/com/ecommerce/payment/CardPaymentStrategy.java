package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;

/**
 * Payment strategy that processes payments using a card.
 *
 * <p>This class is one concrete implementation of the
 * {@link PaymentStrategy} interface.</p>
 */
public final class CardPaymentStrategy implements PaymentStrategy {

    /**
     * Processes a card payment for the specified amount.
     *
     * @param amount amount to be paid
     * @return successful payment result for a card payment
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
                PaymentType.CARD,
                amount,
                "Card payment successful"
        );
    }
}