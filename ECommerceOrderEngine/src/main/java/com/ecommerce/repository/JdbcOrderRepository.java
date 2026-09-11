package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcOrderRepository
        implements OrderRepository {

    /**
     * Saves an order using a transaction owned by this repository.
     *
     * Both the order header and its order items must succeed together.
     * If either insert fails, the whole transaction is rolled back.
     */
    @Override
    public void save(Order order) throws SQLException {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                save(connection, order);

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                connection.rollback();

                throw e;
            }
        }
    }

    /**
     * Saves an order using a caller-provided connection.
     *
     * This method does not commit, roll back, or close the connection.
     * The caller owns the transaction boundary.
     *
     * JdbcCheckoutService uses this overload so inventory, order,
     * order items, and payment can participate in one transaction.
     */
    public void save(
            Connection connection,
            Order order
    ) throws SQLException {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        String orderSql = """
            INSERT INTO orders
            (id, customer_id, status, payment_type, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;

        String orderItemSql = """
            INSERT INTO order_items
            (order_id, product_id, quantity, unit_price)
            VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement orderStatement =
                     connection.prepareStatement(orderSql)) {

            orderStatement.setLong(
                    1,
                    order.getId()
            );

            orderStatement.setLong(
                    2,
                    order.getCustomer().getId()
            );

            orderStatement.setString(
                    3,
                    order.getStatus().name()
            );

            orderStatement.setString(
                    4,
                    order.getPaymentType().name()
            );

            orderStatement.setTimestamp(
                    5,
                    Timestamp.valueOf(
                            order.getCreatedAt()
                    )
            );

            orderStatement.executeUpdate();
        }

        /*
         * Order items are inserted as a batch because they belong to
         * the same order and can be sent to the database together.
         */
        try (PreparedStatement itemStatement =
                     connection.prepareStatement(orderItemSql)) {

            for (OrderItem item : order.getItems()) {

                itemStatement.setLong(
                        1,
                        order.getId()
                );

                itemStatement.setLong(
                        2,
                        item.getProduct().getId()
                );

                itemStatement.setInt(
                        3,
                        item.getQuantity()
                );

                itemStatement.setDouble(
                        4,
                        item.getUnitPrice()
                );

                itemStatement.addBatch();
            }

            itemStatement.executeBatch();
        }
    }

    /**
     * Reconstructs an Order from persisted database state.
     *
     * The order/customer data and order-item/product data are loaded
     * separately, then combined into the domain Order object.
     */
    @Override
    public Optional<Order> findById(long id)
            throws SQLException {

        String orderSql = """
            SELECT
                o.id,
                o.status,
                o.payment_type,
                o.created_at,
                c.id AS customer_id,
                c.name AS customer_name,
                c.email AS customer_email
            FROM orders o
            JOIN customers c
                ON o.customer_id = c.id
            WHERE o.id = ?
            """;

        String itemSql = """
            SELECT
                p.id AS product_id,
                p.name AS product_name,
                p.category,
                oi.quantity,
                oi.unit_price
            FROM order_items oi
            JOIN products p
                ON oi.product_id = p.id
            WHERE oi.order_id = ?
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            Customer customer;
            OrderStatus status;
            PaymentType paymentType;
            LocalDateTime createdAt;

            try (PreparedStatement statement =
                         connection.prepareStatement(orderSql)) {

                statement.setLong(1, id);

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    if (!resultSet.next()) {
                        return Optional.empty();
                    }

                    customer = new Customer(
                            resultSet.getLong("customer_id"),
                            resultSet.getString("customer_name"),
                            resultSet.getString("customer_email")
                    );

                    status = OrderStatus.valueOf(
                            resultSet.getString("status")
                    );

                    paymentType = PaymentType.valueOf(
                            resultSet.getString("payment_type")
                    );

                    createdAt =
                            resultSet.getTimestamp("created_at")
                                    .toLocalDateTime();
                }
            }

            List<OrderItem> items =
                    new ArrayList<>();

            try (PreparedStatement statement =
                         connection.prepareStatement(itemSql)) {

                statement.setLong(1, id);

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    while (resultSet.next()) {

                        /*
                         * unit_price is used instead of the product's current
                         * price so historical orders preserve the price that
                         * was actually paid at checkout time.
                         */
                        Product product = new Product(
                                resultSet.getLong("product_id"),
                                resultSet.getString("product_name"),
                                resultSet.getString("category"),
                                resultSet.getDouble("unit_price")
                        );

                        OrderItem item = new OrderItem(
                                product,
                                resultSet.getInt("quantity")
                        );

                        items.add(item);
                    }
                }
            }

            /*
             * restore() preserves the persisted status and createdAt value.
             * A normal Builder would create a new order with fresh state.
             */
            Order order = Order.restore(
                    id,
                    customer,
                    items,
                    paymentType,
                    status,
                    createdAt
            );

            return Optional.of(order);
        }
    }

    @Override
    public List<Order> findAll()
            throws SQLException {

        String sql = """
            SELECT id
            FROM orders
            ORDER BY id
            """;

        List<Long> orderIds = new ArrayList<>();

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {
                orderIds.add(
                        resultSet.getLong("id")
                );
            }
        }

        List<Order> orders = new ArrayList<>();

        /*
         * Simple V2 implementation:
         * each order ID is reconstructed through findById().
         *
         * This causes multiple queries for many orders and can be
         * optimized in a later version.
         */
        for (Long orderId : orderIds) {

            Optional<Order> order =
                    findById(orderId);

            order.ifPresent(orders::add);
        }

        return orders;
    }

    @Override
    public void updateStatus(
            long orderId,
            OrderStatus status
    ) throws SQLException {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        String sql = """
            UPDATE orders
            SET status = ?
            WHERE id = ?
            """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    status.name()
            );

            statement.setLong(
                    2,
                    orderId
            );

            statement.executeUpdate();
        }
    }

    @Override
    public boolean deleteById(long id)
            throws SQLException {

        String sql = """
            DELETE FROM orders
            WHERE id = ?
            """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            /*
             * order_items and payments are automatically removed by
             * ON DELETE CASCADE constraints in the database schema.
             */
            int rowsAffected =
                    statement.executeUpdate();

            return rowsAffected > 0;
        }
    }
}