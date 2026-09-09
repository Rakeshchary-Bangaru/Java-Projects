package com.ecommerce.repository;

import com.ecommerce.model.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ProductRepository{

    void save(Product product) throws SQLException;

    Optional<Product> findById(long id) throws SQLException;

    List<Product> findAll() throws SQLException;

    void update(Product product) throws SQLException;

    boolean deleteById(long id) throws SQLException;
}