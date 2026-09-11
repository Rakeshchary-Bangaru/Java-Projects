package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.PaymentType;
import com.ecommerce.payment.PaymentResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcPaymentRepository
        implements PaymentRepository {

    /**
     * Saves a payment using a connection owned by this repository.
     *
     * This standalone version is useful when payment persistence
     * is not part of a larger transaction.
     */
    @Override
    public void save(
            long orderId,
            PaymentResult paymentResult
    ) throws SQLException {

        if (paymentResult == null) {
            throw new IllegalArgumentException(
                    "Payment result cannot be null"
            );
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            save(
                    connection,
                    orderId,
                    paymentResult
            );
        }
    }

    /**
     * Saves a payment using a caller-provided connection.
     *
     * This method does not commit, roll back, or close the connection.
     * JdbcCheckoutService uses this overload so payment persistence
     * participates in the same transaction as inventory and order changes.
     */
    public void save(
            Connection connection,
            long orderId,
            PaymentResult paymentResult
    ) throws SQLException {

        if (paymentResult == null) {
            throw new IllegalArgumentException(
                    "Payment result cannot be null"
            );
        }

        String sql = """
            INSERT INTO payments
            (
                order_id,
                payment_type,
                amount,
                successful,
                message
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    orderId
            );

            statement.setString(
                    2,
                    paymentResult.getPaymentType().name()
            );

            statement.setDouble(
                    3,
                    paymentResult.getAmount()
            );

            statement.setBoolean(
                    4,
                    paymentResult.isSuccessful()
            );

            statement.setString(
                    5,
                    paymentResult.getMessage()
            );

            statement.executeUpdate();
        }
    }

    /**
     * Returns all payment attempts for an order in creation order.
     *
     * Multiple payment records can belong to the same order,
     * allowing payment history to be preserved.
     */
    @Override
    public List<PaymentResult> findByOrderId(long orderId)
            throws SQLException {

        String sql = """
            SELECT payment_type, amount, successful, message
            FROM payments
            WHERE order_id = ?
            ORDER BY id
            """;

        List<PaymentResult> payments =
                new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {
                    payments.add(
                            mapPaymentResult(resultSet)
                    );
                }
            }
        }

        return payments;
    }

    /**
     * Returns the most recent payment attempt for an order.
     *
     * Payments are ordered by descending generated ID so the newest
     * record is selected first.
     */
    @Override
    public Optional<PaymentResult> findLatestByOrderId(
            long orderId
    ) throws SQLException {

        String sql = """
            SELECT payment_type, amount, successful, message
            FROM payments
            WHERE order_id = ?
            ORDER BY id DESC
            LIMIT 1
            """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, orderId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(
                            mapPaymentResult(resultSet)
                    );
                }

                return Optional.empty();
            }
        }
    }

    /*
     * Centralizes ResultSet-to-PaymentResult mapping so query methods
     * do not duplicate the same conversion logic.
     */
    private PaymentResult mapPaymentResult(
            ResultSet resultSet
    ) throws SQLException {

        return new PaymentResult(
                resultSet.getBoolean("successful"),
                PaymentType.valueOf(
                        resultSet.getString("payment_type")
                ),
                resultSet.getDouble("amount"),
                resultSet.getString("message")
        );
    }
}