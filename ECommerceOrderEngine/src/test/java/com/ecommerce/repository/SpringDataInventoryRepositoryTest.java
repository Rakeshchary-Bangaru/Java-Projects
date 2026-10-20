package com.ecommerce.repository;

import com.ecommerce.EcommerceApplication;
import com.ecommerce.model.Product;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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

        long productId = 990003L;

        Product product =
                new Product(
                        productId,
                        "Inventory Test Product",
                        "Electronics",
                        100.00
                );

        /*
         * Native inventory INSERT needs the Product row
         * to already exist because product_id is also a FK.
         */
        productRepository.saveAndFlush(product);


        // SET STOCK
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


        // ATOMIC RESERVATION
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


        // INSUFFICIENT STOCK
        int failedReservation =
                inventoryRepository.reserveStock(
                        productId,
                        3
                );

        assertEquals(
                0,
                failedReservation
        );

        assertEquals(
                2,
                inventoryRepository
                        .findStockByProductId(productId)
                        .orElseThrow()
        );


        // RELEASE STOCK
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


        // ADD STOCK
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


        // REPLACE STOCK
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