package com.ecommerce.checkout;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.inventory.InventoryService;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;
import com.ecommerce.notification.OrderEventPublisher;
import com.ecommerce.payment.PaymentFactory;
import com.ecommerce.payment.PaymentProcessor;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency tests for the complete checkout workflow.
 *
 * <p>Verifies that multiple concurrent checkout requests cannot
 * oversell limited inventory.</p>
 */
class CheckoutConcurrencyTest {

    /**
     * Verifies that 100 concurrent checkout attempts competing for
     * 50 units of stock result in exactly 50 successful checkouts
     * and 50 rejected checkouts.
     */
    @Test
    void shouldNotOversellDuringConcurrentCheckouts()
            throws InterruptedException {

        Product product =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        80000
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(product, 50);

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(
                                PaymentType.CARD
                        )
                );

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        int numberOfCustomers = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        /*
         * Worker threads wait at this gate so the checkout attempts
         * begin at approximately the same time and create contention
         * for the same inventory item.
         */
        CountDownLatch startGate =
                new CountDownLatch(1);

        /*
         * AtomicInteger is used because multiple worker threads update
         * these counters concurrently.
         */
        AtomicInteger successfulCheckouts =
                new AtomicInteger();

        AtomicInteger failedCheckouts =
                new AtomicInteger();

        for (int i = 0; i < numberOfCustomers; i++) {

            final long orderId = i + 1L;

            executor.submit(() -> {

                // Each simulated customer gets an independent cart.
                Cart cart = new Cart();
                cart.addProduct(product, 1);

                try {

                    startGate.await();

                    checkoutService.checkout(
                            orderId,
                            customer,
                            cart
                    );

                    successfulCheckouts.incrementAndGet();

                } catch (InsufficientStockException e) {

                    // Expected after all 50 available units are reserved.
                    failedCheckouts.incrementAndGet();

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }
            });
        }

        // Release all waiting checkout tasks at the same time.
        startGate.countDown();

        executor.shutdown();

        assertTrue(
                executor.awaitTermination(
                        10,
                        TimeUnit.SECONDS
                )
        );

        assertAll(
                () -> assertEquals(
                        50,
                        successfulCheckouts.get()
                ),

                () -> assertEquals(
                        50,
                        failedCheckouts.get()
                ),

                () -> assertEquals(
                        0,
                        inventoryService.getStock(
                                product.getId()
                        )
                )
        );
    }
}