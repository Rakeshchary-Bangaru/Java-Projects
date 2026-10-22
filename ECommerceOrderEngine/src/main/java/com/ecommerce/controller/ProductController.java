package com.ecommerce.controller;

import com.ecommerce.dto.ProductPriceUpdateRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.dto.ProductRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService) {

        this.productService = productService;
    }


    @GetMapping
    public List<ProductResponse> getAllProducts() {

        return productService
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable long id) {

        Product product =
                productService
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found: " + id
                                )
                        );

        return ResponseEntity.ok(
                toResponse(product)
        );
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(
            @Valid @RequestBody ProductRequest request) {

        Product product =
                new Product(
                        request.getId(),
                        request.getName(),
                        request.getCategory(),
                        request.getPrice()
                );

        Product savedProduct =
                productService.save(product);

        return toResponse(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> replaceProduct(
            @PathVariable long id,
            @Valid @RequestBody ProductRequest request) {

        Product updatedProduct =
                productService.replace(
                        id,
                        request.getName(),
                        request.getCategory(),
                        request.getPrice()
                );

        return ResponseEntity.ok(
                toResponse(updatedProduct)
        );
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ProductResponse> updateProductPrice(
            @PathVariable long id,
            @Valid @RequestBody ProductPriceUpdateRequest request) {

        Product updatedProduct =
                productService.updatePrice(
                        id,
                        request.getPrice()
                );

        return ResponseEntity.ok(
                toResponse(updatedProduct)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable long id) {

        productService.deleteOrThrow(id);

        return ResponseEntity.noContent().build();
    }
    private ProductResponse toResponse(
            Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getPriceAmount()
        );
    }


}