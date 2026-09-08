
package com.ecommerce.payment;

import com.ecommerce.model.PaymentType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletPaymentStrategyTest{
    @Test
    void shouldProcessWalletPayment() {

        WalletPaymentStrategy strategy =
                new WalletPaymentStrategy();

        PaymentResult result = strategy.pay(1500);

        assertAll(
                () -> assertTrue(result.isSuccessful()),
                () -> assertEquals(
                        PaymentType.WALLET,
                        result.getPaymentType()),
                () -> assertEquals(
                        1500,
                        result.getAmount()),
                () -> assertEquals(
                        "Wallet payment successful",
                        result.getMessage())
        );
    }
}