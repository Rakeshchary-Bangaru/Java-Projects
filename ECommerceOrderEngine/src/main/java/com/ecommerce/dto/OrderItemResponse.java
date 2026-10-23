package com.ecommerce.dto;

public class OrderItemResponse {

    private final long productId;
    private final String productName;
    private final int quantity;
    private final double subtotal;

    public OrderItemResponse(
            long productId,
            String productName,
            int quantity,
            double subtotal) {

        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }
}