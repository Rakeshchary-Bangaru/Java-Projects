package com.ecommerce;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.*;
import com.ecommerce.payment.PaymentResult;
import com.ecommerce.repository.*;

import java.time.LocalDateTime;
import java.util.List;

public class HibernatePaymentRepositoryDemo {

    public static void main(String[] args)
            throws Exception {

        ProductRepository productRepository =
                new HibernateProductRepository();

        CustomerRepository customerRepository =
                new HibernateCustomerRepository();

        OrderRepository orderRepository =
                new HibernateOrderRepository();

        PaymentRepository paymentRepository =
                new HibernatePaymentRepository();


        long productId = 950001L;
        long customerId = 950001L;
        long orderId = 950001L;


        /*
         * Clean up previous demo data first.
         * This makes the demo safe to rerun.
         */
        orderRepository.deleteById(orderId);
        productRepository.deleteById(productId);
        customerRepository.deleteById(customerId);


        // ---------------------------------
        // 1. CREATE PRODUCT
        // ---------------------------------

        Product product =
                new Product(
                        productId,
                        "Payment Test Product",
                        "Testing",
                        75.00
                );

        productRepository.save(product);

        System.out.println("PRODUCT CREATED");


        // ---------------------------------
        // 2. CREATE CUSTOMER
        // ---------------------------------

        Customer customer =
                new Customer(
                        customerId,
                        "Payment Test Customer",
                        "payment.customer@example.com"
                );

        customerRepository.save(customer);

        System.out.println("CUSTOMER CREATED");


        // ---------------------------------
        // 3. CREATE ORDER
        // ---------------------------------

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

        System.out.println("ORDER CREATED");


        // ---------------------------------
        // 4. SAVE FAILED PAYMENT ATTEMPT
        // ---------------------------------

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

        System.out.println(
                "FAILED PAYMENT SAVED"
        );


        // ---------------------------------
        // 5. SAVE SUCCESSFUL PAYMENT
        // ---------------------------------

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

        System.out.println(
                "SUCCESSFUL PAYMENT SAVED"
        );


        // ---------------------------------
        // 6. FIND ALL PAYMENT ATTEMPTS
        // ---------------------------------

        List<PaymentResult> payments =
                paymentRepository.findByOrderId(
                        orderId
                );

        System.out.println(
                "PAYMENT ATTEMPTS: "
                        + payments.size()
        );

        for (PaymentResult payment : payments) {

            System.out.println(
                    "PAYMENT: "
                            + payment.getPaymentType()
                            + " | amount="
                            + payment.getAmount()
                            + " | successful="
                            + payment.isSuccessful()
                            + " | message="
                            + payment.getMessage()
            );
        }


        // ---------------------------------
        // 7. FIND LATEST PAYMENT
        // ---------------------------------

        PaymentResult latest =
                paymentRepository
                        .findLatestByOrderId(orderId)
                        .orElseThrow();

        System.out.println(
                "LATEST PAYMENT SUCCESSFUL: "
                        + latest.isSuccessful()
        );

        System.out.println(
                "LATEST PAYMENT MESSAGE: "
                        + latest.getMessage()
        );


        // ---------------------------------
        // 8. DELETE ORDER
        // ---------------------------------

        orderRepository.deleteById(orderId);

        /*
         * payments.order_id uses ON DELETE CASCADE,
         * so deleting the Order should also remove
         * both payment attempts.
         */
        System.out.println(
                "PAYMENTS AFTER ORDER DELETE: "
                        + paymentRepository
                        .findByOrderId(orderId)
                        .size()
        );


        // ---------------------------------
        // 9. CLEAN UP
        // ---------------------------------

        productRepository.deleteById(
                productId
        );

        customerRepository.deleteById(
                customerId
        );

        System.out.println(
                "TEST DATA CLEANED UP"
        );

        HibernateUtil.shutdown();
    }
}