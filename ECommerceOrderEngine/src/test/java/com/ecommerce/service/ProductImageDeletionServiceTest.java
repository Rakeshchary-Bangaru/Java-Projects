
package com.ecommerce.service;

import com.ecommerce.model.ProductImage;
import com.ecommerce.repository.SpringDataProductImageRepository;
import com.ecommerce.storage.ProductImageStorageService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageDeletionServiceTest {

    @Mock
    private SpringDataProductImageRepository imageRepository;

    @Mock
    private ProductImageStorageService storageService;

    private ProductImageDeletionService deletionService;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        deletionService = new ProductImageDeletionService(
                imageRepository,
                storageService
        );

        transactionTemplate = new TransactionTemplate(
                new TestTransactionManager()
        );
    }

    @Test
    void shouldDeleteFilesOnlyAfterTransactionCommits()
            throws IOException {

        long productId = 8802L;

        ProductImage image1 = mock(ProductImage.class);
        ProductImage image2 = mock(ProductImage.class);

        when(image1.getStorageKey())
                .thenReturn("keyboard-1.png");

        when(image2.getStorageKey())
                .thenReturn("keyboard-2.png");

        when(imageRepository
                .findByProduct_IdOrderBySortOrderAscIdAsc(productId))
                .thenReturn(List.of(image1, image2));


        transactionTemplate.executeWithoutResult(status -> {

            deletionService.deleteImagesForProduct(productId);

            verify(imageRepository)
                    .deleteAll(List.of(image1, image2));

            verify(imageRepository).flush();

            // Verify no physical files are deleted before commit.
            assertDoesNotThrow(() ->
                    verify(storageService, never())
                            .delete(anyString())
            );
        });


        // After commit, physical cleanup is permitted.
        verify(storageService).delete("keyboard-1.png");
        verify(storageService).delete("keyboard-2.png");

        verify(storageService, times(2))
                .delete(anyString());
    }

    @Test
    void shouldNotDeletePhysicalFilesWhenTransactionRollsBack()
            throws IOException {

        long productId = 8802L;

        ProductImage image = mock(ProductImage.class);

        when(image.getStorageKey())
                .thenReturn("keyboard.png");

        when(imageRepository
                .findByProduct_IdOrderBySortOrderAscIdAsc(productId))
                .thenReturn(List.of(image));

        assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(
                        status -> {

                            deletionService.deleteImagesForProduct(
                                    productId
                            );

                            throw new IllegalStateException(
                                    "Simulated product deletion failure"
                            );
                        }
                )
        );

        // The metadata deletion was attempted.
        verify(imageRepository).deleteAll(List.of(image));

        // But filesystem deletion must never run.
        verify(storageService, never())
                .delete(anyString());
    }

    @Test
    void shouldHandleProductWithNoImages()
            throws IOException {

        long productId = 7001L;

        when(imageRepository
                .findByProduct_IdOrderBySortOrderAscIdAsc(productId))
                .thenReturn(List.of());

        assertDoesNotThrow(
                () -> transactionTemplate.executeWithoutResult(
                        status ->
                                deletionService.deleteImagesForProduct(
                                        productId
                                )
                )
        );

        verify(imageRepository, never())
                .deleteAll(anyList());

        verify(storageService, never())
                .delete(anyString());
    }

    @Test
    void shouldNotFailCommittedDeletionWhenFileCleanupFails()
            throws IOException {

        long productId = 8802L;

        ProductImage image = mock(ProductImage.class);

        when(image.getStorageKey())
                .thenReturn("missing-keyboard.png");

        when(imageRepository
                .findByProduct_IdOrderBySortOrderAscIdAsc(productId))
                .thenReturn(List.of(image));

        doThrow(new IOException("Filesystem failure"))
                .when(storageService)
                .delete("missing-keyboard.png");

        assertDoesNotThrow(
                () -> transactionTemplate.executeWithoutResult(
                        status ->
                                deletionService.deleteImagesForProduct(
                                        productId
                                )
                )
        );

        verify(imageRepository).deleteAll(List.of(image));
        verify(imageRepository).flush();

        verify(storageService)
                .delete("missing-keyboard.png");
    }

    /*
     * Lightweight transaction manager for testing
     * Spring's commit and rollback callbacks.
     *
     * This does not simulate a real database.
     */
    private static class TestTransactionManager
            extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(
                Object transaction,
                TransactionDefinition definition
        ) {
            // No database resource required.
        }

        @Override
        protected void doCommit(
                DefaultTransactionStatus status
        ) {
            // Spring will invoke afterCommit callbacks.
        }

        @Override
        protected void doRollback(
                DefaultTransactionStatus status
        ) {
            // Spring will invoke rollback completion callbacks.
        }
    }
}
