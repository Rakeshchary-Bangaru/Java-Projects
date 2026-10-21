package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;

@FunctionalInterface
public interface PaymentProcessorProvider {

    PaymentProcessor get(
            PaymentType paymentType
    );
}