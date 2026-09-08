package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class UpiPaymentStrategyTest{
    @Test
    void shouldProcessUpiPayment() {

        UpiPaymentStrategy strategy =
                new UpiPaymentStrategy();

        PaymentResult result = strategy.pay(2500);

        assertAll(
                () -> assertTrue(result.isSuccessful()),
                () -> assertEquals(
                        PaymentType.UPI,
                        result.getPaymentType()),
                () -> assertEquals(
                        2500,
                        result.getAmount()),
                () -> assertEquals(
                        "UPI payment successful",
                        result.getMessage())
        );
    }
}


