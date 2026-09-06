package com.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer's shopping cart.
 *
 * <p>The cart stores {@link CartItem} objects and supports adding products,
 * removing products, updating quantities, and calculating the current
 * cart total.</p>
 *
 * <p>If the same product is added more than once, its quantity is increased
 * instead of creating a duplicate cart item.</p>
 */
public final class Cart {

    private final List<CartItem> items;

    /**
     * Creates an empty shopping cart.
     */
    public Cart() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds a product to the cart.
     *
     * <p>If the product already exists in the cart, the supplied quantity
     * is added to the existing quantity.</p>
     *
     * @param product product to add
     * @param quantity quantity to add
     * @throws IllegalArgumentException if the product is null
     *                                  or quantity is not greater than zero
     */
    public void addProduct(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        for (CartItem item : items) {
            if (item.getProduct().getId() == product.getId()) {
                int newQuantity = item.getQuantity() + quantity;

                item.updateQuantity(newQuantity);
                return;
            }
        }

        items.add(new CartItem(product, quantity));
    }

    /**
     * Removes a product from the cart using its product ID.
     *
     * @param productId identifier of the product to remove
     */
    public void removeProduct(long productId) {
        items.removeIf(
                item -> item.getProduct().getId() == productId
        );
    }

    /**
     * Updates the quantity of an existing product in the cart.
     *
     * @param productId identifier of the product
     * @param newQuantity new quantity for the cart item
     * @throws IllegalArgumentException if the product is not present
     *                                  in the cart
     */
    public void updateQuantity(long productId, int newQuantity) {
        for (CartItem item : items) {
            if (item.getProduct().getId() == productId) {
                item.updateQuantity(newQuantity);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Product not found in cart"
        );
    }

    /**
     * Returns an unmodifiable copy of the cart-item list.
     *
     * <p>This prevents callers from directly adding or removing elements
     * from the cart's internal list.</p>
     *
     * @return unmodifiable copy of the cart items
     */
    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    /**
     * Checks whether the cart contains any items.
     *
     * @return true if the cart is empty, otherwise false
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Calculates the total value of all items currently in the cart.
     *
     * @return sum of all cart-item subtotals
     */
    public double calculateTotal() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }
}