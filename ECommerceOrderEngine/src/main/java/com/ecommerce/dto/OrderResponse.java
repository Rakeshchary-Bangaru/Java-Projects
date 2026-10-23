package com.ecommerce.dto;

import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private final long id;

    private final long customerId;
    private final String customerName;

    private final PaymentType paymentType;
    private final OrderStatus status;

    private final LocalDateTime createdAt;

    private final double total;

    private final List<OrderItemResponse> items;

    public OrderResponse(
            long id,
            long customerId,
            String customerName,
            PaymentType paymentType,
            OrderStatus status,
            LocalDateTime createdAt,
            double total,
            List<OrderItemResponse> items) {

        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.paymentType = paymentType;
        this.status = status;
        this.createdAt = createdAt;
        this.total = total;
        this.items = items;
    }

    public long getId() {
        return id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public double getTotal() {
        return total;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}