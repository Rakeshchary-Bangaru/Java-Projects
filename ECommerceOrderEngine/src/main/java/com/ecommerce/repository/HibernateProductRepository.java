
package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Product;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class HibernateProductRepository implements ProductRepository {

    /*
     * =========================================================
     * SAVE PRODUCT
     * =========================================================
     *
     * Supports GenerationType.IDENTITY.
     *
     * The new Product must have a null identifier
     * before session.persist() is called.
     */
    @Override
    public void save(Product product) throws SQLException {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            try {

                session.persist(product);

                transaction.commit();

            } catch (RuntimeException e) {

                rollbackSafely(transaction, e);

                throw new SQLException(
                        "Failed to save product",
                        e
                );
            }

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to open Hibernate session for saving product",
                    e
            );
        }
    }

    /*
     * =========================================================
     * FIND PRODUCT BY ID
     * =========================================================
     */
    @Override
    public Optional<Product> findById(long id) throws SQLException {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Product product = session.find(
                    Product.class,
                    id
            );

            return Optional.ofNullable(product);

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to find product with id: " + id,
                    e
            );
        }
    }

    /*
     * =========================================================
     * FIND ALL PRODUCTS
     * =========================================================
     */
    @Override
    public List<Product> findAll() throws SQLException {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "from Product",
                    Product.class
            ).getResultList();

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to retrieve products",
                    e
            );
        }
    }

    /*
     * =========================================================
     * UPDATE PRODUCT
     * =========================================================
     *
     * Uses merge() for a previously persisted Product.
     */
    @Override
    public void update(Product product) throws SQLException {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        if (product.getId() <= 0) {
            throw new IllegalArgumentException(
                    "A persisted product ID is required for update"
            );
        }

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            try {

                session.merge(product);

                transaction.commit();

            } catch (RuntimeException e) {

                rollbackSafely(transaction, e);

                throw new SQLException(
                        "Failed to update product",
                        e
                );
            }

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to open Hibernate session for updating product",
                    e
            );
        }
    }

    /*
     * =========================================================
     * DELETE PRODUCT BY ID
     * =========================================================
     */
    @Override
    public boolean deleteById(long id) throws SQLException {

        try (Session session =
                     HibernateUtil.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            try {

                Product product = session.find(
                        Product.class,
                        id
                );

                if (product == null) {

                    transaction.commit();

                    return false;
                }

                session.remove(product);

                transaction.commit();

                return true;

            } catch (RuntimeException e) {

                rollbackSafely(transaction, e);

                throw new SQLException(
                        "Failed to delete product with id: " + id,
                        e
                );
            }

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to open Hibernate session for deleting product",
                    e
            );
        }
    }

    /*
     * =========================================================
     * SAFE TRANSACTION ROLLBACK
     * =========================================================
     *
     * Rollback happens before the Session is closed.
     *
     * If rollback itself fails, the original exception
     * is preserved and the rollback error is attached
     * as a suppressed exception.
     */
    private void rollbackSafely(
            Transaction transaction,
            RuntimeException originalException
    ) {

        if (transaction == null) {
            return;
        }

        try {

            if (transaction.isActive()) {
                transaction.rollback();
            }

        } catch (RuntimeException rollbackException) {

            originalException.addSuppressed(
                    rollbackException
            );
        }
    }
}
