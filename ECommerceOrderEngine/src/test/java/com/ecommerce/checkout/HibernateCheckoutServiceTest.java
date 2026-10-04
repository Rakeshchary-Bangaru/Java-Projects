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

import static org.junit.jupiter.api.Assertions.*;

class HibernateCheckoutServiceTest {


    @Test
    void shouldCompleteCheckoutSuccessfully()
            throws Exception {

        long productId = 980501L;
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


        cleanup(
                productId,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );


        Product product =
                new Product(
                        productId,
                        "Hibernate Checkout Product",
                        "Testing",
                        50.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Hibernate Checkout Customer",
                        "hibernate.checkout@example.com"
                );


        try {

            productRepository.save(product);
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
                        public PaymentResult pay(
                                double amount
                        ) {

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
                            type ->
                                    new PaymentProcessor(
                                            successfulStrategy
                                    )
                    );


            Order order =
                    checkoutService.checkout(
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


            /*
             * 5 initial stock - 2 purchased = 3.
             */
            assertEquals(
                    3,
                    inventoryRepository.getStock(
                            productId
                    )
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


    @Test
    void shouldRollbackWhenStockIsInsufficient()
            throws Exception {

        long productId = 980502L;
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


        cleanup(
                productId,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );


        Product product =
                new Product(
                        productId,
                        "Insufficient Stock Product",
                        "Testing",
                        25.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Insufficient Stock Customer",
                        "hibernate.stock@example.com"
                );


        try {

            productRepository.save(product);
            customerRepository.save(customer);

            /*
             * Only one item exists.
             */
            inventoryRepository.setStock(
                    productId,
                    1
            );


            Cart cart = new Cart();

            /*
             * Checkout requests two.
             */
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
                    () ->
                            checkoutService.checkout(
                                    orderId,
                                    customer,
                                    cart,
                                    PaymentType.CARD
                            )
            );


            /*
             * Failed reservation must not reduce stock.
             */
            assertEquals(
                    1,
                    inventoryRepository.getStock(
                            productId
                    )
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


    @Test
    void shouldRollbackInventoryWhenPaymentFails()
            throws Exception {

        long productId = 980503L;
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


        cleanup(
                productId,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );


        Product product =
                new Product(
                        productId,
                        "Payment Failure Product",
                        "Testing",
                        40.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Payment Failure Customer",
                        "hibernate.failure@example.com"
                );


        try {

            productRepository.save(product);
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


            /*
             * Force the payment processor to return failure.
             */
            PaymentStrategy failedStrategy =
                    new PaymentStrategy() {

                        @Override
                        public PaymentResult pay(
                                double amount
                        ) {

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
                            type ->
                                    new PaymentProcessor(
                                            failedStrategy
                                    )
                    );


            assertThrows(
                    PaymentFailedException.class,
                    () ->
                            checkoutService.checkout(
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
             * but payment failed, therefore the Hibernate
             * transaction must roll it back:
             *
             * 3 -> 5
             */
            assertEquals(
                    5,
                    inventoryRepository.getStock(
                            productId
                    )
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


    private void cleanup(
            long productId,
            long customerId,
            long orderId,
            HibernateProductRepository productRepository,
            HibernateCustomerRepository customerRepository,
            HibernateOrderRepository orderRepository
    ) throws Exception {

        /*
         * Order deletion also removes order_items and
         * payments because of ON DELETE CASCADE.
         */
        orderRepository.deleteById(
                orderId
        );


        /*
         * Inventory references Product, so remove
         * the inventory row before deleting Product.
         */
        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.createMutationQuery(
                            """
                            delete from Inventory i
                            where i.productId = :productId
                            """
                    )
                    .setParameter(
                            "productId",
                            productId
                    )
                    .executeUpdate();

            transaction.commit();

        } catch (RuntimeException e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw e;
        }


        productRepository.deleteById(
                productId
        );

        customerRepository.deleteById(
                customerId
        );
    }
}