package com.ecommerce.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Creates and provides the Hibernate SessionFactory.
 *
 * <p>Database configuration can be overridden using environment
 * variables. This allows automated tests to use a separate test
 * database without changing the application's Hibernate configuration.</p>
 */
public final class HibernateUtil {

    private static final SessionFactory SESSION_FACTORY =
            buildSessionFactory();

    private HibernateUtil() {

    }

    private static SessionFactory buildSessionFactory() {

        try {

            String password =
                    System.getenv(
                            "ECOMMERCE_DB_PASSWORD"
                    );

            if (password == null ||
                    password.isBlank()) {

                throw new IllegalStateException(
                        "ECOMMERCE_DB_PASSWORD environment variable is not set"
                );
            }


            Configuration configuration =
                    new Configuration()
                            .configure();


            /*
             * The password is intentionally never stored
             * inside hibernate.cfg.xml.
             */
            configuration.setProperty(
                    "hibernate.connection.password",
                    password
            );


            /*
             * Automated tests can override the normal
             * development database URL.
             *
             * During mvn test, Surefire provides:
             *
             * ECOMMERCE_DB_URL =
             * jdbc:mysql://localhost:3306/ecommerce_test_db
             */
            String databaseUrl =
                    System.getenv(
                            "ECOMMERCE_DB_URL"
                    );

            if (databaseUrl != null &&
                    !databaseUrl.isBlank()) {

                configuration.setProperty(
                        "hibernate.connection.url",
                        databaseUrl
                );
            }


            /*
             * Allow the database username to be overridden
             * in the same way.
             */
            String databaseUser =
                    System.getenv(
                            "ECOMMERCE_DB_USER"
                    );

            if (databaseUser != null &&
                    !databaseUser.isBlank()) {

                configuration.setProperty(
                        "hibernate.connection.username",
                        databaseUser
                );
            }


            return configuration
                    .buildSessionFactory();

        } catch (Exception e) {

            e.printStackTrace();

            throw new IllegalStateException(
                    "Failed to build Hibernate SessionFactory",
                    e
            );
        }
    }


    public static SessionFactory getSessionFactory() {

        return SESSION_FACTORY;
    }


    public static void shutdown() {

        if (!SESSION_FACTORY.isClosed()) {

            SESSION_FACTORY.close();
        }
    }
}