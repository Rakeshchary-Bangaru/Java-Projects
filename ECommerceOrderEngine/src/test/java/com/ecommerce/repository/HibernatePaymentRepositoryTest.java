package com.ecommerce.repository;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;
import com.ecommerce.payment.PaymentResult;

import org.junit.jupiter.api.Test;

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


        long productId = 980401L;
        long customerId = 980401L;
        long orderId = 980401L;


        /*
         * Cleanup data from a previously interrupted test.
         */
        orderRepository.deleteById(orderId);
        productRepository.deleteById(productId);
        customerRepository.deleteById(customerId);


        Product product =
                new Product(
                        productId,
                        "Hibernate Payment Product",
                        "Testing",
                        75.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Hibernate Payment Customer",
                        "hibernate.payment@example.com"
                );


        try {

            productRepository.save(product);
            customerRepository.save(customer);


            OrderItem item =
                    new OrderItem(
                            product,
                            2
                    );


            Order order =
                    Order.restore(
                            orderId,
                            customer,
                            List.of(item),
                            PaymentType.CARD,
                            OrderStatus.values()[0],
                            LocalDateTime.now()
                    );


            orderRepository.save(order);


            /*
             * First payment attempt fails.
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
             * Second attempt succeeds.
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


            // FIND ALL PAYMENTS
            List<PaymentResult> payments =
                    paymentRepository.findByOrderId(
                            orderId
                    );


            assertEquals(
                    2,
                    payments.size()
            );


            PaymentResult first =
                    payments.get(0);

            assertFalse(
                    first.isSuccessful()
            );

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


            PaymentResult second =
                    payments.get(1);

            assertTrue(
                    second.isSuccessful()
            );

            assertEquals(
                    "Payment successful",
                    second.getMessage()
            );


            // FIND LATEST PAYMENT
            Optional<PaymentResult> latest =
                    paymentRepository
                            .findLatestByOrderId(
                                    orderId
                            );


            assertTrue(
                    latest.isPresent()
            );

            assertTrue(
                    latest.get()
                            .isSuccessful()
            );

            assertEquals(
                    "Payment successful",
                    latest.get()
                            .getMessage()
            );


            /*
             * Deleting the Order should also delete
             * its Payment rows through ON DELETE CASCADE.
             */
            orderRepository.deleteById(
                    orderId
            );


            assertTrue(
                    paymentRepository
                            .findByOrderId(orderId)
                            .isEmpty()
            );


        } finally {

            /*
             * Safe cleanup even if an assertion fails.
             */
            orderRepository.deleteById(
                    orderId
            );

            productRepository.deleteById(
                    productId
            );

            customerRepository.deleteById(
                    customerId
            );
        }
    }
}