package com.ecommerce.repository;

import com.ecommerce.database.DatabaseConnection;
import com.ecommerce.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcCustomerRepository implements CustomerRepository{

    @Override
    public void save(Customer customer) throws SQLException {
       if(customer == null){
           throw new IllegalArgumentException("Customer cannot be null");
       }

       String sql = "INSERT INTO customers(id, name, email) VALUES (?, ?, ?)";

       try(Connection connection = DatabaseConnection.getConnection();
           PreparedStatement statement = connection.prepareStatement(sql)){
           statement.setLong(1,customer.getId());
           statement.setString(2, customer.getName());
           statement.setString(3,customer.getEmail());

           statement.executeUpdate();
       }
    }

    @Override
    public Optional<Customer> findById(long id) throws SQLException {
      String sql = "SELECT id, name, email FROM customers WHERE id = ?";

      try(Connection connection = DatabaseConnection.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql)){
          statement.setLong(1,id);

          try(ResultSet resultSet = statement.executeQuery()){
              if(resultSet.next()){

                  return  Optional.of(mapCustomer(resultSet));
              }

              return Optional.empty();
          }
      }

    }

    @Override
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT id, name, email FROM customers";

        List<Customer> customers = new ArrayList<>();

        try(Connection connection = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()){
            while(resultSet.next()){
             customers.add(mapCustomer(resultSet));
            }
        }
        return customers;
    }

    @Override
    public void update(Customer customer) throws SQLException {
        if(customer == null){
            throw new IllegalArgumentException("Customer cannot be null");
        }

        String sql = "UPDATE customers SET email = ? WHERE id = ?";

        try(Connection connection  = DatabaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, customer.getEmail());
            statement.setLong(2,customer.getId());
            statement.executeUpdate();
        }

    }

    @Override
    public boolean deleteById(long id) throws SQLException {
       String sql = "DELETE FROM customers WHERE id = ?";
       try(Connection connection = DatabaseConnection.getConnection();
       PreparedStatement statement = connection.prepareStatement(sql)){
           statement.setLong(1,id);
           int rowsAffected = statement.executeUpdate();
           return rowsAffected>0;
       }

    }

    private Customer mapCustomer(ResultSet resultSet)
            throws SQLException {

        return new Customer(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email")
        );
    }
}