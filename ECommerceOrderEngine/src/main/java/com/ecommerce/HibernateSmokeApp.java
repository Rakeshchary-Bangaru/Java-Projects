package com.ecommerce;

import com.ecommerce.database.HibernateUtil;
import org.hibernate.Session;

public class HibernateSmokeApp{
    public static void main(String[] args){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            System.out.println("Hibernate connected successfully...");
        }

        finally{
            HibernateUtil.shutdown();
        }
    }
}