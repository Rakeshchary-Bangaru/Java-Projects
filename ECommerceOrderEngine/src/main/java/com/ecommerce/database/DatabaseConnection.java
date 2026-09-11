package com.ecommerce.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class responsible for creating JDBC connections
 * to the application's MySQL database.
 *
 * <p>Database configuration is read from environment variables
 * so sensitive values such as passwords are not hardcoded
 * in source code.</p>
 */
public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/ecommerce_db";

    private static final String DEFAULT_USER =
            "root";

    /*
     * Prevents creation of DatabaseConnection objects.
     * This class is intended to be used only through static methods.
     */
    private DatabaseConnection() {
    }

    /**
     * Creates and returns a new JDBC connection.
     *
     * <p>The database URL and username can be overridden using
     * ECOMMERCE_DB_URL and ECOMMERCE_DB_USER. The password must be
     * provided through ECOMMERCE_DB_PASSWORD.</p>
     *
     * @return a new database connection
     * @throws SQLException if the connection cannot be established
     * @throws IllegalStateException if the database password is missing
     */
    public static Connection getConnection()
            throws SQLException {

        String url = System.getenv()
                .getOrDefault(
                        "ECOMMERCE_DB_URL",
                        DEFAULT_URL
                );

        String user = System.getenv()
                .getOrDefault(
                        "ECOMMERCE_DB_USER",
                        DEFAULT_USER
                );

        String password =
                System.getenv("ECOMMERCE_DB_PASSWORD");

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "ECOMMERCE_DB_PASSWORD environment variable is not set"
            );
        }

        return DriverManager.getConnection(
                url,
                user,
                password
        );
    }
}