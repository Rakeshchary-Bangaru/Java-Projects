package com.ecommerce.analytics;

import com.ecommerce.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class AnalyticsServiceTest {

    @Test
    void shouldCalculateRevenueFromPaidOrders() {

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

        Order order1 =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        102,
                        customer,
                        List.of(
                                new OrderItem(product, 2)
                        ),
                        PaymentType.UPI
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);

        AnalyticsService analyticsService =
                new AnalyticsService();

        double revenue =
                analyticsService.calculateTotalRevenue(
                        List.of(order1, order2)
                );

        assertEquals(
                30000,
                revenue
        );
    }

    @Test
    void shouldCalculateAverageOrderValue() {

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

        Order order1 =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        102,
                        customer,
                        List.of(
                                new OrderItem(product, 2)
                        ),
                        PaymentType.UPI
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);

        AnalyticsService analyticsService =
                new AnalyticsService();

        double average =
                analyticsService.calculateAverageOrderValue(
                        List.of(order1, order2)
                );

        assertEquals(
                15000,
                average
        );
    }
    @Test
    void shouldCountPaidOrders() {

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

        Order order1 =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        102,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.UPI
                );

        Order order3 =
                new Order(
                        103,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.WALLET
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);
        order3.updateStatus(OrderStatus.CANCELLED);

        AnalyticsService analyticsService =
                new AnalyticsService();

        long count =
                analyticsService.countPaidOrders(
                        List.of(order1, order2, order3)
                );

        assertEquals(2, count);
    }
    @Test
    void shouldCountOrdersByPaymentType() {

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

        Order order1 =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        102,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.CARD
                );

        Order order3 =
                new Order(
                        103,
                        customer,
                        List.of(
                                new OrderItem(product, 1)
                        ),
                        PaymentType.UPI
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);
        order3.updateStatus(OrderStatus.PAID);

        AnalyticsService analyticsService =
                new AnalyticsService();

        Map<PaymentType, Long> result =
                analyticsService.countOrdersByPaymentType(
                        List.of(order1, order2, order3)
                );

        assertAll(
                () -> assertEquals(
                        2L,
                        result.get(PaymentType.CARD)
                ),

                () -> assertEquals(
                        1L,
                        result.get(PaymentType.UPI)
                )
        );
    }
    @Test
    void shouldFindBestSellingProduct() {

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

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Order order1 =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(iphone, 2),
                                new OrderItem(mouse, 1)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        102,
                        customer,
                        List.of(
                                new OrderItem(iphone, 3),
                                new OrderItem(mouse, 1)
                        ),
                        PaymentType.UPI
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);

        AnalyticsService analyticsService =
                new AnalyticsService();

        Optional<Product> result =
                analyticsService.findBestSellingProduct(
                        List.of(order1, order2)
                );

        assertTrue(result.isPresent());

        assertEquals(
                iphone,
                result.get()
        );
    }
    @Test
    void shouldCalculateRevenueByCategory() {

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

        Product book =
                new Product(
                        3,
                        "Java Book",
                        "Books",
                        500
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Order order1 =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(iphone, 2),
                                new OrderItem(mouse, 1)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        102,
                        customer,
                        List.of(
                                new OrderItem(book, 3)
                        ),
                        PaymentType.UPI
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);

        AnalyticsService analyticsService =
                new AnalyticsService();

        Map<String, Double> result =
                analyticsService.calculateRevenueByCategory(
                        List.of(order1, order2)
                );

        assertAll(
                () -> assertEquals(
                        21000,
                        result.get("Electronics")
                ),

                () -> assertEquals(
                        1500,
                        result.get("Books")
                )
        );
    }
    @Test
    void shouldFindTopPaidOrdersByValue() {

        Product cheapProduct =
                new Product(
                        1,
                        "Mouse",
                        "Electronics",
                        1000
                );

        Product expensiveProduct =
                new Product(
                        2,
                        "Laptop",
                        "Electronics",
                        10000
                );

        Customer customer =
                new Customer(
                        1,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Order order1 =
                new Order(
                        101,
                        customer,
                        List.of(
                                new OrderItem(cheapProduct, 2)
                        ),
                        PaymentType.CARD
                );

        Order order2 =
                new Order(
                        102,
                        customer,
                        List.of(
                                new OrderItem(expensiveProduct, 2)
                        ),
                        PaymentType.UPI
                );

        Order order3 =
                new Order(
                        103,
                        customer,
                        List.of(
                                new OrderItem(expensiveProduct, 1)
                        ),
                        PaymentType.WALLET
                );

        order1.updateStatus(OrderStatus.PAID);
        order2.updateStatus(OrderStatus.PAID);
        order3.updateStatus(OrderStatus.PAID);

        AnalyticsService analyticsService =
                new AnalyticsService();

        List<Order> result =
                analyticsService.findTopPaidOrders(
                        List.of(order1, order2, order3),
                        2
                );

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(102, result.get(0).getId()),
                () -> assertEquals(103, result.get(1).getId())
        );
    }
}