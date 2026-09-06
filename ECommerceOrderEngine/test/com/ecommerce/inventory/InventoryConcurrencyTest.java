package com.ecommerce.inventory;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency tests for {@link InventoryService}.
 *
 * <p>Verifies that inventory reservations remain correct when many
 * threads attempt to reserve the same product concurrently.</p>
 */
class InventoryConcurrencyTest {

    /**
     * Verifies that concurrent reservation attempts cannot oversell
     * the available inventory.
     *
     * <p>The test creates 100 customer requests competing for only
     * 50 units of stock. Exactly 50 reservations should succeed.</p>
     */
    @Test
    void shouldNotOversellProduct() throws InterruptedException {

        InventoryService inventory = new InventoryService();

        Product product = new Product(
                1,
                "iPhone",
                "Electronics",
                80000
        );

        inventory.addStock(product, 50);

        int numberOfCustomers = 100;

        ExecutorService executor =
                Executors.newFixedThreadPool(20);

        /*
         * Worker threads wait at this gate so reservation attempts begin
         * at approximately the same time, increasing contention on stock.
         */
        CountDownLatch startGate =
                new CountDownLatch(1);

        /*
         * Multiple threads update this counter, so AtomicInteger is used
         * to record successful reservations safely.
         */
        AtomicInteger successfulReservations =
                new AtomicInteger();

        for (int i = 0; i < numberOfCustomers; i++) {

            executor.submit(() -> {

                try {

                    startGate.await();

                    inventory.reserveStock(
                            product.getId(),
                            1
                    );

                    successfulReservations.incrementAndGet();

                } catch (InsufficientStockException e) {

                    // Expected once all 50 available units have been reserved.

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                }
            });
        }

        // Release all waiting threads so they begin competing for stock.
        startGate.countDown();

        executor.shutdown();

        assertTrue(
                executor.awaitTermination(
                        10,
                        TimeUnit.SECONDS
                )
        );

        assertEquals(
                50,
                successfulReservations.get()
        );

        assertEquals(
                0,
                inventory.getStock(product.getId())
        );
    }
}