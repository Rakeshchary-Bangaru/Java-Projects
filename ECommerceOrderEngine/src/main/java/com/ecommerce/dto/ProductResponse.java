package com.ecommerce.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private final long id;
    private final String name;
    private final String category;
    private final BigDecimal price;

    public ProductResponse(
            long id,
            String name,
            String category,
            BigDecimal price) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
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


}