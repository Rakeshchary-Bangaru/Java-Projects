package com.ecommerce.dto;

public class InventoryResponse {

    private final long productId;
    private final int quantity;

    public InventoryResponse(
            long productId,
            int quantity) {

        this.productId = productId;
        this.quantity = quantity;
    }

    public long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}