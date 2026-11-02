
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

import java.math.BigDecimal;

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

    /*
     * =========================================================
     * TEST 1 - SUCCESSFUL CHECKOUT
     * =========================================================
     */
    @Test
    void shouldCompleteCheckoutSuccessfully() {

        long customerId = 981501L;
        long orderId = 981501L;

        Long productId = null;

        cleanup(null, customerId, orderId);

        Product product = new Product(
                "Spring Checkout Product",
                "Testing",
                new BigDecimal("50.00"),
                "Product for successful checkout testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Spring Checkout Customer",
                "spring.checkout@example.com"
        );

        try {

            Product savedProduct =
                    productRepository.saveAndFlush(product);

            productId = savedProduct.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.saveAndFlush(customer);

            inventoryService.setStock(productId, 5);

            Cart cart = new Cart();

            cart.addProduct(savedProduct, 2);

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

            when(
                    paymentProcessorProvider.get(PaymentType.CARD)
            ).thenReturn(
                    new PaymentProcessor(successfulStrategy)
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

            // Initial stock: 5
            // Purchased: 2
            // Remaining: 3
            assertEquals(
                    3,
                    inventoryService.getStock(productId)
            );

            assertTrue(
                    orderRepository.existsById(orderId)
            );

            assertEquals(
                    1,
                    paymentRepository
                            .findByOrder_IdOrderByIdAsc(orderId)
                            .size()
            );

            assertTrue(
                    paymentRepository
                            .findTopByOrder_IdOrderByIdDesc(orderId)
                            .orElseThrow()
                            .isSuccessful()
            );

        } finally {

            cleanup(productId, customerId, orderId);
        }
    }

    /*
     * =========================================================
     * TEST 2 - INSUFFICIENT STOCK ROLLBACK
     * =========================================================
     */
    @Test
    void shouldRollbackWhenStockIsInsufficient() {

        long customerId = 981502L;
        long orderId = 981502L;

        Long productId = null;

        cleanup(null, customerId, orderId);

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
                "spring.stock@example.com"
        );

        try {

            Product savedProduct =
                    productRepository.saveAndFlush(product);

            productId = savedProduct.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.saveAndFlush(customer);

            // Only one item is available.
            inventoryService.setStock(productId, 1);

            Cart cart = new Cart();

            // Customer requests two items.
            cart.addProduct(savedProduct, 2);

            assertThrows(
                    InsufficientStockException.class,
                    () -> checkoutService.checkout(
                            orderId,
                            customer,
                            cart,
                            PaymentType.CARD
                    )
            );

            // Insufficient stock must not reduce inventory.
            assertEquals(
                    1,
                    inventoryService.getStock(productId)
            );

            // No order should be saved.
            assertFalse(
                    orderRepository.existsById(orderId)
            );

            // No payment record should be saved.
            assertTrue(
                    paymentRepository
                            .findByOrder_IdOrderByIdAsc(orderId)
                            .isEmpty()
            );

        } finally {

            cleanup(productId, customerId, orderId);
        }
    }

    /*
     * =========================================================
     * TEST 3 - PAYMENT FAILURE ROLLBACK
     * =========================================================
     */
    @Test
    void shouldRollbackInventoryWhenPaymentFails() {

        long customerId = 981503L;
        long orderId = 981503L;

        Long productId = null;

        cleanup(null, customerId, orderId);

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
                "spring.failure@example.com"
        );

        try {

            Product savedProduct =
                    productRepository.saveAndFlush(product);

            productId = savedProduct.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.saveAndFlush(customer);

            inventoryService.setStock(productId, 5);

            Cart cart = new Cart();

            cart.addProduct(savedProduct, 2);

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

            when(
                    paymentProcessorProvider.get(PaymentType.CARD)
            ).thenReturn(
                    new PaymentProcessor(failedStrategy)
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
             * Transaction rollback verification:
             *
             * Initial stock: 5
             * Reserved stock: 3
             *
             * Payment fails.
             *
             * The entire transaction must roll back.
             * Final stock must remain 5.
             */
            assertEquals(
                    5,
                    inventoryService.getStock(productId)
            );

            // Failed payment must not create an order.
            assertFalse(
                    orderRepository.existsById(orderId)
            );

            // Failed checkout must not retain payment attempts.
            assertTrue(
                    paymentRepository
                            .findByOrder_IdOrderByIdAsc(orderId)
                            .isEmpty()
            );

        } finally {

            cleanup(productId, customerId, orderId);
        }
    }

    /*
     * =========================================================
     * TEST DATA CLEANUP
     * =========================================================
     *
     * Cleanup order:
     *
     * 1. Order and dependent records
     * 2. Inventory
     * 3. Product
     * 4. Customer
     *
     * Product IDs are generated by MySQL.
     *
     * Null productId means that product creation
     * did not complete or has not yet occurred.
     */
    private void cleanup(
            Long productId,
            long customerId,
            long orderId
    ) {

        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
        }

        if (productId != null) {

            if (inventoryRepository.existsById(productId)) {
                inventoryRepository.deleteById(productId);
            }

            if (productRepository.existsById(productId)) {
                productRepository.deleteById(productId);
            }
        }

        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
        }
    }
}
