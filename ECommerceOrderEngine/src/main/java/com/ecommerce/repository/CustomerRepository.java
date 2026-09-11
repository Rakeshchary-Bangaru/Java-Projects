package com.ecommerce.repository;

import com.ecommerce.model.Customer;


import java.util.Optional;
import java.sql.SQLException;
import java.util.List;

public interface CustomerRepository{

    void save(Customer customer) throws SQLException;

    Optional<Customer> findById(long id) throws SQLException;

    List<Customer> findAll() throws SQLException;

    void update(Customer customer) throws SQLException;

    boolean deleteById(long id) throws SQLException;
}