package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;

/**
 * Payment strategy that processes payments using a digital wallet.
 *
 * <p>This class is a concrete implementation of the
 * {@link PaymentStrategy} interface.</p>
 */
public final class WalletPaymentStrategy implements PaymentStrategy {

    /**
     * Processes a wallet payment for the specified amount.
     *
     * @param amount amount to be paid
     * @return successful payment result for a wallet payment
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
                PaymentType.WALLET,
                amount,
                "Wallet payment successful"
        );
    }
}