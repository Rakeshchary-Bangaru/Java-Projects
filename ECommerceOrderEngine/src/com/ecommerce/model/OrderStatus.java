package com.ecommerce.model;

/**
 * Represents the current lifecycle status of an order.
 *
 * <p>The status is updated as the order progresses through payment,
 * processing, shipping, delivery, or cancellation.</p>
 */
public enum OrderStatus {

    CREATED,
    PAYMENT_PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    PAYMENT_FAILED,
    CANCELLED
}