package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductRepository
        extends JpaRepository<Product, Long> {
}