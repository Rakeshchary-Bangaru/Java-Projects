package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;
import org.springframework.stereotype.Component;

@Component
public class DefaultPaymentProcessorProvider
        implements PaymentProcessorProvider {

    @Override
    public PaymentProcessor get(
            PaymentType paymentType) {

        return new PaymentProcessor(
                PaymentFactory.create(
                        paymentType
                )
        );
    }
}