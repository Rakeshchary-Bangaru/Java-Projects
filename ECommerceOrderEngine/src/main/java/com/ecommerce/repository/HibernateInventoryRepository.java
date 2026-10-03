package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;

public final class HibernateInventoryRepository implements InventoryRepository{

    @Override
    public void addStock(long productId, int quantity) throws SQLException {
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Transaction transaction = null;

        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();


            /*
             * Preserve the V3 atomic UPSERT behavior.
             *
             * If inventory does not exist:
             *     INSERT it.
             *
             * If inventory already exists:
             *     increment the quantity.
             */

            session.createNativeMutationQuery("""
                    INSERT INTO inventory ( product_id,quantity) VALUES (:productId, :quantity) ON DUPLICATE KEY UPDATE quantity = quantity + :quantity
                    """)
                    .setParameter("productId",productId)
                    .setParameter("quantity",quantity)
                    .executeUpdate();
            transaction.commit();
        }
        catch(HibernateException e){
            if(transaction != null && transaction.isActive()){
                transaction.rollback();
            }
            throw new SQLException("Failed to add inventory stock",e);
        }
    }

    @Override
    public int getStock(long productId) throws SQLException {
        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()){
            Integer quantity = session.createQuery("""
                    select i.quantity
                    from Inventory i
                    where i.productId = :productId
                    """, Integer.class)
                    .setParameter("productId",productId)
                    .uniqueResult();

            /*
             * Same behavior as the JDBC repository:
             *
             * No inventory row -> return 0.
             */

            return quantity == null ? 0:quantity;
        }
        catch(HibernateException e){
            throw new SQLException("Failed to retrieve inventory stock",e);
        }
    }

    @Override
    public boolean reserveStock(long productId, int quantity) throws SQLException {
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()){
            transaction = session.beginTransaction();

            boolean reserved = reserveStock(session,productId,quantity);

            transaction.commit();
            return reserved;

        }

        catch (SQLException e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw e;

        } catch (HibernateException e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw new SQLException(
                    "Failed to reserve inventory stock",
                    e
            );
        }
    }


    /**
     * Atomically reserves stock using a caller-owned Hibernate Session.
     *
     * This overload will later be used by HibernateCheckoutService so
     * inventory, order, and payment changes can participate in the
     * same Hibernate transaction.
     *
     * This method does NOT commit, roll back, or close the Session.
     */

    public boolean reserveStock(Session session,long productId,int quantity) throws SQLException{
        if(session == null){
            throw new IllegalArgumentException("Session cannot be null");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        try{
            int rowsAffected = session.createMutationQuery("""
                    update Inventory i 
                    set i.quantity = i.quantity - :quantity
                    where i.productId = :productId
                    and i.quantity >= :quantity
                    """)
                    .setParameter("quantity",quantity)
                    .setParameter("productId",productId)
                    .executeUpdate();

            return rowsAffected > 0;
        }
        catch(HibernateException e){
            throw new SQLException("Failed to reserve inventory stock",e);
        }
    }

    @Override
    public void releaseStock(long productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.createMutationQuery(
                            """
                            update Inventory i
                            set i.quantity =
                                i.quantity + :quantity
                            where i.productId = :productId
                            """
                    )
                    .setParameter(
                            "quantity",
                            quantity
                    )
                    .setParameter(
                            "productId",
                            productId
                    )
                    .executeUpdate();

            transaction.commit();

        } catch (HibernateException e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw new SQLException(
                    "Failed to release inventory stock",
                    e
            );
        }
    }

    @Override
    public void setStock(long productId, int quantity) throws SQLException {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Quantity cannot be negative"
            );
        }

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            /*
             * Preserve the V3 UPSERT behavior:
             *
             * No inventory row:
             *     create one.
             *
             * Existing inventory row:
             *     replace quantity.
             */
            session.createNativeMutationQuery("""
                    INSERT INTO inventory
                        (product_id, quantity)
                    VALUES
                        (:productId, :quantity)
                    ON DUPLICATE KEY UPDATE
                        quantity = :quantity
                    """)
                    .setParameter(
                            "productId",
                            productId
                    )
                    .setParameter(
                            "quantity",
                            quantity
                    )
                    .executeUpdate();

            transaction.commit();

        } catch (HibernateException e) {

            if (transaction != null &&
                    transaction.isActive()) {

                transaction.rollback();
            }

            throw new SQLException(
                    "Failed to set inventory stock",
                    e
            );
        }
    }
}