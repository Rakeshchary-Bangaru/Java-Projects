package com.ecommerce.checkout;

import com.ecommerce.database.HibernateUtil;
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
import com.ecommerce.repository.HibernateInventoryRepository;
import com.ecommerce.repository.HibernateOrderRepository;
import com.ecommerce.repository.HibernatePaymentRepository;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public final class HibernateCheckoutService {

    private final HibernateInventoryRepository inventoryRepository;
    private final HibernateOrderRepository orderRepository;
    private final HibernatePaymentRepository paymentRepository;

    /*
     * Maps the requested payment type to a PaymentProcessor.
     *
     * The factory remains injectable so tests can simulate
     * payment success and payment failure.
     */
    private final Function<PaymentType, PaymentProcessor>
            paymentProcessorFactory;


    public HibernateCheckoutService(
            HibernateInventoryRepository inventoryRepository,
            HibernateOrderRepository orderRepository,
            HibernatePaymentRepository paymentRepository
    ) {

        this(
                inventoryRepository,
                orderRepository,
                paymentRepository,
                paymentType ->
                        new PaymentProcessor(
                                PaymentFactory.create(
                                        paymentType
                                )
                        )
        );
    }


    /*
     * Package-private constructor primarily for testing.
     *
     * Tests can inject a custom PaymentProcessor factory
     * without changing production behavior.
     */
    HibernateCheckoutService(
            HibernateInventoryRepository inventoryRepository,
            HibernateOrderRepository orderRepository,
            HibernatePaymentRepository paymentRepository,
            Function<PaymentType, PaymentProcessor>
                    paymentProcessorFactory
    ) {

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

        this.inventoryRepository =
                inventoryRepository;

        this.orderRepository =
                orderRepository;

        this.paymentRepository =
                paymentRepository;

        this.paymentProcessorFactory =
                paymentProcessorFactory;
    }


    /**
     * Processes the complete checkout inside one Hibernate transaction.
     *
     * Inventory reservation, order persistence, order-item persistence,
     * and payment persistence share the same Session and Transaction.
     *
     * If any step fails, the complete transaction is rolled back.
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


        /*
         * Convert CartItems into OrderItems.
         *
         * Each OrderItem captures the product price at checkout time.
         */
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


        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            Transaction transaction =
                    session.beginTransaction();

            try {

                /*
                 * Reserve all stock using the SAME Session.
                 *
                 * reserveStock() uses one atomic conditional UPDATE:
                 *
                 * quantity = quantity - requested
                 * WHERE quantity >= requested
                 *
                 * This preserves the concurrency protection from V3.
                 */
                for (CartItem item : cart.getItems()) {

                    boolean reserved =
                            inventoryRepository.reserveStock(
                                    session,
                                    item.getProduct().getId(),
                                    item.getQuantity()
                            );

                    if (!reserved) {

                        throw new InsufficientStockException(
                                "Insufficient stock for product "
                                        + item
                                        .getProduct()
                                        .getId()
                        );
                    }
                }


                /*
                 * Calculate the checkout total from historical
                 * OrderItem unit prices.
                 */
                double total =
                        orderItems.stream()
                                .mapToDouble(
                                        OrderItem::getSubtotal
                                )
                                .sum();


                /*
                 * Select the requested payment strategy.
                 */
                PaymentProcessor paymentProcessor =
                        paymentProcessorFactory.apply(
                                paymentType
                        );

                if (paymentProcessor == null) {
                    throw new IllegalStateException(
                            "Payment processor factory returned null"
                    );
                }


                /*
                 * Process payment.
                 */
                PaymentResult paymentResult =
                        paymentProcessor.processPayment(
                                total
                        );


                if (paymentResult.getPaymentType()
                        != paymentType) {

                    throw new IllegalStateException(
                            "Payment result type does not match requested payment type"
                    );
                }


                /*
                 * Preserve the behavior of JdbcCheckoutService:
                 *
                 * failed payment -> throw exception
                 *                  -> rollback transaction
                 *
                 * Therefore reserved inventory is automatically restored.
                 */
                if (!paymentResult.isSuccessful()) {

                    throw new PaymentFailedException(
                            paymentResult.getMessage()
                    );
                }


                /*
                 * Build the completed Order.
                 */
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


                /*
                 * Save Order + OrderItems using the SAME Session.
                 *
                 * HibernateOrderRepository uses merge() because
                 * Customer/Product objects may be detached.
                 */
                orderRepository.save(
                        session,
                        order
                );


                /*
                 * Save successful payment using the SAME Session.
                 */
                paymentRepository.save(
                        session,
                        orderId,
                        paymentResult
                );


                /*
                 * Hibernate flushes pending SQL and permanently commits
                 * inventory, order, items, and payment together.
                 */
                transaction.commit();

                return order;

            } catch (SQLException | RuntimeException e) {

                /*
                 * One rollback reverses every uncommitted operation:
                 *
                 * - inventory reservations
                 * - order insert
                 * - order-item inserts
                 * - payment insert
                 */
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw e;
            }
        }
    }
}