package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcProductRepository implements ProductRepository{


    @Override
    public void save(Product product) throws SQLException {
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }

        String sql = "INSERT INTO products " +
                    "(id,name,category,price)" +
                    "VALUES(?,?,?,?)";

        try(Connection connection = DatabaseConnection.getConnection();
          PreparedStatement statement = connection.prepareStatement(sql) ){
            statement.setLong(1,product.getId());
            statement.setString(2, product.getName());
            statement.setString(
                    3,
                    product.getCategory()
            );

            statement.setDouble(
                    4,
                    product.getPrice()
            );

            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Product> findById(long id) throws SQLException {
        String sql = "SELECT id, name, category, price " +
                    "FROM products " +
                    "WHERE id = ?";

        try(Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setLong(1,id);

            try(ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return Optional.of(
                            mapProduct(resultSet)
                    );
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Product> findAll() throws SQLException {
      String sql = "SELECT id, name, category, price FROM products ";

      List<Product> products = new ArrayList<>();

      try(Connection connection = DatabaseConnection.getConnection();
          PreparedStatement statement = connection.prepareStatement(sql);
          ResultSet resultSet = statement.executeQuery()){
          while(resultSet.next()){
              products.add(mapProduct(resultSet));
          }
      }
      return products;
    }

    @Override
    public void update(Product product) throws SQLException {
     if(product == null){
         throw new IllegalArgumentException("Product cannot be null");
     }

     String sql = "UPDATE products SET name = ?, category = ?, price = ? WHERE id = ?";

     try(Connection connection = DatabaseConnection.getConnection();
     PreparedStatement statement = connection.prepareStatement(sql)
        ){
         statement.setString(1, product.getName());
         statement.setString(2, product.getCategory());
         statement.setDouble(3,product.getPrice());
         statement.setLong(4,product.getId());

         statement.executeUpdate();
     }

    }

    @Override
    public boolean deleteById(long id) throws SQLException {
      String sql = "DELETE FROM products WHERE id = ?";

      try(Connection connection = DatabaseConnection.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)){
          statement.setLong(1,id);
          int rowsAffected = statement.executeUpdate();

          return rowsAffected>0;
      }
    }

    private Product mapProduct(ResultSet resultSet)
            throws SQLException {

        return new Product(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getDouble("price")
        );
    }
}