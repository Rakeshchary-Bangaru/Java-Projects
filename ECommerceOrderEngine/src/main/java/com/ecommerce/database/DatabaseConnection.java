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
 *
 * <p>The default configuration connects to the development database
 * {@code ecommerce_db}. Automated tests can override the database URL,
 * allowing the same connection utility to connect to
 * {@code ecommerce_test_db} without changing application code.</p>
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
     * {@code ECOMMERCE_DB_URL} and {@code ECOMMERCE_DB_USER}.
     * The password must be provided through
     * {@code ECOMMERCE_DB_PASSWORD}.</p>
     *
     * <p>During normal application execution, the default URL points
     * to {@code ecommerce_db}. Maven tests override
     * {@code ECOMMERCE_DB_URL} so database integration tests run
     * against the separate {@code ecommerce_test_db} database.</p>
     *
     * @return a new database connection
     * @throws SQLException if the connection cannot be established
     *                      or the MySQL JDBC driver cannot be loaded
     * @throws IllegalStateException if the database password is missing
     */
    public static Connection getConnection()
            throws SQLException {

        /*
         * Use environment overrides when provided.
         * Otherwise connect to the normal development database.
         */
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

        /*
         * The password intentionally has no hardcoded default
         * because credentials should not be stored in source code.
         */
        String password =
                System.getenv("ECOMMERCE_DB_PASSWORD");

        if (password == null || password.isBlank()) {

            throw new IllegalStateException(
                    "ECOMMERCE_DB_PASSWORD environment variable is not set"
            );
        }

        /*
         * Explicitly load the MySQL JDBC driver before requesting
         * a connection from DriverManager.
         */
        try {

            Class.forName(
                    "com.mysql.cj.jdbc.Driver"
            );

        } catch (ClassNotFoundException e) {

            throw new SQLException(
                    "MySQL JDBC driver not found",
                    e
            );
        }

        return DriverManager.getConnection(
                url,
                user,
                password
        );
    }
}