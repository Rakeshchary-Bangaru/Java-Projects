package com.ecommerce.service;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.SpringDataInventoryRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SpringInventoryServiceTest {

    @Mock
    private SpringDataInventoryRepository inventoryRepository;


    @InjectMocks
    private SpringInventoryService inventoryService;


    /*
     * addStock()
     */
    @Test
    void shouldAddStock() {

        long productId = 7001L;
        int quantity = 10;


        inventoryService.addStock(
                productId,
                quantity
        );


        verify(inventoryRepository)
                .addStock(
                        productId,
                        quantity
                );
    }


    /*
     * addStock() invalid quantity
     */
    @Test
    void shouldThrowExceptionWhenAddingZeroStock() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                inventoryService.addStock(
                                        7001L,
                                        0
                                )
                );


        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );


        verifyNoInteractions(
                inventoryRepository
        );
    }


    /*
     * addStock() negative quantity
     */
    @Test
    void shouldThrowExceptionWhenAddingNegativeStock() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                inventoryService.addStock(
                                        7001L,
                                        -5
                                )
                );


        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );


        verifyNoInteractions(
                inventoryRepository
        );
    }


    /*
     * getStock()
     */
    @Test
    void shouldReturnStockWhenStockExists() {

        long productId = 7001L;


        when(
                inventoryRepository
                        .findStockByProductId(productId)
        )
                .thenReturn(
                        Optional.of(25)
                );


        int result =
                inventoryService.getStock(
                        productId
                );


        assertEquals(
                25,
                result
        );


        verify(inventoryRepository)
                .findStockByProductId(
                        productId
                );
    }


    /*
     * getStock() no inventory record
     */
    @Test
    void shouldReturnZeroWhenStockDoesNotExist() {

        long productId = 9999L;


        when(
                inventoryRepository
                        .findStockByProductId(productId)
        )
                .thenReturn(
                        Optional.empty()
                );


        int result =
                inventoryService.getStock(
                        productId
                );


        assertEquals(
                0,
                result
        );


        verify(inventoryRepository)
                .findStockByProductId(
                        productId
                );
    }


    /*
     * isAvailable()
     */
    @Test
    void shouldReturnTrueWhenEnoughStockIsAvailable() {

        long productId = 7001L;


        when(
                inventoryRepository
                        .findStockByProductId(productId)
        )
                .thenReturn(
                        Optional.of(20)
                );


        boolean result =
                inventoryService.isAvailable(
                        productId,
                        10
                );


        assertTrue(result);


        verify(inventoryRepository)
                .findStockByProductId(
                        productId
                );
    }


    /*
     * isAvailable()
     */
    @Test
    void shouldReturnFalseWhenStockIsInsufficient() {

        long productId = 7001L;


        when(
                inventoryRepository
                        .findStockByProductId(productId)
        )
                .thenReturn(
                        Optional.of(5)
                );


        boolean result =
                inventoryService.isAvailable(
                        productId,
                        10
                );


        assertFalse(result);


        verify(inventoryRepository)
                .findStockByProductId(
                        productId
                );
    }


    /*
     * isAvailable() exact quantity
     */
    @Test
    void shouldReturnTrueWhenRequestedQuantityEqualsAvailableStock() {

        long productId = 7001L;


        when(
                inventoryRepository
                        .findStockByProductId(productId)
        )
                .thenReturn(
                        Optional.of(10)
                );


        boolean result =
                inventoryService.isAvailable(
                        productId,
                        10
                );


        assertTrue(result);
    }


    /*
     * isAvailable() invalid quantity
     */
    @Test
    void shouldThrowExceptionWhenCheckingInvalidQuantity() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                inventoryService.isAvailable(
                                        7001L,
                                        0
                                )
                );


        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );


        verifyNoInteractions(
                inventoryRepository
        );
    }


    /*
     * reserveStock()
     *
     * Repository returns number of updated rows.
     * 1 means stock was successfully reserved.
     */
    @Test
    void shouldReserveStock() {

        long productId = 7001L;
        int quantity = 5;


        when(
                inventoryRepository.reserveStock(
                        productId,
                        quantity
                )
        )
                .thenReturn(1);


        inventoryService.reserveStock(
                productId,
                quantity
        );


        verify(inventoryRepository)
                .reserveStock(
                        productId,
                        quantity
                );
    }


    /*
     * reserveStock()
     *
     * 0 affected rows means the conditional
     * stock update failed.
     */
    @Test
    void shouldThrowInsufficientStockExceptionWhenStockCannotBeReserved() {

        long productId = 7001L;
        int quantity = 50;


        when(
                inventoryRepository.reserveStock(
                        productId,
                        quantity
                )
        )
                .thenReturn(0);


        InsufficientStockException exception =
                assertThrows(
                        InsufficientStockException.class,
                        () ->
                                inventoryService.reserveStock(
                                        productId,
                                        quantity
                                )
                );


        assertEquals(
                "Insufficient Stock for product: 7001",
                exception.getMessage()
        );


        verify(inventoryRepository)
                .reserveStock(
                        productId,
                        quantity
                );
    }


    /*
     * reserveStock() invalid quantity
     */
    @Test
    void shouldThrowExceptionWhenReservingInvalidQuantity() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                inventoryService.reserveStock(
                                        7001L,
                                        0
                                )
                );


        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );


        verifyNoInteractions(
                inventoryRepository
        );
    }


    /*
     * releaseStock()
     */
    @Test
    void shouldReleaseStock() {

        long productId = 7001L;
        int quantity = 5;


        inventoryService.releaseStock(
                productId,
                quantity
        );


        verify(inventoryRepository)
                .releaseStock(
                        productId,
                        quantity
                );
    }


    /*
     * releaseStock() invalid quantity
     */
    @Test
    void shouldThrowExceptionWhenReleasingInvalidQuantity() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                inventoryService.releaseStock(
                                        7001L,
                                        0
                                )
                );


        assertEquals(
                "Quantity must be greater than zero",
                exception.getMessage()
        );


        verifyNoInteractions(
                inventoryRepository
        );
    }


    /*
     * setStock()
     */
    @Test
    void shouldSetStock() {

        long productId = 7001L;
        int quantity = 30;


        inventoryService.setStock(
                productId,
                quantity
        );


        verify(inventoryRepository)
                .setStock(
                        productId,
                        quantity
                );
    }


    /*
     * setStock() allows zero
     */
    @Test
    void shouldAllowSettingStockToZero() {

        long productId = 7001L;


        inventoryService.setStock(
                productId,
                0
        );


        verify(inventoryRepository)
                .setStock(
                        productId,
                        0
                );
    }


    /*
     * setStock() negative quantity
     */
    @Test
    void shouldThrowExceptionWhenSettingNegativeStock() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                inventoryService.setStock(
                                        7001L,
                                        -1
                                )
                );


        assertEquals(
                "Quantity cannot be negative",
                exception.getMessage()
        );


        verify(
                inventoryRepository,
                never()
        )
                .setStock(
                        7001L,
                        -1
                );
    }
}