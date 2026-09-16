package com.ecommerce.testutil;

import com.ecommerce.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Safety utility for database integration tests.
 *
 * <p>Prevents JDBC tests from accidentally running against the
 * development database and modifying development data.</p>
 */
public final class TestDatabaseGuard {

    private TestDatabaseGuard() {
    }

    /**
     * Verifies that the current JDBC connection is using
     * the dedicated test database.
     *
     * @throws Exception if the database cannot be queried
     *                  or the active database is not the test database
     */
    public static void assertUsingTestDatabase()
            throws Exception {

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                Statement statement =
                        connection.createStatement();

                ResultSet resultSet =
                        statement.executeQuery(
                                "SELECT DATABASE()"
                        )
        ) {

            resultSet.next();

            String database =
                    resultSet.getString(1);

            assertEquals(
                    "ecommerce_test_db",
                    database,
                    "Tests must never run against the development database"
            );
        }
    }
}