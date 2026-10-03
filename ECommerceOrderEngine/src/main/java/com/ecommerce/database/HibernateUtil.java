package com.ecommerce.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Creates and provides the Hibernate SessionFactory.
 */

public final class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY = buildSessionFactory();

    private HibernateUtil(){

    }

    private static SessionFactory buildSessionFactory(){
        try{
            String password  = System.getenv("ECOMMERCE_DB_PASSWORD");

            if(password == null || password.isBlank()){
                throw  new IllegalStateException("ECOMMERCE_DB_PASSWORD environment variable is not set");
            }

            Configuration configuration = new Configuration().configure();

            /*
             * Keep credentials out of hibernate.cfg.xml.
             */

            configuration.setProperty("hibernate.connection.password",password);

            return configuration.buildSessionFactory();
        }
        catch (Exception e){
            e.printStackTrace();

            throw new IllegalStateException("Failed to build Hibernate SessionFactory", e);
        }
    }

    public static SessionFactory getSessionFactory(){
        return SESSION_FACTORY;
    }

    public static  void shutdown(){
        if(!SESSION_FACTORY.isClosed()){
            SESSION_FACTORY.close();
        }
    }
}