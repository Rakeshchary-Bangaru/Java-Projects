package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HibernateProductRepositoryTest {

    @Test
    void shouldSaveFindUpdateAndDeleteProduct()
            throws Exception {

        ProductRepository repository =
                new HibernateProductRepository();

        long productId = 980001L;

        /*
         * Cleanup in case a previous interrupted test
         * left the test product behind.
         */
        repository.deleteById(productId);

        Product product =
                new Product(
                        productId,
                        "Hibernate Test Mouse",
                        "Electronics",
                        39.99
                );

        // SAVE
        repository.save(product);

        Optional<Product> saved =
                repository.findById(productId);

        assertTrue(saved.isPresent());

        assertEquals(
                "Hibernate Test Mouse",
                saved.get().getName()
        );

        assertEquals(
                "Electronics",
                saved.get().getCategory()
        );

        assertEquals(
                39.99,
                saved.get().getPrice(),
                0.001
        );


        // UPDATE
        Product updatedProduct =
                new Product(
                        productId,
                        "Hibernate Test Mouse",
                        "Electronics",
                        49.99
                );

        repository.update(
                updatedProduct
        );

        Product updated =
                repository.findById(productId)
                        .orElseThrow();

        assertEquals(
                49.99,
                updated.getPrice(),
                0.001
        );


        // DELETE
        repository.deleteById(
                productId
        );

        assertTrue(
                repository.findById(productId)
                        .isEmpty()
        );
    }
}