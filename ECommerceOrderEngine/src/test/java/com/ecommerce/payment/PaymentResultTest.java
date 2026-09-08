package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentResultTest {

    @Test
    void shouldCreateValidPaymentResult() {

        PaymentResult result =
                new PaymentResult(
                        true,
                        PaymentType.CARD,
                        5000,
                        "Payment successful"
                );

        assertAll(
                () -> assertTrue(result.isSuccessful()),
                () -> assertEquals(
                        PaymentType.CARD,
                        result.getPaymentType()),
                () -> assertEquals(
                        5000,
                        result.getAmount()),
                () -> assertEquals(
                        "Payment successful",
                        result.getMessage())
        );
    }

    @Test
    void shouldRejectNullPaymentType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentResult(
                        true,
                        null,
                        5000,
                        "Payment successful"
                )
        );
    }

    @Test
    void shouldRejectZeroAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentResult(
                        true,
                        PaymentType.CARD,
                        0,
                        "Payment successful"
                )
        );
    }

    @Test
    void shouldRejectBlankMessage() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentResult(
                        true,
                        PaymentType.CARD,
                        5000,
                        " "
                )
        );
    }
}