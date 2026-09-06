package com.ecommerce.persistence;

import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;

/**
 * Represents a lightweight summary of an order read from persistence.
 *
 * <p>The summary contains only the information stored by
 * {@link OrderFileRepository}: order ID, status, payment type,
 * and total value.</p>
 *
 * <p>All fields are immutable after construction.</p>
 */
public final class OrderSummary {

    private final long orderId;
    private final OrderStatus status;
    private final PaymentType paymentType;
    private final double total;

    /**
     * Creates an order summary.
     *
     * @param orderId identifier of the order
     * @param status current order status
     * @param paymentType payment method used
     * @param total total value of the order
     */
    public OrderSummary(
            long orderId,
            OrderStatus status,
            PaymentType paymentType,
            double total) {

        this.orderId = orderId;
        this.status = status;
        this.paymentType = paymentType;
        this.total = total;
    }

    public long getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public double getTotal() {
        return total;
    }
}