package com.ecommerce.dto;

public record ProductAvailabilityResponse(
        long productId,
        boolean inStock,
        String availability
) {
}