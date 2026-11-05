
package com.ecommerce.service;

import com.ecommerce.model.ProductImage;
import com.ecommerce.repository.SpringDataProductImageRepository;
import com.ecommerce.storage.ProductImageStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.List;

@Service
public class ProductImageDeletionService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    ProductImageDeletionService.class
            );

    private final SpringDataProductImageRepository imageRepository;

    private final ProductImageStorageService storageService;

    public ProductImageDeletionService(
            SpringDataProductImageRepository imageRepository,
            ProductImageStorageService storageService
    ) {
        this.imageRepository = imageRepository;
        this.storageService = storageService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteImagesForProduct(long productId) {

        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be positive"
            );
        }

        List<ProductImage> images =
                imageRepository
                        .findByProduct_IdOrderBySortOrderAscIdAsc(
                                productId
                        );

        if (images.isEmpty()) {
            return;
        }

        List<String> storageKeys = images.stream()
                .map(ProductImage::getStorageKey)
                .toList();

        /*
         * Remove database metadata first.
         * This is part of the product deletion transaction.
         */
        imageRepository.deleteAll(images);
        imageRepository.flush();

        /*
         * Never remove physical files before the database
         * transaction commits.
         */
        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            throw new IllegalStateException(
                    "Transaction synchronization is required"
            );
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {

                        for (String storageKey : storageKeys) {

                            try {
                                storageService.delete(storageKey);

                                log.info(
                                        "Deleted image file for product {}",
                                        productId
                                );

                            } catch (IOException ex) {

                                /*
                                 * Metadata has already committed.
                                 * Log the failure for later reconciliation.
                                 */
                                log.error(
                                        "Unable to delete image file for product {}",
                                        productId,
                                        ex
                                );
                            }
                        }
                    }
                }
        );
    }
}
