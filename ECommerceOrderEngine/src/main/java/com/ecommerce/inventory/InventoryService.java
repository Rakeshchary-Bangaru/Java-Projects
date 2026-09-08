package com.ecommerce.inventory;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.Product;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages product inventory and provides thread-safe stock operations.
 *
 * <p>Inventory quantities are stored in a {@link ConcurrentHashMap},
 * where each product ID maps to its currently available stock.</p>
 *
 * <p>Stock reservation uses an atomic {@code compute()} operation so the
 * read-check-update sequence is performed safely when multiple threads
 * attempt to reserve the same product concurrently.</p>
 */
public final class InventoryService {

    private final ConcurrentHashMap<Long, Integer> stock;

    /**
     * Creates an empty inventory.
     */
    public InventoryService() {
        this.stock = new ConcurrentHashMap<>();
    }

    /**
     * Adds stock for the specified product.
     *
     * <p>If stock already exists for the product, the supplied quantity
     * is added to the current quantity.</p>
     *
     * @param product product whose stock should be increased
     * @param quantity quantity to add
     * @throws IllegalArgumentException if the product is null
     *                                  or quantity is not greater than zero
     */
    public void addStock(Product product, int quantity) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        stock.merge(
                product.getId(),
                quantity,
                Integer::sum
        );
    }

    /**
     * Returns the currently available stock for a product.
     *
     * <p>If the product is not present in the inventory, zero is returned.</p>
     *
     * @param productId identifier of the product
     * @return currently available stock, or zero if the product is absent
     */
    public int getStock(long productId) {
        return stock.getOrDefault(productId, 0);
    }

    /**
     * Checks whether the requested quantity is currently available.
     *
     * @param productId identifier of the product
     * @param quantity quantity requested
     * @return true if sufficient stock is available, otherwise false
     * @throws IllegalArgumentException if quantity is not greater than zero
     */
    public boolean isAvailable(long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        return getStock(productId) >= quantity;
    }

    /**
     * Atomically reserves the requested quantity for a product.
     *
     * <p>The stock check and stock update are performed together using
     * {@link ConcurrentHashMap#compute(Object, java.util.function.BiFunction)}.
     * This prevents concurrent checkout requests from overselling the
     * same product.</p>
     *
     * @param productId identifier of the product
     * @param quantity quantity to reserve
     * @throws IllegalArgumentException if quantity is not greater than zero
     * @throws InsufficientStockException if sufficient stock is unavailable
     */
    public void reserveStock(long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        /*
         * compute() makes the read-check-update sequence atomic for this key.
         * Competing reservations for the same product therefore cannot both
         * read the same stock value and overwrite each other's updates.
         */
        stock.compute(productId, (id, currentStock) -> {

            int availableStock =
                    currentStock == null ? 0 : currentStock;

            if (availableStock < quantity) {
                throw new InsufficientStockException(
                        "Insufficient Stock for product: " + productId
                );
            }

            return availableStock - quantity;
        });
    }

    /**
     * Returns previously reserved stock back to inventory.
     *
     * <p>If the product already has stock, the released quantity is
     * added atomically to the existing amount.</p>
     *
     * @param productId identifier of the product
     * @param quantity quantity to release
     * @throws IllegalArgumentException if quantity is not greater than zero
     */
    public void releaseStock(long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        stock.merge(
                productId,
                quantity,
                Integer::sum
        );
    }
}