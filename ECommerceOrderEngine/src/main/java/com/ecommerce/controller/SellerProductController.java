package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private final ProductService productService;


    public SellerProductController(
            ProductService productService
    ) {

        this.productService =
                productService;
    }


    @GetMapping
    public List<ProductResponse> getMyProducts(
            Authentication authentication
    ) {

        return productService
                .findAllForSeller(authentication)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private ProductResponse toResponse(
            Product product
    ) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPriceAmount()
        );
    }
}