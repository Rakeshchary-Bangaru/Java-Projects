package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardPaymentStrategyTest {

    @Test
    void shouldProcessCardPayment() {

        CardPaymentStrategy strategy =
                new CardPaymentStrategy();

        PaymentResult result = strategy.pay(5000);

        assertAll(
                () -> assertTrue(result.isSuccessful()),
                () -> assertEquals(PaymentType.CARD,
                        result.getPaymentType()),
                () -> assertEquals(5000,
                        result.getAmount()),
                () -> assertEquals(
                        "Card payment successful",
                        result.getMessage())
        );
    }

    @Test
    void shouldRejectZeroAmount() {

        CardPaymentStrategy strategy =
                new CardPaymentStrategy();

        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.pay(0)
        );
    }

    @Test
    void shouldRejectNegativeAmount() {

        CardPaymentStrategy strategy =
                new CardPaymentStrategy();

        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.pay(-100)
        );
    }
}