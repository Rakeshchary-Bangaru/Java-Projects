package com.ecommerce.analytics;

import com.ecommerce.model.Order;
import com.ecommerce.notification.OrderObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer that collects order events for later analytics processing.
 *
 * <p>When an order event is published, the observer stores the received
 * {@link Order}. The collected orders can then be analyzed by
 * {@link AnalyticsService}.</p>
 */
public final class AnalyticsObserver implements OrderObserver {

    private final List<Order> orders;

    /**
     * Creates an empty analytics observer.
     */
    public AnalyticsObserver() {
        this.orders = new ArrayList<>();
    }

    /**
     * Receives and stores a published order event.
     *
     * @param order order associated with the event
     * @throws IllegalArgumentException if the order is null
     */
    @Override
    public void update(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        orders.add(order);
    }

    /**
     * Returns an unmodifiable copy of the collected orders.
     *
     * <p>This prevents callers from directly modifying the observer's
     * internal list structure.</p>
     *
     * @return unmodifiable copy of collected orders
     */
    public List<Order> getOrders() {
        return List.copyOf(orders);
    }
}