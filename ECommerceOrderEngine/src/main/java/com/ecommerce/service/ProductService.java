package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.audit.AuditAction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final SpringDataProductRepository productRepository;

    public ProductService(
            SpringDataProductRepository productRepository) {

        this.productRepository = productRepository;
    }
    @AuditAction(
            action = "PRODUCT_CREATED",
            entity = "PRODUCT"
    )
    public Product save(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product findByIdOrThrow(
            long id) {

        return productRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found: " + id
                        )
                );
    }

    public List<Product> findAll() {

        return productRepository.findAll();
    }

    public Product update(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        return productRepository.save(product);
    }

    public void deleteById(long id) {

        productRepository.deleteById(id);
    }

    @AuditAction(
            action = "PRODUCT_REPLACED",
            entity = "PRODUCT"
    )
    @Transactional
    public Product replace(
            long id,
            String name,
            String category,
            BigDecimal price) {

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found: " + id
            );
        }

        Product product =
                new Product(
                        id,
                        name,
                        category,
                        price
                );

        return productRepository.save(product);
    }

    @AuditAction(
            action = "PRODUCT_PRICE_UPDATED",
            entity = "PRODUCT"
    )
    @Transactional
    public Product updatePrice(
            long id,
            BigDecimal newPrice) {

        Product product =
                productRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Product not found: " + id
                                )
                        );

        product.updatePrice(newPrice);

        return product;
    }

    @AuditAction(
            action = "PRODUCT_DELETED",
            entity = "PRODUCT"
    )
    @Transactional
    public void deleteOrThrow(
            long id) {

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found: " + id
            );
        }

        productRepository.deleteById(id);
    }
}