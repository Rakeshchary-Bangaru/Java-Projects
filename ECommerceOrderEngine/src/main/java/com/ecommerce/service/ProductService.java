package com.ecommerce.service;

import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService{
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository){
        this.productRepository = productRepository;
    }
}