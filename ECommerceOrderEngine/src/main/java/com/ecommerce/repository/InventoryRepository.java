package com.ecommerce.repository;

import java.sql.SQLException;

public interface InventoryRepository{
    void addStock(long productId,int quantity) throws SQLException;

    int  getStock(long productId) throws SQLException;
//reduce quantity when an order is placed
    boolean reserveStock(long productId,int quantity) throws SQLException;
//add quantity back if checkout/payment fails
    void releaseStock(long productId, int quantity) throws SQLException;
}