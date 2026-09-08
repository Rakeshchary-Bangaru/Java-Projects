package com.ecommerce.model;

/**
 * Represents a product and its quantity inside a shopping cart.
 *
 * <p>The quantity can be updated while the product remains in the cart.
 * The subtotal is calculated using the product's current price.</p>
 */
public final class CartItem {

    private final Product product;
    private int quantity;

    /**
     * Creates a cart item for the specified product and quantity.
     *
     * @param product product added to the cart
     * @param quantity quantity of the product
     * @throws IllegalArgumentException if the product is null
     *                                  or the quantity is not greater than zero
     */
    public CartItem(Product product, int quantity) {

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
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Updates the quantity of this cart item.
     *
     * @param newQuantity new quantity for the product
     * @throws IllegalArgumentException if the quantity is not greater than zero
     */
    public void updateQuantity(int newQuantity) {

        if (newQuantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        this.quantity = newQuantity;
    }

    /**
     * Calculates the current subtotal for this cart item.
     *
     * <p>The calculation uses the product's current price, so changing
     * the product price also changes the cart-item subtotal.</p>
     *
     * @return current product price multiplied by quantity
     */
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }
}