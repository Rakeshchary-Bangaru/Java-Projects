package com.ecommerce.payment;

/**
 * Defines the contract for payment processing strategies.
 *
 * <p>Different payment methods can provide their own implementation
 * of this interface while {@link PaymentProcessor} remains independent
 * of the concrete payment type.</p>
 */
public interface PaymentStrategy {

    /**
     * Processes a payment for the specified amount.
     *
     * @param amount amount to be paid
     * @return result of the payment attempt
     * @throws IllegalArgumentException if the amount is invalid
     */
    PaymentResult pay(double amount);
}