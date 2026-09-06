package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFactoryTest {

    @Test
    void shouldCreateCardPaymentStrategy() {

        PaymentStrategy strategy =
                PaymentFactory.create(
                        PaymentType.CARD
                );

        assertInstanceOf(
                CardPaymentStrategy.class,
                strategy
        );
    }

    @Test
    void shouldCreateUpiPaymentStrategy() {

        PaymentStrategy strategy =
                PaymentFactory.create(
                        PaymentType.UPI
                );

        assertInstanceOf(
                UpiPaymentStrategy.class,
                strategy
        );
    }

    @Test
    void shouldCreateWalletPaymentStrategy() {

        PaymentStrategy strategy =
                PaymentFactory.create(
                        PaymentType.WALLET
                );

        assertInstanceOf(
                WalletPaymentStrategy.class,
                strategy
        );
    }

    @Test
    void shouldRejectNullPaymentType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> PaymentFactory.create(null)
        );
    }
}