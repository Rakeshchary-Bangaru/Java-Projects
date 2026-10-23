package com.ecommerce.dto;

import jakarta.validation.constraints.PositiveOrZero;

public class InventoryQuantityRequest {

    @PositiveOrZero
    private int quantity;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(
            int quantity) {

        this.quantity = quantity;
    }
}