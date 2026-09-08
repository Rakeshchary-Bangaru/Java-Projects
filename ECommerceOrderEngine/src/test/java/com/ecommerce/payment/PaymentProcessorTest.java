package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentProcessorTest {

    @Test
    void shouldProcessCardPayment() {

        PaymentStrategy strategy =
                new CardPaymentStrategy();

        PaymentProcessor processor =
                new PaymentProcessor(strategy);

        PaymentResult result =
                processor.processPayment(5000);

        assertAll(
                () -> assertTrue(result.isSuccessful()),
                () -> assertEquals(
                        PaymentType.CARD,
                        result.getPaymentType()
                ),
                () -> assertEquals(
                        5000,
                        result.getAmount()
                ),
                () -> assertEquals(
                        "Card payment successful",
                        result.getMessage()
                )
        );
    }

    @Test
    void shouldProcessUpiPayment() {

        PaymentStrategy strategy =
                new UpiPaymentStrategy();

        PaymentProcessor processor =
                new PaymentProcessor(strategy);

        PaymentResult result =
                processor.processPayment(2500);

        assertTrue(result.isSuccessful());
        assertEquals(
                PaymentType.UPI,
                result.getPaymentType()
        );
        assertEquals(
                2500,
                result.getAmount()
        );
    }

    @Test
    void shouldProcessWalletPayment() {

        PaymentStrategy strategy =
                new WalletPaymentStrategy();

        PaymentProcessor processor =
                new PaymentProcessor(strategy);

        PaymentResult result =
                processor.processPayment(1500);

        assertTrue(result.isSuccessful());
        assertEquals(
                PaymentType.WALLET,
                result.getPaymentType()
        );
    }

    @Test
    void shouldRejectNullPaymentStrategy() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentProcessor(null)
        );
    }

    @Test
    void shouldRejectZeroPaymentAmount() {

        PaymentProcessor processor =
                new PaymentProcessor(
                        new CardPaymentStrategy()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.processPayment(0)
        );
    }

    @Test
    void shouldRejectNegativePaymentAmount() {

        PaymentProcessor processor =
                new PaymentProcessor(
                        new UpiPaymentStrategy()
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> processor.processPayment(-100)
        );
    }
}