package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;

/**
 * Simple Factory responsible for creating payment strategy implementations.
 *
 * <p>The factory centralizes object creation so higher-level code does not
 * need to directly decide which concrete {@link PaymentStrategy}
 * implementation should be instantiated.</p>
 */
public final class PaymentFactory {

    /**
     * Prevents creation of PaymentFactory objects because the class
     * provides only static factory behavior.
     */
    private PaymentFactory() {
    }

    /**
     * Creates the appropriate payment strategy for the supplied payment type.
     *
     * @param paymentType type of payment strategy to create
     * @return payment strategy corresponding to the supplied payment type
     * @throws IllegalArgumentException if the payment type is null
     */
    public static PaymentStrategy create(PaymentType paymentType) {

        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Payment type cannot be null"
            );
        }

        return switch (paymentType) {
            case CARD -> new CardPaymentStrategy();
            case UPI -> new UpiPaymentStrategy();
            case WALLET -> new WalletPaymentStrategy();
        };
    }
}