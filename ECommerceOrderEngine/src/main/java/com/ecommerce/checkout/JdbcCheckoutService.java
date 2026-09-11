package com.ecommerce.checkout;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;
import com.ecommerce.payment.PaymentFactory;
import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentResult;
import com.ecommerce.repository.JdbcInventoryRepository;
import com.ecommerce.repository.JdbcOrderRepository;
import com.ecommerce.repository.JdbcPaymentRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public final class JdbcCheckoutService {

    private final JdbcInventoryRepository inventoryRepository;
    private final JdbcOrderRepository orderRepository;
    private final JdbcPaymentRepository paymentRepository;

    /*
     * Maps the requested payment type to a PaymentProcessor.
     * The factory is injectable so tests can provide simulated
     * payment success or failure without changing production code.
     */
    private final Function<PaymentType, PaymentProcessor>
            paymentProcessorFactory;

    public JdbcCheckoutService(
            JdbcInventoryRepository inventoryRepository,
            JdbcOrderRepository orderRepository,
            JdbcPaymentRepository paymentRepository) {

        this(
                inventoryRepository,
                orderRepository,
                paymentRepository,
                paymentType ->
                        new PaymentProcessor(
                                PaymentFactory.create(paymentType)
                        )
        );
    }

    /*
     * Package-private constructor used mainly for testing.
     * It allows a custom payment processor factory to be injected.
     */
    JdbcCheckoutService(
            JdbcInventoryRepository inventoryRepository,
            JdbcOrderRepository orderRepository,
            JdbcPaymentRepository paymentRepository,
            Function<PaymentType, PaymentProcessor>
                    paymentProcessorFactory) {

        if (inventoryRepository == null) {
            throw new IllegalArgumentException(
                    "Inventory repository cannot be null"
            );
        }

        if (orderRepository == null) {
            throw new IllegalArgumentException(
                    "Order repository cannot be null"
            );
        }

        if (paymentRepository == null) {
            throw new IllegalArgumentException(
                    "Payment repository cannot be null"
            );
        }

        if (paymentProcessorFactory == null) {
            throw new IllegalArgumentException(
                    "Payment processor factory cannot be null"
            );
        }

        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentProcessorFactory =
                paymentProcessorFactory;
    }

    /**
     * Processes a complete checkout as one database transaction.
     *
     * Inventory reservation, order persistence, and payment persistence
     * share the same JDBC connection. If any database or business step
     * fails, the transaction is rolled back.
     */
    public Order checkout(
            long orderId,
            Customer customer,
            Cart cart,
            PaymentType paymentType
    ) throws SQLException {

        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order id must be greater than 0"
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart cannot be null or empty"
            );
        }

        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Payment type cannot be null"
            );
        }

        List<OrderItem> orderItems =
                cart.getItems()
                        .stream()
                        .map(item ->
                                new OrderItem(
                                        item.getProduct(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        /*
         * A single connection is shared by all repository operations so
         * MySQL can commit or roll back the entire checkout atomically.
         */
        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                /*
                 * reserveStock() uses an atomic conditional UPDATE,
                 * preventing concurrent checkouts from overselling stock.
                 */
                for (CartItem item : cart.getItems()) {

                    boolean reserved =
                            inventoryRepository.reserveStock(
                                    connection,
                                    item.getProduct().getId(),
                                    item.getQuantity()
                            );

                    if (!reserved) {
                        throw new InsufficientStockException(
                                "Insufficient stock for product "
                                        + item.getProduct().getId()
                        );
                    }
                }

                double total =
                        orderItems.stream()
                                .mapToDouble(
                                        OrderItem::getSubtotal
                                )
                                .sum();

                PaymentProcessor paymentProcessor =
                        paymentProcessorFactory.apply(
                                paymentType
                        );

                if (paymentProcessor == null) {
                    throw new IllegalStateException(
                            "Payment processor factory returned null"
                    );
                }

                PaymentResult paymentResult =
                        paymentProcessor.processPayment(total);

                if (paymentResult.getPaymentType() != paymentType) {
                    throw new IllegalStateException(
                            "Payment result type does not match requested payment type"
                    );
                }

                if (!paymentResult.isSuccessful()) {
                    throw new PaymentFailedException(
                            paymentResult.getMessage()
                    );
                }

                Order order =
                        new Order.Builder()
                                .id(orderId)
                                .customer(customer)
                                .items(orderItems)
                                .paymentType(paymentType)
                                .build();

                order.updateStatus(
                        OrderStatus.PAID
                );

                orderRepository.save(
                        connection,
                        order
                );

                paymentRepository.save(
                        connection,
                        orderId,
                        paymentResult
                );

                connection.commit();

                return order;

            } catch (SQLException | RuntimeException e) {

                /*
                 * Because inventory, order, and payment operations use
                 * the same connection, rollback reverses all uncommitted
                 * database changes, including inventory reservations.
                 */
                connection.rollback();

                throw e;
            }
        }
    }
}