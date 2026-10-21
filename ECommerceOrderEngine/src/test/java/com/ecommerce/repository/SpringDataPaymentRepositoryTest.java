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

        long productId = 990007L;
        long customerId = 990008L;
        long orderId = 990009L;


        Product product =
                new Product(
                        productId,
                        "Payment Test Product",
                        "Electronics",
                        100.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Payment Test Customer",
                        "paymenttest@test.com"
                );


        productRepository.save(product);
        customerRepository.save(customer);


        PaymentType paymentType =
                PaymentType.values()[0];


        OrderItem item =
                new OrderItem(
                        product,
                        2
                );


        Order order =
                new Order.Builder()
                        .id(orderId)
                        .customer(customer)
                        .items(List.of(item))
                        .paymentType(paymentType)
                        .build();


        orderRepository.saveAndFlush(order);


        Payment firstPayment =
                new Payment(
                        order,
                        paymentType,
                        200.00,
                        false,
                        "Payment failed"
                );


        Payment secondPayment =
                new Payment(
                        order,
                        paymentType,
                        200.00,
                        true,
                        "Payment successful"
                );


        Payment savedFirst =
                paymentRepository.saveAndFlush(
                        firstPayment
                );

        Payment savedSecond =
                paymentRepository.saveAndFlush(
                        secondPayment
                );


        assertNotNull(savedFirst.getId());
        assertNotNull(savedSecond.getId());


        // ALL PAYMENT ATTEMPTS
        List<Payment> payments =
                paymentRepository
                        .findByOrder_IdOrderByIdAsc(
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


        // LATEST PAYMENT ATTEMPT
        Payment latest =
                paymentRepository
                        .findTopByOrder_IdOrderByIdDesc(
                                orderId
                        )
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
