package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class HibernateOrderRepository implements OrderRepository{

    @Override
    public void save(Order order) throws SQLException {
        if(order == null){
            throw new IllegalArgumentException("Order cannot be null");
        }
        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()){
            Transaction transaction = session.beginTransaction();

            try{
                save(session,order);
                transaction.commit();
            }
            catch(SQLException | RuntimeException e){
                if(transaction.isActive()){
                    transaction.rollback();
                }
                if(e instanceof SQLException sqlException){
                    throw sqlException;
                }
                throw new SQLException("Failed to save order",e);
            }
        }

    }
    /**
     * Saves an order using a caller-owned Hibernate Session.
     *
     * This method does not commit, roll back, or close the Session.
     *
     * HibernateCheckoutService will use this overload so inventory,
     * order items, order, and payment can participate in one
     * Hibernate transaction.
     */

    public void save(Session session , Order order) throws SQLException{
        if(session == null){
            throw new IllegalArgumentException("Session cannot be null");
        }

        if(order == null){
            throw new IllegalArgumentException("Order cannot be null");
        }

        try{
            /*
             * Order.items uses CascadeType.ALL,
             * so persisting the Order also persists
             * its OrderItem entities.
             */
            session.merge(order);
        }
        catch(HibernateException e){
            throw new SQLException("Failed to persist order ",e);
        }
    }
    @Override
    public Optional<Order> findById(long id) throws SQLException {
       try(Session session = HibernateUtil.getSessionFactory().openSession()){
           Order order = session.createQuery("""
                   select distinct o 
                   from com.ecommerce.model.Order o
                   join fetch o.customer
                   left join fetch o.items i
                   left join fetch i.product
                   where o.id = :id""",Order.class)
                   .setParameter("id",id)
                   .uniqueResult();

           return Optional.ofNullable(order);
       }
       catch(HibernateException e){
           throw new SQLException("Failed to find order",e);
       }
    }

    @Override
    public List<Order> findAll() throws SQLException {
        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            return session.createQuery(
                            """
                            select distinct o
                            from com.ecommerce.model.Order o
                            join fetch o.customer
                            left join fetch o.items i
                            left join fetch i.product
                            order by o.id
                            """,
                            Order.class
                    )
                    .getResultList();

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to retrieve orders",
                    e
            );
        }
    }

    @Override
    public void updateStatus(long orderId, OrderStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            Transaction transaction =
                    session.beginTransaction();

            try {

                session.createMutationQuery(
                                """
                                update com.ecommerce.model.Order o
                                set o.status = :status
                                where o.id = :orderId
                                """
                        )
                        .setParameter(
                                "status",
                                status
                        )
                        .setParameter(
                                "orderId",
                                orderId
                        )
                        .executeUpdate();

                transaction.commit();

            } catch (RuntimeException e) {

                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw new SQLException(
                        "Failed to update order status",
                        e
                );
            }
        }
    }

    @Override
    public boolean deleteById(long id) throws SQLException {
        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            Transaction transaction =
                    session.beginTransaction();

            try {

                int rowsAffected =
                        session.createMutationQuery(
                                        """
                                        delete
                                        from com.ecommerce.model.Order o
                                        where o.id = :id
                                        """
                                )
                                .setParameter(
                                        "id",
                                        id
                                )
                                .executeUpdate();

                transaction.commit();

                return rowsAffected > 0;

            } catch (RuntimeException e) {

                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw new SQLException(
                        "Failed to delete order",
                        e
                );
            }
        }
    }
}