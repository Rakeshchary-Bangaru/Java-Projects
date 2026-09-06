package com.ecommerce.integration;

import com.ecommerce.analytics.AnalyticsObserver;
import com.ecommerce.analytics.AnalyticsService;
import com.ecommerce.checkout.CheckoutService;
import com.ecommerce.inventory.InventoryService;
import com.ecommerce.model.*;
import com.ecommerce.notification.EmailNotificationObserver;
import com.ecommerce.notification.OrderEventPublisher;
import com.ecommerce.payment.PaymentFactory;
import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.persistence.OrderFileRepository;
import com.ecommerce.persistence.OrderSummary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test for the Core Java e-commerce order engine.
 *
 * <p>This test verifies that the major application modules work together
 * correctly, including inventory management, cart processing, payment,
 * checkout, observer notifications, analytics, and file persistence.</p>
 */
class ECommerceOrderEngineIntegrationTest {

    /**
     * Temporary directory provided by JUnit so persistence tests do not
     * create permanent files on the local machine.
     */
    @TempDir
    Path tempDir;

    /**
     * Verifies the complete successful order-processing workflow
     * from product creation through persistence and read-back.
     */
    @Test
    void shouldCompleteFullECommerceOrderFlow()
            throws Exception {

        // 1. Create products used in the order.
        Product iphone =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        10000
                );

        Product mouse =
                new Product(
                        2,
                        "Mouse",
                        "Electronics",
                        1000
                );


        // 2. Create the customer placing the order.
        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );


        // 3. Prepare initial inventory.
        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(iphone, 10);
        inventoryService.addStock(mouse, 20);


        // 4. Build the customer's shopping cart.
        Cart cart = new Cart();

        cart.addProduct(iphone, 2);
        cart.addProduct(mouse, 3);


        // 5. Create the selected payment strategy using the factory.
        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(
                                PaymentType.CARD
                        )
                );


        // 6. Create the publisher responsible for order events.
        OrderEventPublisher publisher =
                new OrderEventPublisher();


        // 7. Register notification and analytics observers.
        EmailNotificationObserver emailObserver =
                new EmailNotificationObserver();

        AnalyticsObserver analyticsObserver =
                new AnalyticsObserver();

        publisher.addObserver(emailObserver);
        publisher.addObserver(analyticsObserver);


        // 8. Create CheckoutService with its required dependencies.
        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );


        // 9. Execute the complete checkout workflow.
        Order order =
                checkoutService.checkout(
                        1001,
                        customer,
                        cart
                );


        // 10. Calculate analytics from the published completed order.
        AnalyticsService analyticsService =
                new AnalyticsService();

        double revenue =
                analyticsService.calculateTotalRevenue(
                        analyticsObserver.getOrders()
                );


        // 11. Persist the completed order to a temporary CSV-style file.
        Path file =
                tempDir.resolve("orders.csv");

        OrderFileRepository repository =
                new OrderFileRepository();

        repository.saveOrders(
                file,
                analyticsObserver.getOrders()
        );


        // 12. Read the persisted order back from the file.
        List<OrderSummary> restoredOrders =
                repository.readOrders(file);


        /*
         * 13. Verify the complete workflow:
         *
         * Order total:
         * 2 × 10,000 = 20,000
         * 3 × 1,000  =  3,000
         * Total      = 23,000
         *
         * Inventory after checkout:
         * iPhone: 10 - 2 = 8
         * Mouse:  20 - 3 = 17
         */
        assertAll(

                // Order verification
                () -> assertEquals(
                        1001,
                        order.getId()
                ),

                () -> assertEquals(
                        OrderStatus.PAID,
                        order.getStatus()
                ),

                () -> assertEquals(
                        PaymentType.CARD,
                        order.getPaymentType()
                ),

                () -> assertEquals(
                        23000,
                        order.calculateTotal()
                ),


                // Inventory verification
                () -> assertEquals(
                        8,
                        inventoryService.getStock(
                                iphone.getId()
                        )
                ),

                () -> assertEquals(
                        17,
                        inventoryService.getStock(
                                mouse.getId()
                        )
                ),


                // Observer verification
                () -> assertEquals(
                        1,
                        analyticsObserver
                                .getOrders()
                                .size()
                ),

                () -> assertSame(
                        order,
                        analyticsObserver
                                .getOrders()
                                .get(0)
                ),


                // Analytics verification
                () -> assertEquals(
                        23000,
                        revenue
                ),


                // Persistence verification
                () -> assertEquals(
                        1,
                        restoredOrders.size()
                ),

                () -> assertEquals(
                        1001,
                        restoredOrders
                                .get(0)
                                .getOrderId()
                ),

                () -> assertEquals(
                        OrderStatus.PAID,
                        restoredOrders
                                .get(0)
                                .getStatus()
                ),

                () -> assertEquals(
                        PaymentType.CARD,
                        restoredOrders
                                .get(0)
                                .getPaymentType()
                ),

                () -> assertEquals(
                        23000,
                        restoredOrders
                                .get(0)
                                .getTotal()
                )
        );
    }
}