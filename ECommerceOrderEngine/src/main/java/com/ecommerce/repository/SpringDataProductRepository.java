package com.ecommerce.repository;

import com.ecommerce.model.Product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataProductRepository
        extends JpaRepository<Product, Long> {

    boolean existsByIdAndSellerUserId(
            Long productId,
            Long userId
    );

    List<Product> findAllBySellerUserId(Long userId);
}