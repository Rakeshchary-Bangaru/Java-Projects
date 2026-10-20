package com.ecommerce.service;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.SpringDataInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpringInventoryService {

    private final SpringDataInventoryRepository inventoryRepository;

    public SpringInventoryService(
            SpringDataInventoryRepository inventoryRepository) {

        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public void addStock(
            long productId,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        inventoryRepository.addStock(
                productId,
                quantity
        );
    }

    @Transactional(readOnly = true)
    public int getStock(long productId) {

        return inventoryRepository
                .findStockByProductId(productId)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public boolean isAvailable(
            long productId,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        return getStock(productId) >= quantity;
    }

    @Transactional
    public void reserveStock(
            long productId,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        int rowsAffected =
                inventoryRepository.reserveStock(
                        productId,
                        quantity
                );

        if (rowsAffected == 0) {
            throw new InsufficientStockException(
                    "Insufficient Stock for product: "
                            + productId
            );
        }
    }

    @Transactional
    public void releaseStock(
            long productId,
            int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        inventoryRepository.releaseStock(
                productId,
                quantity
        );
    }

    @Transactional
    public void setStock(
            long productId,
            int quantity) {

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative"
            );
        }

        inventoryRepository.setStock(
                productId,
                quantity
        );
    }
}