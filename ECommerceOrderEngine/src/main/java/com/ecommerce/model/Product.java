package com.ecommerce.model;

import java.io.Serializable;

/**
 * Represents a product available in the e-commerce system.
 *
 * <p>A product has an immutable identifier, name, and category.
 * Its price can be updated when required.</p>
 *
 * <p>The class implements {@link Serializable} so product information
 * can be included when an {@link Order} is serialized.</p>
 */
public final class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String name;
    private final String category;
    private double price;

    /**
     * Creates a product with the specified identifier, name,
     * category, and price.
     *
     * @param id unique product identifier
     * @param name product name
     * @param category product category
     * @param price current product price
     * @throws IllegalArgumentException if the id is not greater than zero,
     *                                  the name is null or blank,
     *                                  the category is null or blank,
     *                                  or the price is not greater than zero
     */
    public Product(long id, String name, String category, double price) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Product id must be greater than zero"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name cannot be empty"
            );
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Product category cannot be empty"
            );
        }

        if (price <= 0) {
            throw new IllegalArgumentException(
                    "Product price must be greater than zero"
            );
        }

        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    /**
     * Updates the current price of the product.
     *
     * @param newPrice new product price
     * @throws IllegalArgumentException if the new price is not greater than zero
     */
    public void updatePrice(double newPrice) {

        if (newPrice <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero"
            );
        }

        this.price = newPrice;
    }
}