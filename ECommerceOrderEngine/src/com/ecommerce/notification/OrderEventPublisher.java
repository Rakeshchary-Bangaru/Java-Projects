package com.ecommerce.notification;

import com.ecommerce.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Publishes order events to registered {@link OrderObserver} implementations.
 *
 * <p>The publisher does not depend on concrete observer classes.
 * It communicates only through the {@link OrderObserver} interface,
 * which keeps notification and analytics components loosely coupled.</p>
 */
public final class OrderEventPublisher {

    private final List<OrderObserver> observers;

    /**
     * Creates a publisher with no registered observers.
     */
    public OrderEventPublisher() {
        this.observers = new ArrayList<>();
    }

    /**
     * Registers an observer to receive future order events.
     *
     * @param observer observer to register
     * @throws IllegalArgumentException if the observer is null
     */
    public void addObserver(OrderObserver observer) {

        if (observer == null) {
            throw new IllegalArgumentException(
                    "Observer cannot be null"
            );
        }

        observers.add(observer);
    }

    /**
     * Notifies all registered observers about an order event.
     *
     * <p>Each observer receives the same {@link Order} by invoking
     * {@link OrderObserver#update(Order)}.</p>
     *
     * @param order order associated with the event
     * @throws IllegalArgumentException if the order is null
     */
    public void notifyObservers(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        for (OrderObserver observer : observers) {
            observer.update(order);
        }
    }
}