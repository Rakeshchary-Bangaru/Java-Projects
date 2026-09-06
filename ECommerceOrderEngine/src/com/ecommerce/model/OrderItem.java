package com.ecommerce.model;

import java.io.Serializable;

/**
 * Represents an immutable product entry inside an order.
 *
 * <p>The quantity and unit price are fixed when the OrderItem is created.
 * The unit price is captured from the product at purchase time so that
 * later changes to the product price do not affect historical order totals.</p>
 *
 * <p>The class implements {@link Serializable} because Order objects
 * containing OrderItem instances can be persisted using Java serialization.</p>
 */
public final class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Product product;
    private final int quantity;
    private final double unitPrice;

    /**
     * Creates an order item for the specified product and quantity.
     *
     * <p>The current product price is captured as the unit price
     * when this object is created.</p>
     *
     * @param product product being purchased
     * @param quantity quantity purchased
     * @throws IllegalArgumentException if the product is null
     *                                  or the quantity is not greater than zero
     */
    public OrderItem(Product product, int quantity) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        this.product = product;
        this.quantity = quantity;

        // Capture the purchase-time price so historical order totals remain stable.
        this.unitPrice = product.getPrice();
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Calculates the subtotal for this order item using the
     * captured purchase-time unit price.
     *
     * @return unit price multiplied by quantity
     */
    public double getSubtotal() {
        return unitPrice * quantity;
    }
}