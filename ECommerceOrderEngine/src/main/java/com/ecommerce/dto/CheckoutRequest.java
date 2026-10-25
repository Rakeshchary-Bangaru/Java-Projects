package com.ecommerce.dto;

import com.ecommerce.model.PaymentType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class CheckoutRequest {

    @Positive
    private long orderId;

    @Positive
    private long customerId;

    @NotNull
    private PaymentType paymentType;

    @Valid
    @NotEmpty
    private List<CheckoutItemRequest> items;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(
            PaymentType paymentType) {

        this.paymentType = paymentType;
    }

    public List<CheckoutItemRequest> getItems() {
        return items;
    }

    public void setItems(
            List<CheckoutItemRequest> items) {

        this.items = items;
    }
}