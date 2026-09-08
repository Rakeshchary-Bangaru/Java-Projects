package com.ecommerce.notification;

import com.ecommerce.model.Order;

/**
 * Observer that reacts to order events by producing an email-style
 * notification.
 *
 * <p>In Core Java V1, the notification is simulated by printing the
 * order information to the console. A real email provider can be
 * integrated in a future version without changing the publisher.</p>
 */
public final class EmailNotificationObserver implements OrderObserver {

    /**
     * Handles a published order event.
     *
     * <p>The current implementation prints the order ID and status
     * to the console as a simulated email notification.</p>
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

        System.out.println(
                "Email notification Order "
                        + order.getId()
                        + " Status is "
                        + order.getStatus()
        );
    }
}