package com.ecommerce.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection{
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/ecommerce_db";

    private static final String DEFAULT_USER = "root";

    private DatabaseConnection(){

    }

    public static Connection getConnection() throws SQLException{
        String url = System.getenv()
                .getOrDefault("ECOMMERCE_DB_URL" , DEFAULT_URL);

        String user = System.getenv()
                .getOrDefault("ECOMMERCE_DB_USER" , DEFAULT_USER);

        String password = System.getenv("ECOMMERCE_DB_PASSWORD");

        if(password == null || password.isBlank()){
            throw new IllegalStateException("ECOMMERCE_DB_PASSWORD environment variable is not set");
        }

        return DriverManager.getConnection(url,user,password);
    }
}