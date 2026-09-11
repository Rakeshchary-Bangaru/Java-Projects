package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class JdbcInventoryRepository
        implements InventoryRepository {

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
         * UPSERT behavior:
         * - Insert a new inventory row if one does not exist.
         * - Otherwise increase the existing stock quantity.
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

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return resultSet.getInt("quantity");
                }

                return 0;
            }
        }
    }

    @Override
    public boolean reserveStock(
            long productId,
            int quantity
    ) throws SQLException {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            return reserveStock(
                    connection,
                    productId,
                    quantity
            );
        }
    }

    /**
     * Atomically reserves stock using the caller-provided connection.
     *
     * The conditional UPDATE prevents overselling because stock is
     * reduced only when enough quantity is still available.
     *
     * This overload does not commit, roll back, or close the connection.
     * Transaction ownership remains with the caller.
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

        String sql = """
                UPDATE inventory
                SET quantity = quantity - ?
                WHERE product_id = ?
                  AND quantity >= ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, quantity);
            statement.setLong(2, productId);
            statement.setInt(3, quantity);

            int rowsAffected =
                    statement.executeUpdate();

            /*
             * One affected row means the reservation succeeded.
             * Zero rows means either the product has no inventory row
             * or insufficient stock was available.
             */
            return rowsAffected > 0;
        }
    }

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
}