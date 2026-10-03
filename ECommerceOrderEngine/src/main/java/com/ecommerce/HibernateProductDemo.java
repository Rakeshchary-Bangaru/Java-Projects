package com.ecommerce;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Product;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateProductDemo{
    public static  void main(String[] args){


        // -------------------------------
        // Transaction 1: ensure product exists
        // -------------------------------
        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            Transaction transaction =
                    session.beginTransaction();

            Product product =
                    session.find(
                            Product.class,
                            900002L
                    );

            if (product == null) {

                product =
                        new Product(
                                900002L,
                                "Temporary Mouse",
                                "Electronics",
                                49.99
                        );

                session.persist(product);

                System.out.println(
                        "Temporary product inserted."
                );

            } else {

                System.out.println(
                        "Temporary product already exists."
                );
            }

            transaction.commit();
        }


        // -------------------------------
        // Transaction 2: remove product
        // -------------------------------
        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            Transaction transaction =
                    session.beginTransaction();

            Product product =
                    session.find(
                            Product.class,
                            900002L
                    );

            if (product != null) {

                session.remove(product);

                System.out.println(
                        "Product marked for removal."
                );
            }

            transaction.commit();

        }
//        catch(Exception e){
//            if(transaction != null && transaction.isActive()){
//                transaction.rollback();
//            }
//            e.printStackTrace();
//        }
        finally{
            HibernateUtil.shutdown();
        }
    }
}