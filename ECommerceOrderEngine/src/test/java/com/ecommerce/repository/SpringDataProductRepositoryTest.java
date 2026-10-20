package com.ecommerce.repository;

import com.ecommerce.EcommerceApplication;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommerceApplication.class)
@Transactional
class SpringDataProductRepositoryTest {

    @Autowired
    private SpringDataProductRepository productRepository;

    @Test
    void shouldPerformProductCrudUsingSpringDataJpa() {

        long productId = 990001L;

        Product product = new Product(
                productId,
                "Spring Data Keyboard",
                "Electronics",
                99.99
        );

        // CREATE
        Product saved =
                productRepository.save(product);

        assertNotNull(saved);
        assertEquals(productId, saved.getId());


        // READ BY ID
        Optional<Product> found =
                productRepository.findById(productId);

        assertTrue(found.isPresent());
        assertEquals(
                "Spring Data Keyboard",
                found.get().getName()
        );


        // READ ALL
        List<Product> products =
                productRepository.findAll();

        assertTrue(
                products.stream()
                        .anyMatch(
                                p -> p.getId() == productId
                        )
        );


        // UPDATE
        Product existing = found.get();

        existing.updatePrice(129.99);

        productRepository.save(existing);

        Product updated =
                productRepository
                        .findById(productId)
                        .orElseThrow();

        assertEquals(
                129.99,
                updated.getPrice(),
                0.001
        );


        // DELETE
        productRepository.deleteById(productId);

        assertFalse(
                productRepository.existsById(productId)
        );
    }
}