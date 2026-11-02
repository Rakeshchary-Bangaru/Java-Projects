package com.ecommerce.model;
import jakarta.persistence.*;

import com.ecommerce.seller.model.Seller;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a product available in the e-commerce system.
 *
 * <p>This class is also a JPA entity mapped to the existing
 * {@code products} table.</p>
 *
 * <p>The class implements {@link Serializable} so product information
 * can be included when an {@link Order} is serialized.</p>
 */

@Entity
@Table(name = "products")
public  class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name" , nullable = false)
    private  String name;

    @Column(name = "category" , nullable = false)
    private  String category;

    @Column(name = "price" , nullable = false, precision = 10,scale = 2)
    private BigDecimal price;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "brand", length = 120)
    private String brand;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

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

    /**
     * Required by JPA/Hibernate.
     *
     * <p>Hibernate uses this constructor when reconstructing
     * Product objects from database rows.</p>
     */

    protected Product(){

    }
    public Product(
            long id,
            String name,
            String category,
            BigDecimal price) {

        this(id, name, category, price, null, null, null, true);
    }

    // Compatibility constructor for older versions/tests
    public Product(
            long id,
            String name,
            String category,
            double price) {

        this(
                id,
                name,
                category,
                BigDecimal.valueOf(price)
        );
    }

    public Product(
            long id,
            String name,
            String category,
            BigDecimal price,
            String description,
            String brand,
            String imageUrl,
            boolean active) {

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

        if (price == null ||
                price.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Product price must be greater than zero"
            );
        }

        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price.setScale(2, RoundingMode.HALF_UP);
        this.description = description;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.active = active;
    }


    public Product(
            String name,
            String category,
            BigDecimal price,
            String description,
            String brand
    ) {
        this.name = Objects.requireNonNull(name).trim();
        this.category = Objects.requireNonNull(category).trim();

        if (this.name.isBlank() || this.category.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name and category are required"
            );
        }

        updatePrice(price);

        this.description = description;
        this.brand = brand;
        this.active = true;
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
        return price.doubleValue();
    }


    public BigDecimal getPriceAmount() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActive() {
        return active;
    }
    public Seller getSeller() {
        return seller;
    }

    /**
     * Updates the current price of the product.
     *
     * @param newPrice new product price
     * @throws IllegalArgumentException if the new price is not greater than zero
     */
    public void updatePrice(BigDecimal newPrice) {

        if (newPrice == null ||
                newPrice.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Price must be greater than zero"
            );
        }

        this.price =
                newPrice.setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    // Keep temporarily for old code
    public void updatePrice(
            double price) {

        updatePrice(
                BigDecimal.valueOf(price)
        );
    }
    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
    public void assignSeller(Seller seller) {

        this.seller =
                Objects.requireNonNull(
                        seller,
                        "Seller is required"
                );
    }

    public void replaceDetails(
            String name,
            String category,
            BigDecimal price
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException(
                    "Product category is required"
            );
        }

        if (price == null ||
                price.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Product price must be greater than zero"
            );
        }

        this.name = name.trim();
        this.category = category.trim();
        this.price = price;
    }
}