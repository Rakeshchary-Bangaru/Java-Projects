
package com.ecommerce.repository;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;

import com.ecommerce.payment.PaymentResult;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HibernatePaymentRepositoryTest {

    @Test
    void shouldSaveAndRetrievePaymentAttempts()
            throws Exception {

        ProductRepository productRepository =
                new HibernateProductRepository();

        CustomerRepository customerRepository =
                new HibernateCustomerRepository();

        OrderRepository orderRepository =
                new HibernateOrderRepository();

        PaymentRepository paymentRepository =
                new HibernatePaymentRepository();

        /*
         * Customer and order IDs remain manually assigned.
         * Only the Product ID is generated automatically.
         */
        long customerId = 980401L;
        long orderId = 980401L;

        Long productId = null;

        /*
         * Clean up leftover order/customer records
         * from a previously interrupted test.
         */
        orderRepository.deleteById(orderId);
        customerRepository.deleteById(customerId);

        /*
         * Create a product without specifying its ID.
         */
        Product product = new Product(
                "Hibernate Payment Product",
                "Testing",
                new BigDecimal("75.00"),
                "Product used for payment repository testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Hibernate Payment Customer",
                "hibernate.payment@example.com"
        );

        try {

            /*
             * =================================================
             * SAVE PRODUCT AND CUSTOMER
             * =================================================
             */
            productRepository.save(product);

            productId = product.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.save(customer);

            /*
             * =================================================
             * CREATE ORDER WITH TWO ITEMS
             * =================================================
             */
            OrderItem item = new OrderItem(
                    product,
                    2
            );

            Order order = Order.restore(
                    orderId,
                    customer,
                    List.of(item),
                    PaymentType.CARD,
                    OrderStatus.values()[0],
                    LocalDateTime.now()
            );

            orderRepository.save(order);

            /*
             * =================================================
             * FIRST PAYMENT ATTEMPT - FAILED
             * =================================================
             */
            PaymentResult failedPayment =
                    new PaymentResult(
                            false,
                            PaymentType.CARD,
                            150.00,
                            "Payment declined"
                    );

            paymentRepository.save(
                    orderId,
                    failedPayment
            );

            /*
             * =================================================
             * SECOND PAYMENT ATTEMPT - SUCCESSFUL
             * =================================================
             */
            PaymentResult successfulPayment =
                    new PaymentResult(
                            true,
                            PaymentType.CARD,
                            150.00,
                            "Payment successful"
                    );

            paymentRepository.save(
                    orderId,
                    successfulPayment
            );

            /*
             * =================================================
             * FIND ALL PAYMENT ATTEMPTS
             * =================================================
             */
            List<PaymentResult> payments =
                    paymentRepository.findByOrderId(orderId);

            assertEquals(
                    2,
                    payments.size()
            );

            PaymentResult first = payments.get(0);

            assertFalse(first.isSuccessful());

            assertEquals(
                    PaymentType.CARD,
                    first.getPaymentType()
            );

            assertEquals(
                    150.00,
                    first.getAmount(),
                    0.001
            );

            assertEquals(
                    "Payment declined",
                    first.getMessage()
            );

            /*
             * =================================================
             * VERIFY SUCCESSFUL PAYMENT
             * =================================================
             */
            PaymentResult second = payments.get(1);

            assertTrue(second.isSuccessful());

            assertEquals(
                    "Payment successful",
                    second.getMessage()
            );

            /*
             * =================================================
             * FIND LATEST PAYMENT
             * =================================================
             */
            Optional<PaymentResult> latest =
                    paymentRepository.findLatestByOrderId(orderId);

            assertTrue(latest.isPresent());

            assertTrue(
                    latest.get().isSuccessful()
            );

            assertEquals(
                    "Payment successful",
                    latest.get().getMessage()
            );

            /*
             * =================================================
             * VERIFY ON DELETE CASCADE
             * =================================================
             *
             * Deleting an order should also delete
             * its payment records.
             */
            boolean deleted =
                    orderRepository.deleteById(orderId);

            assertTrue(deleted);

            assertTrue(
                    paymentRepository
                            .findByOrderId(orderId)
                            .isEmpty()
            );

        } finally {

            /*
             * =================================================
             * CLEANUP
             * =================================================
             *
             * Delete order first because order items
             * reference the product.
             */
            orderRepository.deleteById(orderId);

            /*
             * Delete only the product created
             * by this test.
             */
            if (productId != null) {
                productRepository.deleteById(productId);
            }

            customerRepository.deleteById(customerId);
        }
    }
}
