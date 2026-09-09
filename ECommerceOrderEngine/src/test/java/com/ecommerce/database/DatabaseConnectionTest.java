package com.ecommerce.database;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseConnectionTest {

    @Test
    void shouldConnectToEcommerceDatabase()
            throws Exception {

        try (Connection connection =
                     DatabaseConnection.getConnection()) {

            assertNotNull(connection);
            assertFalse(connection.isClosed());
        }
    }
}