package com.ecommerce.repository;

import java.sql.SQLException;

/**
 * Defines persistence operations for product inventory.
 *
 * <p>Implementations are responsible for reading and modifying
 * stock quantities while hiding the underlying storage mechanism
 * from the rest of the application.</p>
 */
public interface InventoryRepository {

    /**
     * Adds the specified quantity to the existing stock.
     *
     * @param productId product whose stock should be increased
     * @param quantity quantity to add
     * @throws SQLException if the persistence operation fails
     */
    void addStock(
            long productId,
            int quantity
    ) throws SQLException;

    /**
     * Returns the current stock quantity for a product.
     *
     * @param productId product whose stock should be retrieved
     * @return current stock quantity
     * @throws SQLException if the persistence operation fails
     */
    int getStock(
            long productId
    ) throws SQLException;

    /**
     * Attempts to reserve stock when an order is being processed.
     *
     * <p>The operation succeeds only when enough stock is available.</p>
     *
     * @param productId product whose stock should be reserved
     * @param quantity quantity to reserve
     * @return true if the reservation succeeds, otherwise false
     * @throws SQLException if the persistence operation fails
     */
    boolean reserveStock(
            long productId,
            int quantity
    ) throws SQLException;

    /**
     * Adds previously reserved stock back to inventory.
     *
     * <p>This may be used when reserved stock needs to be restored
     * after a failed or cancelled operation.</p>
     *
     * @param productId product whose stock should be restored
     * @param quantity quantity to restore
     * @throws SQLException if the persistence operation fails
     */
    void releaseStock(
            long productId,
            int quantity
    ) throws SQLException;

    /**
     * Replaces the current stock quantity with an exact value.
     *
     * <p>Unlike {@link #addStock(long, int)}, this operation does not
     * increment the existing stock. A quantity of zero is allowed.</p>
     *
     * @param productId product whose stock should be updated
     * @param quantity exact stock quantity to store
     * @throws SQLException if the persistence operation fails
     */
    void setStock(
            long productId,
            int quantity
    ) throws SQLException;
}