package com.ecommerce.inventory;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private InventoryService inventory;
    private Product iphone;

    @BeforeEach
    void setUp() {

        inventory = new InventoryService();

        iphone = new Product(
                1,
                "iPhone",
                "Electronics",
                80000
        );
    }

    @Test
    void shouldStartWithZeroStock() {

        assertEquals(
                0,
                inventory.getStock(iphone.getId())
        );
    }

    @Test
    void shouldAddStock() {

        inventory.addStock(iphone, 10);

        assertEquals(
                10,
                inventory.getStock(iphone.getId())
        );
    }

    @Test
    void shouldIncreaseExistingStock() {

        inventory.addStock(iphone, 10);
        inventory.addStock(iphone, 5);

        assertEquals(
                15,
                inventory.getStock(iphone.getId())
        );
    }

    @Test
    void shouldReturnTrueWhenEnoughStockAvailable() {

        inventory.addStock(iphone, 10);

        assertTrue(
                inventory.isAvailable(
                        iphone.getId(),
                        5
                )
        );
    }

    @Test
    void shouldReturnFalseWhenStockIsInsufficient() {

        inventory.addStock(iphone, 3);

        assertFalse(
                inventory.isAvailable(
                        iphone.getId(),
                        5
                )
        );
    }

    @Test
    void shouldReserveStock() {

        inventory.addStock(iphone, 10);

        inventory.reserveStock(
                iphone.getId(),
                3
        );

        assertEquals(
                7,
                inventory.getStock(iphone.getId())
        );
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {

        inventory.addStock(iphone, 2);

        assertThrows(
                InsufficientStockException.class,
                () -> inventory.reserveStock(
                        iphone.getId(),
                        5
                )
        );
    }

    @Test
    void shouldReleaseReservedStock() {

        inventory.addStock(iphone, 10);

        inventory.reserveStock(
                iphone.getId(),
                3
        );

        inventory.releaseStock(
                iphone.getId(),
                3
        );

        assertEquals(
                10,
                inventory.getStock(iphone.getId())
        );
    }

    @Test
    void shouldRejectNullProductWhenAddingStock() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.addStock(null, 5)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidAddStockQuantity(
            int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.addStock(
                        iphone,
                        invalidQuantity
                )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidAvailabilityQuantity(
            int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.isAvailable(
                        iphone.getId(),
                        invalidQuantity
                )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidReserveQuantity(
            int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.reserveStock(
                        iphone.getId(),
                        invalidQuantity
                )
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void shouldRejectInvalidReleaseQuantity(
            int invalidQuantity) {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.releaseStock(
                        iphone.getId(),
                        invalidQuantity
                )
        );
    }
}