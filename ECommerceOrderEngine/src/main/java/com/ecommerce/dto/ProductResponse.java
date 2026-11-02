
package com.ecommerce.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private final long id;
    private final String name;
    private final String category;
    private final BigDecimal price;

    // V7 product catalog fields
    private final String description;
    private final String brand;
    private final String imageUrl;
    private final boolean active;

    /*
     * Existing constructor retained for compatibility
     * with older controllers, services, and tests.
     */
    public ProductResponse(
            long id,
            String name,
            String category,
            BigDecimal price
    ) {
        this(
                id,
                name,
                category,
                price,
                null,
                null,
                null,
                true
        );
    }

    /*
     * Full V7 constructor.
     */
    public ProductResponse(
            long id,
            String name,
            String category,
            BigDecimal price,
            String description,
            String brand,
            String imageUrl,
            boolean active
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.active = active;
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

    public BigDecimal getPrice() {
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
}
