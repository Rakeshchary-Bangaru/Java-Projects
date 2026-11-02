
package com.ecommerce.repository;

import com.ecommerce.EcommerceApplication;
import com.ecommerce.model.Product;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommerceApplication.class)
@Transactional
class SpringDataInventoryRepositoryTest {

    @Autowired
    private SpringDataInventoryRepository inventoryRepository;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Test
    void shouldPerformInventoryOperationsUsingSpringDataJpa() {

        /*
         * =====================================================
         * CREATE PRODUCT
         * =====================================================
         *
         * Product ID is generated automatically by MySQL.
         * Do not manually assign an ID.
         */
        Product product = new Product(
                "Inventory Test Product",
                "Electronics",
                new BigDecimal("100.00"),
                "Product used for Spring Data inventory testing",
                "TestBrand"
        );

        /*
         * =====================================================
         * SAVE PRODUCT
         * =====================================================
         *
         * The inventory table references products.id.
         * The Product row must exist before stock is inserted.
         */
        Product savedProduct =
                productRepository.saveAndFlush(product);

        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());
        assertTrue(savedProduct.getId() > 0);

        long productId = savedProduct.getId();

        /*
         * =====================================================
         * SET INITIAL STOCK
         * =====================================================
         */
        inventoryRepository.setStock(
                productId,
                5
        );

        assertEquals(
                5,
                inventoryRepository
                        .findStockByProductId(productId)
                        .orElseThrow()
        );

        /*
         * =====================================================
         * SUCCESSFUL ATOMIC RESERVATION
         * =====================================================
         *
         * Initial stock = 5
         * Reserved quantity = 3
         * Remaining stock = 2
         */
        int reserved =
                inventoryRepository.reserveStock(
                        productId,
                        3
                );

        assertEquals(1, reserved);

        assertEquals(
                2,
                inventoryRepository
                        .findStockByProductId(productId)
                        .orElseThrow()
        );

        /*
         * =====================================================
         * INSUFFICIENT STOCK
         * =====================================================
         *
         * Only 2 items remain.
         * Attempting to reserve 3 must fail.
         */
        int failedReservation =
                inventoryRepository.reserveStock(
                        productId,
                        3
                );

        assertEquals(
                0,
                failedReservation
        );

        /*
         * Failed reservation must not change the inventory.
         */
        assertEquals(
                2,
                inventoryRepository
                        .findStockByProductId(productId)
                        .orElseThrow()
        );

        /*
         * =====================================================
         * RELEASE STOCK
         * =====================================================
         *
         * Current stock = 2
         * Released quantity = 2
         * New stock = 4
         */
        inventoryRepository.releaseStock(
                productId,
                2
        );

        assertEquals(
                4,
                inventoryRepository
                        .findStockByProductId(productId)
                        .orElseThrow()
        );

        /*
         * =====================================================
         * ADD STOCK
         * =====================================================
         *
         * Current stock = 4
         * Added quantity = 6
         * New stock = 10
         */
        inventoryRepository.addStock(
                productId,
                6
        );

        assertEquals(
                10,
                inventoryRepository
                        .findStockByProductId(productId)
                        .orElseThrow()
        );

        /*
         * =====================================================
         * REPLACE STOCK
         * =====================================================
         *
         * Replace current quantity with 7.
         */
        inventoryRepository.setStock(
                productId,
                7
        );

        assertEquals(
                7,
                inventoryRepository
                        .findStockByProductId(productId)
                        .orElseThrow()
        );
    }
}
