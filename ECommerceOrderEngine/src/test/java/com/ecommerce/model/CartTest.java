package com.ecommerce.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;
    private Product iphone;
    private Product laptop;

    @BeforeEach
    void setUp() {

        cart = new Cart();

        iphone = new Product(
                1,
                "iPhone",
                "Electronics",
                80000
        );

        laptop = new Product(
                2,
                "MacBook",
                "Electronics",
                120000
        );
    }

    @Test
    void shouldBeEmptyWhenCreated() {

        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void shouldAddProduct() {

        cart.addProduct(iphone, 2);

        assertAll(
                () -> assertFalse(cart.isEmpty()),
                () -> assertEquals(1, cart.getItems().size()),
                () -> assertSame(
                        iphone,
                        cart.getItems().get(0).getProduct()
                ),
                () -> assertEquals(
                        2,
                        cart.getItems().get(0).getQuantity()
                )
        );
    }

    @Test
    void shouldAddMultipleProducts() {

        cart.addProduct(iphone, 2);
        cart.addProduct(laptop, 1);

        assertEquals(2, cart.getItems().size());
    }

    @Test
    void shouldIncreaseQuantityWhenSameProductAddedAgain() {

        cart.addProduct(iphone, 2);
        cart.addProduct(iphone, 3);

        assertAll(
                () -> assertEquals(1, cart.getItems().size()),
                () -> assertEquals(
                        5,
                        cart.getItems().get(0).getQuantity()
                )
        );
    }

    @Test
    void shouldRemoveProduct() {

        cart.addProduct(iphone, 2);
        cart.addProduct(laptop, 1);

        cart.removeProduct(iphone.getId());

        assertAll(
                () -> assertEquals(1, cart.getItems().size()),
                () -> assertSame(
                        laptop,
                        cart.getItems().get(0).getProduct()
                )
        );
    }

    @Test
    void shouldUpdateQuantity() {

        cart.addProduct(iphone, 2);

        cart.updateQuantity(iphone.getId(), 5);

        assertEquals(
                5,
                cart.getItems().get(0).getQuantity()
        );
    }

    @Test
    void shouldCalculateTotal() {

        cart.addProduct(iphone, 2);   // 160000
        cart.addProduct(laptop, 1);   // 120000

        assertEquals(
                280000,
                cart.calculateTotal()
        );
    }

    @Test
    void shouldRejectNullProduct() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.addProduct(null, 1)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidQuantityWhenAddingProduct(
            int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.addProduct(
                        iphone,
                        invalidQuantity
                )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidQuantityUpdate(
            int invalidQuantity) {

        cart.addProduct(iphone, 2);

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.updateQuantity(
                        iphone.getId(),
                        invalidQuantity
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMissingProduct() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cart.updateQuantity(999, 5)
        );
    }

    @Test
    void shouldReturnUnmodifiableItemsList() {

        cart.addProduct(iphone, 2);

        List<CartItem> items = cart.getItems();

        assertThrows(
                UnsupportedOperationException.class,
                () -> items.add(new CartItem(laptop, 1))
        );
    }
}