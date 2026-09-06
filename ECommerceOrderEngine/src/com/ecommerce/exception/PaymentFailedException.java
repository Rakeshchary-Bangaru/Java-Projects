package com.ecommerce.exception;

/**
 * Thrown when payment processing is unsuccessful during checkout.
 */
public class PaymentFailedException extends RuntimeException {

    /**
     * Creates an exception with the specified payment failure message.
     *
     * @param message description of the payment failure
     */
    public PaymentFailedException(String message) {
        super(message);
    }
}