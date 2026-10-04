package com.ecommerce.checkout;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;
import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentResult;
import com.ecommerce.payment.PaymentStrategy;
import com.ecommerce.repository.*;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateCheckoutServiceDemo {

    public static void main(String[] args)
            throws Exception {

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


        testSuccessfulCheckout(
                productRepository,
                customerRepository,
                inventoryRepository,
                orderRepository,
                paymentRepository
        );


        testInsufficientStockRollback(
                productRepository,
                customerRepository,
                inventoryRepository,
                orderRepository,
                paymentRepository
        );


        testPaymentFailureRollback(
                productRepository,
                customerRepository,
                inventoryRepository,
                orderRepository,
                paymentRepository
        );


        HibernateUtil.shutdown();
    }


    private static void testSuccessfulCheckout(
            HibernateProductRepository productRepository,
            HibernateCustomerRepository customerRepository,
            HibernateInventoryRepository inventoryRepository,
            HibernateOrderRepository orderRepository,
            HibernatePaymentRepository paymentRepository
    ) throws Exception {

        System.out.println(
                "\n===== SUCCESSFUL CHECKOUT ====="
        );

        long productId = 960001L;
        long customerId = 960001L;
        long orderId = 960001L;

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
                        "Checkout Product",
                        "Testing",
                        50.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Checkout Customer",
                        "checkout@example.com"
                );

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
         * Deterministic successful payment.
         */
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


        System.out.println(
                "ORDER ID: "
                        + order.getId()
        );

        System.out.println(
                "ORDER STATUS: "
                        + order.getStatus()
        );

        System.out.println(
                "STOCK AFTER CHECKOUT: "
                        + inventoryRepository
                        .getStock(productId)
        );

        System.out.println(
                "ORDER EXISTS: "
                        + orderRepository
                        .findById(orderId)
                        .isPresent()
        );

        System.out.println(
                "PAYMENTS: "
                        + paymentRepository
                        .findByOrderId(orderId)
                        .size()
        );


        cleanup(
                productId,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );
    }


    private static void testInsufficientStockRollback(
            HibernateProductRepository productRepository,
            HibernateCustomerRepository customerRepository,
            HibernateInventoryRepository inventoryRepository,
            HibernateOrderRepository orderRepository,
            HibernatePaymentRepository paymentRepository
    ) throws Exception {

        System.out.println(
                "\n===== INSUFFICIENT STOCK ====="
        );

        long productId = 960002L;
        long customerId = 960002L;
        long orderId = 960002L;

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
                        "Low Stock Product",
                        "Testing",
                        25.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Low Stock Customer",
                        "lowstock@example.com"
                );

        productRepository.save(product);
        customerRepository.save(customer);

        inventoryRepository.setStock(
                productId,
                1
        );


        Cart cart = new Cart();

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


        try {

            checkoutService.checkout(
                    orderId,
                    customer,
                    cart,
                    PaymentType.CARD
            );

            System.out.println(
                    "ERROR: checkout should have failed"
            );

        } catch (InsufficientStockException e) {

            System.out.println(
                    "INSUFFICIENT STOCK DETECTED"
            );
        }


        System.out.println(
                "STOCK AFTER FAILURE: "
                        + inventoryRepository
                        .getStock(productId)
        );

        System.out.println(
                "ORDER EXISTS: "
                        + orderRepository
                        .findById(orderId)
                        .isPresent()
        );

        System.out.println(
                "PAYMENTS: "
                        + paymentRepository
                        .findByOrderId(orderId)
                        .size()
        );


        cleanup(
                productId,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );
    }


    private static void testPaymentFailureRollback(
            HibernateProductRepository productRepository,
            HibernateCustomerRepository customerRepository,
            HibernateInventoryRepository inventoryRepository,
            HibernateOrderRepository orderRepository,
            HibernatePaymentRepository paymentRepository
    ) throws Exception {

        System.out.println(
                "\n===== PAYMENT FAILURE ====="
        );

        long productId = 960003L;
        long customerId = 960003L;
        long orderId = 960003L;

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
                        "paymentfailure@example.com"
                );

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
         * Force payment failure so we can verify
         * inventory rollback.
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


        try {

            checkoutService.checkout(
                    orderId,
                    customer,
                    cart,
                    PaymentType.CARD
            );

            System.out.println(
                    "ERROR: payment should have failed"
            );

        } catch (PaymentFailedException e) {

            System.out.println(
                    "PAYMENT FAILURE DETECTED"
            );
        }


        System.out.println(
                "STOCK AFTER PAYMENT FAILURE: "
                        + inventoryRepository
                        .getStock(productId)
        );

        System.out.println(
                "ORDER EXISTS: "
                        + orderRepository
                        .findById(orderId)
                        .isPresent()
        );

        System.out.println(
                "PAYMENTS: "
                        + paymentRepository
                        .findByOrderId(orderId)
                        .size()
        );


        cleanup(
                productId,
                customerId,
                orderId,
                productRepository,
                customerRepository,
                orderRepository
        );
    }


    private static void cleanup(
            long productId,
            long customerId,
            long orderId,
            HibernateProductRepository productRepository,
            HibernateCustomerRepository customerRepository,
            HibernateOrderRepository orderRepository
    ) throws Exception {

        /*
         * Order deletion also removes order_items and
         * payments through ON DELETE CASCADE.
         */
        orderRepository.deleteById(
                orderId
        );


        /*
         * Remove inventory first because inventory.product_id
         * references products.id.
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