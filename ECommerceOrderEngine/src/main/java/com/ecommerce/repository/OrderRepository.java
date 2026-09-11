package com.ecommerce.repository;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;

import java.sql.SQLException;
import java.util.Optional;
import java.util.List;

public interface OrderRepository {

    void save(Order order) throws SQLException;

    Optional<Order> findById(long id) throws SQLException;

    List<Order> findAll() throws SQLException;

    void updateStatus(long orderId, OrderStatus status) throws SQLException;

    boolean deleteById(long id) throws  SQLException;
}