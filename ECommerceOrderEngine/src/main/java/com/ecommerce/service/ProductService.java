package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.repository.SpringDataProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final SpringDataProductRepository productRepository;

    public ProductService(
            SpringDataProductRepository productRepository) {

        this.productRepository = productRepository;
    }

    public Product save(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        return productRepository.save(product);
    }

    public Optional<Product> findById(long id) {

        return productRepository.findById(id);
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
}