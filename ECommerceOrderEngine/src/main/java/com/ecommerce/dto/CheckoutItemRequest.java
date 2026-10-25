package com.ecommerce.dto;

import jakarta.validation.constraints.Positive;

public class CheckoutItemRequest {

    @Positive
    private long productId;

    @Positive
    private int quantity;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}