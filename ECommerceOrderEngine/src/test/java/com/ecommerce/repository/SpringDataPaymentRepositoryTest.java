
package com.ecommerce.repository;

import com.ecommerce.EcommerceApplication;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Payment;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommerceApplication.class)
@Transactional
class SpringDataPaymentRepositoryTest {

    @Autowired
    private SpringDataPaymentRepository paymentRepository;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataCustomerRepository customerRepository;

    @Test
    void shouldSaveAndFindPaymentsByOrder() {

        /*
         * Customer and Order IDs remain manually assigned.
         * Product ID is generated automatically by MySQL.
         */
        long customerId = 990008L;
        long orderId = 990009L;

        /*
         * =====================================================
         * CREATE PRODUCT WITHOUT MANUALLY ASSIGNED ID
         * =====================================================
         */
        Product product = new Product(
                "Payment Test Product",
                "Electronics",
                new BigDecimal("100.00"),
                "Product used for payment repository integration testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Payment Test Customer",
                "paymenttest@test.com"
        );

        /*
         * =====================================================
         * SAVE PRODUCT AND CUSTOMER
         * =====================================================
         */
        Product savedProduct =
                productRepository.saveAndFlush(product);

        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());
        assertTrue(savedProduct.getId() > 0);

        long productId = savedProduct.getId();

        customerRepository.saveAndFlush(customer);

        /*
         * =====================================================
         * CREATE ORDER ITEM
         * =====================================================
         *
         * Use the persisted Product to ensure the OrderItem
         * references the database-generated product ID.
         */
        OrderItem item = new OrderItem(
                savedProduct,
                2
        );

        /*
         * =====================================================
         * CREATE ORDER
         * =====================================================
         */
        PaymentType paymentType =
                PaymentType.values()[0];

        Order order = new Order.Builder()
                .id(orderId)
                .customer(customer)
                .items(List.of(item))
                .paymentType(paymentType)
                .build();

        Order savedOrder =
                orderRepository.saveAndFlush(order);

        assertNotNull(savedOrder);
        assertEquals(orderId, savedOrder.getId());

        assertEquals(
                productId,
                savedProduct.getId()
        );

        /*
         * =====================================================
         * FIRST PAYMENT ATTEMPT - FAILED
         * =====================================================
         */
        Payment firstPayment = new Payment(
                savedOrder,
                paymentType,
                200.00,
                false,
                "Payment failed"
        );

        /*
         * =====================================================
         * SECOND PAYMENT ATTEMPT - SUCCESSFUL
         * =====================================================
         */
        Payment secondPayment = new Payment(
                savedOrder,
                paymentType,
                200.00,
                true,
                "Payment successful"
        );

        /*
         * =====================================================
         * SAVE BOTH PAYMENT ATTEMPTS
         * =====================================================
         */
        Payment savedFirst =
                paymentRepository.saveAndFlush(firstPayment);

        Payment savedSecond =
                paymentRepository.saveAndFlush(secondPayment);

        assertNotNull(savedFirst.getId());
        assertNotNull(savedSecond.getId());

        /*
         * =====================================================
         * FIND ALL PAYMENT ATTEMPTS
         * =====================================================
         */
        List<Payment> payments =
                paymentRepository.findByOrder_IdOrderByIdAsc(
                        orderId
                );

        assertEquals(
                2,
                payments.size()
        );

        assertEquals(
                savedFirst.getId(),
                payments.get(0).getId()
        );

        assertEquals(
                savedSecond.getId(),
                payments.get(1).getId()
        );

        /*
         * =====================================================
         * VERIFY FAILED PAYMENT
         * =====================================================
         */
        Payment first = payments.get(0);

        assertFalse(first.isSuccessful());

        assertEquals(
                "Payment failed",
                first.getMessage()
        );

        /*
         * =====================================================
         * VERIFY SUCCESSFUL PAYMENT
         * =====================================================
         */
        Payment second = payments.get(1);

        assertTrue(second.isSuccessful());

        assertEquals(
                "Payment successful",
                second.getMessage()
        );

        /*
         * =====================================================
         * FIND LATEST PAYMENT ATTEMPT
         * =====================================================
         */
        Payment latest =
                paymentRepository
                        .findTopByOrder_IdOrderByIdDesc(orderId)
                        .orElseThrow();

        assertEquals(
                savedSecond.getId(),
                latest.getId()
        );

        assertTrue(
                latest.isSuccessful()
        );

        assertEquals(
                "Payment successful",
                latest.getMessage()
        );
    }
}
