
package com.ecommerce.checkout;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;
import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentResult;
import com.ecommerce.payment.PaymentStrategy;
import com.ecommerce.repository.*;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HibernateCheckoutServiceTest {

    /*
     * =========================================================
     * TEST 1 - SUCCESSFUL CHECKOUT
     * =========================================================
     */
    @Test
    void shouldCompleteCheckoutSuccessfully() throws Exception {

        long customerId = 980501L;
        long orderId = 980501L;

        HibernateProductRepository productRepository =
                new HibernateProductRepository();

        HibernateCustomerRepository customerRepository =
                new HibernateCustomerRepository();

        HibernateInventoryRepository inventoryRepository =
                new HibernateInventoryRepository();

        HibernateOrderRepository orderRepository =
                new HibernateOrderRepository();

        HibernatePaymentRepository paymentRepository =
                new HibernatePaymentRepository();

        Long productId = null;

        // Remove leftover order/customer test data.
        cleanup(
                null,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );

        Product product = new Product(
                "Hibernate Checkout Product",
                "Testing",
                new BigDecimal("50.00"),
                "Product for successful checkout testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Hibernate Checkout Customer",
                "hibernate.checkout@example.com"
        );

        try {

            // Hibernate generates the product ID.
            productRepository.save(product);
            productId = product.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.save(customer);

            inventoryRepository.setStock(
                    productId,
                    5
            );

            Cart cart = new Cart();

            cart.addProduct(
                    product,
                    2
            );

            PaymentStrategy successfulStrategy =
                    new PaymentStrategy() {

                        @Override
                        public PaymentResult pay(double amount) {

                            return new PaymentResult(
                                    true,
                                    PaymentType.CARD,
                                    amount,
                                    "Payment successful"
                            );
                        }
                    };

            HibernateCheckoutService checkoutService =
                    new HibernateCheckoutService(
                            inventoryRepository,
                            orderRepository,
                            paymentRepository,
                            type -> new PaymentProcessor(
                                    successfulStrategy
                            )
                    );

            Order order = checkoutService.checkout(
                    orderId,
                    customer,
                    cart,
                    PaymentType.CARD
            );

            assertNotNull(order);

            assertEquals(
                    orderId,
                    order.getId()
            );

            assertEquals(
                    OrderStatus.PAID,
                    order.getStatus()
            );

            assertEquals(
                    100.00,
                    order.calculateTotal(),
                    0.001
            );

            // 5 initial stock - 2 purchased = 3.
            assertEquals(
                    3,
                    inventoryRepository.getStock(productId)
            );

            assertTrue(
                    orderRepository
                            .findById(orderId)
                            .isPresent()
            );

            assertEquals(
                    1,
                    paymentRepository
                            .findByOrderId(orderId)
                            .size()
            );

            assertTrue(
                    paymentRepository
                            .findLatestByOrderId(orderId)
                            .orElseThrow()
                            .isSuccessful()
            );

        } finally {

            cleanup(
                    productId,
                    customerId,
                    orderId,
                    productRepository,
                    customerRepository,
                    orderRepository
            );
        }
    }

    /*
     * =========================================================
     * TEST 2 - INSUFFICIENT STOCK ROLLBACK
     * =========================================================
     */
    @Test
    void shouldRollbackWhenStockIsInsufficient() throws Exception {

        long customerId = 980502L;
        long orderId = 980502L;

        HibernateProductRepository productRepository =
                new HibernateProductRepository();

        HibernateCustomerRepository customerRepository =
                new HibernateCustomerRepository();

        HibernateInventoryRepository inventoryRepository =
                new HibernateInventoryRepository();

        HibernateOrderRepository orderRepository =
                new HibernateOrderRepository();

        HibernatePaymentRepository paymentRepository =
                new HibernatePaymentRepository();

        Long productId = null;

        cleanup(
                null,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );

        Product product = new Product(
                "Insufficient Stock Product",
                "Testing",
                new BigDecimal("25.00"),
                "Product for insufficient stock testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Insufficient Stock Customer",
                "hibernate.stock@example.com"
        );

        try {

            productRepository.save(product);
            productId = product.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.save(customer);

            // Only one item exists.
            inventoryRepository.setStock(
                    productId,
                    1
            );

            Cart cart = new Cart();

            // Checkout requests two items.
            cart.addProduct(
                    product,
                    2
            );

            HibernateCheckoutService checkoutService =
                    new HibernateCheckoutService(
                            inventoryRepository,
                            orderRepository,
                            paymentRepository
                    );

            assertThrows(
                    InsufficientStockException.class,
                    () -> checkoutService.checkout(
                            orderId,
                            customer,
                            cart,
                            PaymentType.CARD
                    )
            );

            // Stock must remain unchanged.
            assertEquals(
                    1,
                    inventoryRepository.getStock(productId)
            );

            // No order should be created.
            assertTrue(
                    orderRepository
                            .findById(orderId)
                            .isEmpty()
            );

            // No payment should be created.
            assertTrue(
                    paymentRepository
                            .findByOrderId(orderId)
                            .isEmpty()
            );

        } finally {

            cleanup(
                    productId,
                    customerId,
                    orderId,
                    productRepository,
                    customerRepository,
                    orderRepository
            );
        }
    }

    /*
     * =========================================================
     * TEST 3 - PAYMENT FAILURE ROLLBACK
     * =========================================================
     */
    @Test
    void shouldRollbackInventoryWhenPaymentFails() throws Exception {

        long customerId = 980503L;
        long orderId = 980503L;

        HibernateProductRepository productRepository =
                new HibernateProductRepository();

        HibernateCustomerRepository customerRepository =
                new HibernateCustomerRepository();

        HibernateInventoryRepository inventoryRepository =
                new HibernateInventoryRepository();

        HibernateOrderRepository orderRepository =
                new HibernateOrderRepository();

        HibernatePaymentRepository paymentRepository =
                new HibernatePaymentRepository();

        Long productId = null;

        cleanup(
                null,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );

        Product product = new Product(
                "Payment Failure Product",
                "Testing",
                new BigDecimal("40.00"),
                "Product for payment rollback testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Payment Failure Customer",
                "hibernate.failure@example.com"
        );

        try {

            productRepository.save(product);
            productId = product.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.save(customer);

            inventoryRepository.setStock(
                    productId,
                    5
            );

            Cart cart = new Cart();

            cart.addProduct(
                    product,
                    2
            );

            // Simulate payment failure.
            PaymentStrategy failedStrategy =
                    new PaymentStrategy() {

                        @Override
                        public PaymentResult pay(double amount) {

                            return new PaymentResult(
                                    false,
                                    PaymentType.CARD,
                                    amount,
                                    "Payment declined"
                            );
                        }
                    };

            HibernateCheckoutService checkoutService =
                    new HibernateCheckoutService(
                            inventoryRepository,
                            orderRepository,
                            paymentRepository,
                            type -> new PaymentProcessor(
                                    failedStrategy
                            )
                    );

            assertThrows(
                    PaymentFailedException.class,
                    () -> checkoutService.checkout(
                            orderId,
                            customer,
                            cart,
                            PaymentType.CARD
                    )
            );

            /*
             * Stock was temporarily reserved:
             *
             * 5 -> 3
             *
             * Payment failed, so the Hibernate
             * transaction must roll back:
             *
             * 3 -> 5
             */
            assertEquals(
                    5,
                    inventoryRepository.getStock(productId)
            );

            assertTrue(
                    orderRepository
                            .findById(orderId)
                            .isEmpty()
            );

            assertTrue(
                    paymentRepository
                            .findByOrderId(orderId)
                            .isEmpty()
            );

        } finally {

            cleanup(
                    productId,
                    customerId,
                    orderId,
                    productRepository,
                    customerRepository,
                    orderRepository
            );
        }
    }

    /*
     * =========================================================
     * CLEANUP
     * =========================================================
     *
     * Deletes records in the correct order:
     *
     * 1. Order (and dependent order items/payments)
     * 2. Inventory
     * 3. Product
     * 4. Customer
     *
     * The product ID is generated by MySQL, so it may
     * be null when product persistence fails.
     */
    private void cleanup(
            Long productId,
            long customerId,
            long orderId,
            HibernateProductRepository productRepository,
            HibernateCustomerRepository customerRepository,
            HibernateOrderRepository orderRepository
    ) throws Exception {

        // Delete order and dependent records.
        orderRepository.deleteById(orderId);

        if (productId != null) {

            Transaction transaction = null;

            try (Session session =
                         HibernateUtil
                                 .getSessionFactory()
                                 .openSession()) {

                transaction = session.beginTransaction();

                session.createMutationQuery(
                                """
                                delete from Inventory i
                                where i.productId = :productId
                                """
                        )
                        .setParameter("productId", productId)
                        .executeUpdate();

                transaction.commit();

            } catch (RuntimeException e) {

                if (transaction != null &&
                        transaction.isActive()) {

                    transaction.rollback();
                }

                throw e;
            }

            productRepository.deleteById(productId);
        }

        customerRepository.deleteById(customerId);
    }
}
