package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC implementation of inventory persistence operations.
 *
 * <p>This repository supports adding stock, reading stock,
 * reserving stock atomically during checkout, releasing stock,
 * and setting an exact inventory quantity.</p>
 */
public final class JdbcInventoryRepository
        implements InventoryRepository {

    /**
     * Adds the specified quantity to the current stock.
     *
     * <p>If no inventory row exists for the product, a new row is
     * created. Otherwise, the existing quantity is increased.</p>
     *
     * @param productId product whose inventory should be increased
     * @param quantity quantity to add
     * @throws IllegalArgumentException if quantity is not positive
     * @throws SQLException if the database operation fails
     */
    @Override
    public void addStock(
            long productId,
            int quantity
    ) throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        /*
         * UPSERT:
         *
         * No inventory row:
         *     create one with the supplied quantity.
         *
         * Existing inventory row:
         *     increase its current quantity.
         */
        String sql = """
                INSERT INTO inventory (product_id, quantity)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    quantity = quantity + ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, productId);
            statement.setInt(2, quantity);
            statement.setInt(3, quantity);

            statement.executeUpdate();
        }
    }

    /**
     * Returns the current stock quantity for a product.
     *
     * <p>If no inventory row exists, zero is returned.</p>
     *
     * @param productId product whose stock should be retrieved
     * @return current stock quantity, or 0 when no inventory row exists
     * @throws SQLException if the database operation fails
     */
    @Override
    public int getStock(long productId)
            throws SQLException {

        String sql = """
                SELECT quantity
                FROM inventory
                WHERE product_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, productId);

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {
                    return resultSet.getInt(
                            "quantity"
                    );
                }

                return 0;
            }
        }
    }

    /**
     * Attempts to reserve inventory using a new database connection.
     *
     * @param productId product whose stock should be reserved
     * @param quantity quantity to reserve
     * @return true when the reservation succeeds, otherwise false
     * @throws SQLException if the database operation fails
     */
    @Override
    public boolean reserveStock(
            long productId,
            int quantity
    ) throws SQLException {

        try (
                Connection connection =
                        DatabaseConnection.getConnection()
        ) {

            return reserveStock(
                    connection,
                    productId,
                    quantity
            );
        }
    }

    /**
     * Atomically reserves stock using a caller-provided connection.
     *
     * <p>The conditional UPDATE prevents overselling because inventory
     * is reduced only when enough stock is still available.</p>
     *
     * <p>This method does not commit, roll back, or close the supplied
     * connection. Transaction ownership remains with the caller,
     * allowing checkout to include inventory, orders, and payments in
     * the same database transaction.</p>
     *
     * @param connection transaction connection owned by the caller
     * @param productId product whose stock should be reserved
     * @param quantity quantity to reserve
     * @return true if stock was reserved, otherwise false
     * @throws IllegalArgumentException if quantity is not positive
     * @throws SQLException if the database operation fails
     */
    public boolean reserveStock(
            Connection connection,
            long productId,
            int quantity
    ) throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        /*
         * This UPDATE performs the availability check and stock
         * reduction in one atomic database operation.
         */
        String sql = """
                UPDATE inventory
                SET quantity = quantity - ?
                WHERE product_id = ?
                  AND quantity >= ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);

            int rowsAffected =
                    statement.executeUpdate();

            /*
             * 1 row affected -> reservation succeeded.
             *
             * 0 rows affected -> inventory row does not exist
             *                    or available stock is insufficient.
             */
            return rowsAffected > 0;
        }
    }

    /**
     * Returns stock to an existing inventory row.
     *
     * @param productId product whose stock should be restored
     * @param quantity quantity to restore
     * @throws IllegalArgumentException if quantity is not positive
     * @throws SQLException if the database operation fails
     */
    @Override
    public void releaseStock(
            long productId,
            int quantity
    ) throws SQLException {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        String sql = """
                UPDATE inventory
                SET quantity = quantity + ?
                WHERE product_id = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, quantity);
            statement.setLong(2, productId);

            statement.executeUpdate();
        }
    }

    /**
     * Replaces the current stock quantity for a product.
     *
     * <p>Unlike {@link #addStock(long, int)}, this method stores an
     * exact quantity rather than incrementing the existing stock.
     * Zero is allowed, but negative quantities are rejected.</p>
     *
     * @param productId product whose inventory should be updated
     * @param quantity exact stock quantity to store
     * @throws IllegalArgumentException if quantity is negative
     * @throws SQLException if the database operation fails
     */

    @Override
    public void setStock(
            long productId,
            int quantity
    ) throws SQLException {

        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative"
            );
        }

        /*
         * UPSERT:
         *
         * No inventory row:
         *     create one with the exact quantity.
         *
         * Existing inventory row:
         *     replace its quantity with the supplied value.
         */
        String sql = """
                INSERT INTO inventory (product_id, quantity)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE
                    quantity = ?
                """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, productId);
            statement.setInt(2, quantity);
            statement.setInt(3, quantity);

            statement.executeUpdate();
        }
    }
}