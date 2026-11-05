
package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.model.ProductImage;
import com.ecommerce.repository.SpringDataProductRepository;
import com.ecommerce.repository.SpringDataProductImageRepository;
import com.ecommerce.storage.ProductImageStorageService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductImageMySqlIntegrationTest {

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataProductImageRepository imageRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @TempDir
    Path tempDirectory;

    private final List<Long> testProductIds = new ArrayList<>();

    private TransactionTemplate transactions() {
        return new TransactionTemplate(transactionManager);
    }

    @BeforeEach
    void verifyTestDatabase() {

        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        assertEquals(
                "ecommerce_test_db",
                database,
                "Refusing to run against a non-test database"
        );
    }

    private Product createProduct() {

        Product product = transactions().execute(status ->
                productRepository.saveAndFlush(
                        new Product(
                                "V11 Test " + UUID.randomUUID(),
                                "Electronics",
                                new BigDecimal("2499.00"),
                                "Disposable integration test product",
                                "Test Brand"
                        )
                )
        );

        assertNotNull(product);

        testProductIds.add(product.getId());

        return product;
    }

    private void createImage(
            long productId,
            String storageKey
    ) {

        transactions().executeWithoutResult(status -> {

            Product product = productRepository
                    .findById(productId)
                    .orElseThrow();

            ProductImage image = new ProductImage(
                    product,
                    storageKey,
                    "test-keyboard.png",
                    "image/png",
                    100L,
                    0,
                    true
            );

            imageRepository.saveAndFlush(image);
        });
    }

    private ProductImageDeletionService deletionService(
            ProductImageStorageService storage
    ) {
        return new ProductImageDeletionService(
                imageRepository,
                storage
        );
    }

    @Test
    void shouldDeleteProductImagesAfterCommit()
            throws Exception {

        Product product = createProduct();

        long productId = product.getId();

        String key = UUID.randomUUID() + ".png";

        Path file = tempDirectory.resolve(key);

        Files.write(file, new byte[] {1, 2, 3});

        createImage(productId, key);

        ProductImageStorageService storage =
                mock(ProductImageStorageService.class);

        doAnswer(invocation -> {
            String storageKey = invocation.getArgument(0);

            Files.deleteIfExists(
                    tempDirectory.resolve(storageKey)
            );

            return null;
        }).when(storage).delete(key);

        ProductImageDeletionService service =
                deletionService(storage);

        transactions().executeWithoutResult(status -> {

            service.deleteImagesForProduct(productId);

            assertTrue(
                    Files.exists(file),
                    "File must exist before commit"
            );

            productRepository.deleteById(productId);
            productRepository.flush();
        });

        assertFalse(productRepository.existsById(productId));

        assertFalse(
                imageRepository.existsByProduct_Id(productId)
        );

        assertFalse(Files.exists(file));

        verify(storage).delete(key);
    }

    @Test
    void shouldPreserveImageWhenTransactionRollsBack()
            throws Exception {

        Product product = createProduct();

        long productId = product.getId();

        String key = UUID.randomUUID() + ".png";

        Path file = tempDirectory.resolve(key);

        Files.write(file, new byte[] {4, 5, 6});

        createImage(productId, key);

        ProductImageStorageService storage =
                mock(ProductImageStorageService.class);

        ProductImageDeletionService service =
                deletionService(storage);

        assertThrows(
                IllegalStateException.class,
                () -> transactions().executeWithoutResult(
                        status -> {

                            service.deleteImagesForProduct(
                                    productId
                            );

                            throw new IllegalStateException(
                                    "Simulated rollback"
                            );
                        }
                )
        );

        assertTrue(productRepository.existsById(productId));

        assertTrue(
                imageRepository.existsByProduct_Id(productId)
        );

        assertTrue(Files.exists(file));

        verify(storage, never()).delete(anyString());
    }

    @Test
    void shouldDeleteProductWithoutImages() {

        Product product = createProduct();

        long productId = product.getId();

        ProductImageStorageService storage =
                mock(ProductImageStorageService.class);

        ProductImageDeletionService service =
                deletionService(storage);

        transactions().executeWithoutResult(status -> {

            service.deleteImagesForProduct(productId);

            productRepository.deleteById(productId);
            productRepository.flush();
        });

        assertFalse(productRepository.existsById(productId));

        verifyNoInteractions(storage);
    }

    @AfterEach
    void cleanup() {

        // Protect against incorrect database configuration.
        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        if (!"ecommerce_test_db".equals(database)) {
            throw new IllegalStateException(
                    "Unsafe cleanup database: " + database
            );
        }

        for (Long productId : testProductIds) {

            transactions().executeWithoutResult(status -> {

                List<ProductImage> remaining =
                        imageRepository
                                .findByProduct_IdOrderBySortOrderAscIdAsc(
                                        productId
                                );

                imageRepository.deleteAll(remaining);
                imageRepository.flush();

                if (productRepository.existsById(productId)) {
                    productRepository.deleteById(productId);
                    productRepository.flush();
                }
            });
        }

        testProductIds.clear();
    }
}
