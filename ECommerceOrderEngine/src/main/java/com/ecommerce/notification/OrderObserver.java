package com.ecommerce.notification;

import com.ecommerce.model.Order;

/**
 * Defines the observer contract for components that react to order events.
 *
 * <p>Implementations can perform actions such as sending notifications
 * or collecting analytics when an order event is published.</p>
 */
public interface OrderObserver {

    /**
     * Handles a published order event.
     *
     * @param order order associated with the event
     */
    void update(Order order);
}