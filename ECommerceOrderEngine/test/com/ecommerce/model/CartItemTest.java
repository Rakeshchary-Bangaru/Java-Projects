package com.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {

        product = new Product(
                1,
                "iPhone",
                "Electronics",
                80000
        );

        cartItem = new CartItem(product, 2);
    }

    @Test
    void shouldCreateValidCartItem() {

        assertAll(
                () -> assertSame(product, cartItem.getProduct()),
                () -> assertEquals(2, cartItem.getQuantity())
        );
    }

    @Test
    void shouldCalculateSubtotal() {

        assertEquals(
                160000,
                cartItem.getSubtotal()
        );
    }

    @Test
    void shouldUpdateQuantity() {

        cartItem.updateQuantity(3);

        assertEquals(
                3,
                cartItem.getQuantity()
        );
    }

    @Test
    void shouldRecalculateSubtotalAfterQuantityUpdate() {

        cartItem.updateQuantity(3);

        assertEquals(
                240000,
                cartItem.getSubtotal()
        );
    }

    @Test
    void shouldReflectCurrentProductPrice() {

        product.updatePrice(75000);

        assertEquals(
                150000,
                cartItem.getSubtotal()
        );
    }

    @Test
    void shouldRejectNullProduct() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(null, 2)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidQuantity(int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(
                        product,
                        invalidQuantity
                )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidQuantityUpdate(int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartItem.updateQuantity(
                        invalidQuantity
                )
        );
    }
}