package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Product;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class HibernateProductRepository implements ProductRepository{

    @Override
    public void save(Product product) throws SQLException{
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }

        Transaction transaction = null;

        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();

            session.persist(product);
            transaction.commit();
        }
        catch(HibernateException e){
            if(transaction != null && transaction.isActive()){
                transaction.rollback();
            }

            throw new SQLException("Failed to save product" , e);
        }
    }

    @Override
    public Optional<Product> findById(long id) throws SQLException{
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Product product = session.find(Product.class,id);

            return Optional.ofNullable(product);
        }
        catch(HibernateException e){
            throw new SQLException("Failed to find product",e);
        }
    }

    @Override
    public List<Product> findAll() throws SQLException{
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("from Product", Product.class).getResultList();
        }
        catch(HibernateException e){
            throw new SQLException("Failed to retrieve products", e);
        }
    }

    @Override
    public void update(Product product) throws SQLException{
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }

        Transaction transaction = null;

        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            session.merge(product);
            transaction.commit();
        }
        catch(HibernateException e){
            if(transaction != null && transaction.isActive()){
                transaction.rollback();
            }
            throw new SQLException("Failed to update product",e);
        }
    }

    @Override
    public boolean deleteById(long id) throws SQLException{
        Transaction transaction = null;

        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();

            Product product = session.find(Product.class,id);

            if(product == null){
                transaction.commit();
                return false;
            }

            session.remove(product);
            transaction.commit();

            return true;
        }
        catch(HibernateException e){
            if(transaction != null && transaction.isActive()){
                transaction.rollback();
            }
            throw new SQLException("Failed to delete product",e);
        }
    }
}