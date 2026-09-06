package com.ecommerce.checkout;

import com.ecommerce.inventory.InventoryService;
import com.ecommerce.notification.OrderEventPublisher;
import com.ecommerce.payment.CardPaymentStrategy;
import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.model.*;
import com.ecommerce.payment.*;
import com.ecommerce.analytics.AnalyticsObserver;
import com.ecommerce.analytics.AnalyticsService;
import com.ecommerce.payment.PaymentFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutServiceTest {

    @Test
    void shouldRejectNullInventoryService() {

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );
        OrderEventPublisher publisher  = new OrderEventPublisher();
        assertThrows(
                IllegalArgumentException.class,
                () -> new CheckoutService(
                        null,
                        paymentProcessor,
                        publisher
                )
        );
    }

    @Test
    void shouldRejectNullPaymentProcessor() {

        InventoryService inventoryService =
                new InventoryService();
        OrderEventPublisher publisher  = new OrderEventPublisher();
        assertThrows(
                IllegalArgumentException.class,
                () -> new CheckoutService(
                        inventoryService,
                        null,
                        publisher
                )
        );
    }

    @Test
    void shouldCreateCheckoutServiceWithValidDependencies() {

        InventoryService inventoryService =
                new InventoryService();

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );
        OrderEventPublisher publisher = new OrderEventPublisher();
        assertDoesNotThrow(
                () -> new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                )
        );
    }
    @Test
    void shouldCompleteCheckoutSuccessfully() {

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

        Cart cart = new Cart();
        cart.addProduct(product, 2);

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(product, 5);

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );
        OrderEventPublisher publisher = new OrderEventPublisher();

        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        Order order =
                checkoutService.checkout(
                        1001,
                        customer,
                        cart
                );

        assertAll(
                () -> assertNotNull(order),

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
                        160000,
                        order.calculateTotal()
                ),

                () -> assertEquals(
                        3,
                        inventoryService.getStock(
                                product.getId()
                        )
                )
        );
    }
    @Test
    void shouldFailWhenStockIsInsufficient() {

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

        Cart cart = new Cart();

        cart.addProduct(product, 5);

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(product, 2);

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );
        OrderEventPublisher publisher = new OrderEventPublisher();
        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        assertThrows(
                InsufficientStockException.class,
                () -> checkoutService.checkout(
                        1001,
                        customer,
                        cart
                )
        );

        assertEquals(
                2,
                inventoryService.getStock(
                        product.getId()
                )
        );
    }
    @Test
    void shouldRollbackPreviouslyReservedItemsWhenLaterReservationFails() {

        Product iphone =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        80000
                );

        Product laptop =
                new Product(
                        2,
                        "Laptop",
                        "Electronics",
                        100000
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Cart cart = new Cart();

        cart.addProduct(iphone, 2);
        cart.addProduct(laptop, 5);

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(iphone, 10);
        inventoryService.addStock(laptop, 2);

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );
        OrderEventPublisher publisher  =  new OrderEventPublisher();
        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        assertThrows(
                InsufficientStockException.class,
                () -> checkoutService.checkout(
                        1001,
                        customer,
                        cart
                )
        );

        assertAll(
                () -> assertEquals(
                        10,
                        inventoryService.getStock(
                                iphone.getId()
                        )
                ),

                () -> assertEquals(
                        2,
                        inventoryService.getStock(
                                laptop.getId()
                        )
                )
        );
    }
    @Test
    void shouldReleaseInventoryWhenPaymentFails() {

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

        Cart cart = new Cart();
        cart.addProduct(product, 2);

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(product, 5);


        PaymentStrategy failedPayment =
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


        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        failedPayment
                );
        OrderEventPublisher publisher = new OrderEventPublisher();
        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );


        assertThrows(
                PaymentFailedException.class,
                () -> checkoutService.checkout(
                        1001,
                        customer,
                        cart
                )
        );


        assertEquals(
                5,
                inventoryService.getStock(
                        product.getId()
                )
        );
    }
    @Test
    void shouldRejectEmptyCart() {

        Cart cart = new Cart();

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        InventoryService inventoryService =
                new InventoryService();

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );
        OrderEventPublisher publisher  = new OrderEventPublisher();
        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout(
                        1001,
                        customer,
                        cart
                )
        );
    }

    @Test
    void shouldNotifyObserverAfterSuccessfulCheckout() {

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

        Cart cart = new Cart();
        cart.addProduct(product, 1);

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(product, 5);

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        AtomicBoolean notified =
                new AtomicBoolean(false);

        publisher.addObserver(
                order -> notified.set(true)
        );

        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        checkoutService.checkout(
                1001,
                customer,
                cart
        );

        assertTrue(notified.get());
    }
    @Test
    void shouldRejectNullOrderEventPublisher() {

        InventoryService inventoryService =
                new InventoryService();

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        null
                )
        );
    }

    @Test
    void shouldRecordSuccessfulOrderInAnalyticsObserver() {

        Product product =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        10000
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Cart cart = new Cart();
        cart.addProduct(product, 2);

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(product, 5);

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        AnalyticsObserver analyticsObserver =
                new AnalyticsObserver();

        publisher.addObserver(analyticsObserver);

        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        Order order =
                checkoutService.checkout(
                        1001,
                        customer,
                        cart
                );

        assertAll(
                () -> assertEquals(
                        1,
                        analyticsObserver.getOrders().size()
                ),

                () -> assertSame(
                        order,
                        analyticsObserver.getOrders().get(0)
                )
        );
    }

    @Test
    void shouldCalculateRevenueFromCompletedCheckouts() {

        Product iphone =
                new Product(
                        1,
                        "iPhone",
                        "Electronics",
                        10000
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        InventoryService inventoryService =
                new InventoryService();

        inventoryService.addStock(iphone, 10);

        PaymentProcessor paymentProcessor =
                new PaymentProcessor(
                        PaymentFactory.create(PaymentType.CARD)
                );

        OrderEventPublisher publisher =
                new OrderEventPublisher();

        AnalyticsObserver analyticsObserver =
                new AnalyticsObserver();

        publisher.addObserver(analyticsObserver);

        CheckoutService checkoutService =
                new CheckoutService(
                        inventoryService,
                        paymentProcessor,
                        publisher
                );

        // First checkout
        Cart cart1 = new Cart();
        cart1.addProduct(iphone, 2);

        checkoutService.checkout(
                1001,
                customer,
                cart1
        );

        // Second checkout
        Cart cart2 = new Cart();
        cart2.addProduct(iphone, 3);

        checkoutService.checkout(
                1002,
                customer,
                cart2
        );

        AnalyticsService analyticsService =
                new AnalyticsService();

        double revenue =
                analyticsService.calculateTotalRevenue(
                        analyticsObserver.getOrders()
                );

        assertAll(
                () -> assertEquals(
                        2,
                        analyticsObserver.getOrders().size()
                ),

                () -> assertEquals(
                        50000,
                        revenue
                ),

                () -> assertEquals(
                        5,
                        inventoryService.getStock(
                                iphone.getId()
                        )
                )
        );
    }

}