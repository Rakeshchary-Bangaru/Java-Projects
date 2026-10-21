package com.ecommerce.checkout;

import com.ecommerce.EcommerceApplication;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;

import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentProcessorProvider;
import com.ecommerce.payment.PaymentResult;
import com.ecommerce.payment.PaymentStrategy;

import com.ecommerce.repository.SpringDataCustomerRepository;
import com.ecommerce.repository.SpringDataInventoryRepository;
import com.ecommerce.repository.SpringDataOrderRepository;
import com.ecommerce.repository.SpringDataPaymentRepository;
import com.ecommerce.repository.SpringDataProductRepository;

import com.ecommerce.service.SpringInventoryService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = EcommerceApplication.class)
class SpringCheckoutServiceTest {

    @Autowired
    private SpringCheckoutService checkoutService;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataCustomerRepository customerRepository;

    @Autowired
    private SpringDataInventoryRepository inventoryRepository;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private SpringDataPaymentRepository paymentRepository;

    @Autowired
    private SpringInventoryService inventoryService;


    @MockitoBean
    private PaymentProcessorProvider paymentProcessorProvider;


    @Test
    void shouldCompleteCheckoutSuccessfully() {

        long productId = 981501L;
        long customerId = 981501L;
        long orderId = 981501L;

        cleanup(
                productId,
                customerId,
                orderId
        );


        Product product =
                new Product(
                        productId,
                        "Spring Checkout Product",
                        "Testing",
                        50.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Spring Checkout Customer",
                        "spring.checkout@example.com"
                );


        try {

            productRepository.saveAndFlush(
                    product
            );

            customerRepository.saveAndFlush(
                    customer
            );

            inventoryService.setStock(
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
                                double amount) {

                            return new PaymentResult(
                                    true,
                                    PaymentType.CARD,
                                    amount,
                                    "Payment successful"
                            );
                        }
                    };


            when(
                    paymentProcessorProvider.get(
                            PaymentType.CARD
                    )
            ).thenReturn(
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


            assertEquals(
                    3,
                    inventoryService.getStock(
                            productId
                    )
            );


            assertTrue(
                    orderRepository.existsById(
                            orderId
                    )
            );


            assertEquals(
                    1,
                    paymentRepository
                            .findByOrder_IdOrderByIdAsc(
                                    orderId
                            )
                            .size()
            );


            assertTrue(
                    paymentRepository
                            .findTopByOrder_IdOrderByIdDesc(
                                    orderId
                            )
                            .orElseThrow()
                            .isSuccessful()
            );

        } finally {

            cleanup(
                    productId,
                    customerId,
                    orderId
            );
        }
    }


    @Test
    void shouldRollbackWhenStockIsInsufficient() {

        long productId = 981502L;
        long customerId = 981502L;
        long orderId = 981502L;

        cleanup(
                productId,
                customerId,
                orderId
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
                        "spring.stock@example.com"
                );


        try {

            productRepository.saveAndFlush(
                    product
            );

            customerRepository.saveAndFlush(
                    customer
            );

            inventoryService.setStock(
                    productId,
                    1
            );


            Cart cart = new Cart();

            cart.addProduct(
                    product,
                    2
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


            assertEquals(
                    1,
                    inventoryService.getStock(
                            productId
                    )
            );


            assertFalse(
                    orderRepository.existsById(
                            orderId
                    )
            );


            assertTrue(
                    paymentRepository
                            .findByOrder_IdOrderByIdAsc(
                                    orderId
                            )
                            .isEmpty()
            );

        } finally {

            cleanup(
                    productId,
                    customerId,
                    orderId
            );
        }
    }


    @Test
    void shouldRollbackInventoryWhenPaymentFails() {

        long productId = 981503L;
        long customerId = 981503L;
        long orderId = 981503L;

        cleanup(
                productId,
                customerId,
                orderId
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
                        "spring.failure@example.com"
                );


        try {

            productRepository.saveAndFlush(
                    product
            );

            customerRepository.saveAndFlush(
                    customer
            );

            inventoryService.setStock(
                    productId,
                    5
            );


            Cart cart = new Cart();

            cart.addProduct(
                    product,
                    2
            );


            PaymentStrategy failedStrategy =
                    new PaymentStrategy() {

                        @Override
                        public PaymentResult pay(
                                double amount) {

                            return new PaymentResult(
                                    false,
                                    PaymentType.CARD,
                                    amount,
                                    "Payment declined"
                            );
                        }
                    };


            when(
                    paymentProcessorProvider.get(
                            PaymentType.CARD
                    )
            ).thenReturn(
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
             * This is the important @Transactional test.
             *
             * reserveStock changed:
             *
             * 5 -> 3
             *
             * Payment then failed.
             *
             * Spring must roll the entire transaction back:
             *
             * 3 -> 5
             */
            assertEquals(
                    5,
                    inventoryService.getStock(
                            productId
                    )
            );


            assertFalse(
                    orderRepository.existsById(
                            orderId
                    )
            );


            assertTrue(
                    paymentRepository
                            .findByOrder_IdOrderByIdAsc(
                                    orderId
                            )
                            .isEmpty()
            );

        } finally {

            cleanup(
                    productId,
                    customerId,
                    orderId
            );
        }
    }


    private void cleanup(
            long productId,
            long customerId,
            long orderId) {

        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
        }

        if (inventoryRepository.existsById(productId)) {
            inventoryRepository.deleteById(productId);
        }

        if (productRepository.existsById(productId)) {
            productRepository.deleteById(productId);
        }

        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
        }
    }
}