package com.ecommerce.payment;

/**
 * Coordinates payment processing using a supplied {@link PaymentStrategy}.
 *
 * <p>This class acts as the context in the Strategy Pattern.
 * It does not depend on a specific payment implementation such as
 * card, UPI, or wallet payment. Instead, the required strategy is
 * provided through constructor dependency injection.</p>
 */
public final class PaymentProcessor {

    private final PaymentStrategy paymentStrategy;

    /**
     * Creates a payment processor with the strategy that should be used
     * to process payments.
     *
     * @param paymentStrategy payment strategy to use
     * @throws IllegalArgumentException if the payment strategy is null
     */
    public PaymentProcessor(PaymentStrategy paymentStrategy) {

        if (paymentStrategy == null) {
            throw new IllegalArgumentException(
                    "Payment Strategy cannot be null"
            );
        }

        this.paymentStrategy = paymentStrategy;
    }

    /**
     * Processes a payment by delegating the operation to the configured
     * payment strategy.
     *
     * @param amount amount to be paid
     * @return result returned by the payment strategy
     */
    public PaymentResult processPayment(double amount) {
        return paymentStrategy.pay(amount);
    }
}