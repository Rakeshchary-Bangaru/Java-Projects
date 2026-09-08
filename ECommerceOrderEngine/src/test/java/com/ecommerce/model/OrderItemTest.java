package com.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    private Product product;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {

        product = new Product(
                1,
                "iPhone",
                "Electronics",
                80000
        );

        orderItem = new OrderItem(product, 2);
    }

    @Test
    void shouldCreateValidOrderItem() {

        assertAll(
                () -> assertSame(product, orderItem.getProduct()),
                () -> assertEquals(2, orderItem.getQuantity()),
                () -> assertEquals(80000, orderItem.getUnitPrice())
        );
    }

    @Test
    void shouldCalculateSubtotal() {

        assertEquals(
                160000,
                orderItem.getSubtotal()
        );
    }

    @Test
    void shouldKeepOriginalPriceWhenProductPriceChanges() {

        product.updatePrice(75000);

        assertEquals(
                80000,
                orderItem.getUnitPrice()
        );

        assertEquals(
                160000,
                orderItem.getSubtotal()
        );
    }

    @Test
    void shouldRejectNullProduct() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(null, 2)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidQuantity(int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> new OrderItem(
                        product,
                        invalidQuantity
                )
        );
    }
}