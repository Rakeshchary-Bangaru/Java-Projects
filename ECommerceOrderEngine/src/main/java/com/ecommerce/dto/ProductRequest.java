
package com.ecommerce.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProductRequest {

    // Existing product ID contract
    @Positive(message = "Product ID must be greater than zero")
    private long id;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name is too long")
    private String name;

    @NotBlank(message = "Product category is required")
    @Size(max = 120, message = "Category is too long")
    private String category;

    @NotNull(message = "Product price is required")
    @DecimalMin(
            value = "0.00",
            inclusive = false,
            message = "Product price must be greater than zero"
    )
    @DecimalMax(
            value = "99999999.99",
            message = "Product price exceeds the allowed limit"
    )
    private BigDecimal price;

    @Size(
            max = 2000,
            message = "Description cannot exceed 2000 characters"
    )
    private String description;

    @Size(
            max = 120,
            message = "Brand cannot exceed 120 characters"
    )
    private String brand;

    /*
     * Image uploads will use a separate secured endpoint.
     * Do not accept arbitrary image URLs or S3 object keys
     * from a seller through this request.
     */

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}
