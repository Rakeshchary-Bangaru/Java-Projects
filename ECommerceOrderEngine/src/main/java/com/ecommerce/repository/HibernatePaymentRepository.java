package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.payment.PaymentResult;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class HibernatePaymentRepository
        implements PaymentRepository {

    @Override
    public void save(
            long orderId,
            PaymentResult paymentResult
    ) throws SQLException {

        if (paymentResult == null) {
            throw new IllegalArgumentException(
                    "Payment result cannot be null"
            );
        }

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            save(
                    session,
                    orderId,
                    paymentResult
            );

            transaction.commit();

        } catch (SQLException e) {

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
                    "Failed to save payment",
                    e
            );
        }
    }


    /**
     * Saves a payment using a caller-owned Hibernate Session.
     *
     * This method does not commit, roll back, or close the Session.
     * HibernateCheckoutService will use this overload so inventory,
     * order, order items, and payment all participate in one transaction.
     */
    public void save(
            Session session,
            long orderId,
            PaymentResult paymentResult
    ) throws SQLException {

        if (session == null) {
            throw new IllegalArgumentException(
                    "Session cannot be null"
            );
        }

        if (paymentResult == null) {
            throw new IllegalArgumentException(
                    "Payment result cannot be null"
            );
        }

        try {

            /*
             * We only need an Order reference for the foreign key.
             *
             * Hibernate can associate the Payment with the existing
             * order without manually writing order_id.
             */
            Order order =
                    session.getReference(
                            Order.class,
                            orderId
                    );

            Payment payment =
                    new Payment(
                            order,
                            paymentResult.getPaymentType(),
                            paymentResult.getAmount(),
                            paymentResult.isSuccessful(),
                            paymentResult.getMessage()
                    );

            session.persist(payment);

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to persist payment",
                    e
            );
        }
    }


    @Override
    public List<PaymentResult> findByOrderId(
            long orderId
    ) throws SQLException {

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            return session.createQuery(
                            """
                            select p
                            from Payment p
                            where p.order.id = :orderId
                            order by p.id
                            """,
                            Payment.class
                    )
                    .setParameter(
                            "orderId",
                            orderId
                    )
                    .getResultList()
                    .stream()
                    .map(this::mapPaymentResult)
                    .toList();

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to retrieve payments",
                    e
            );
        }
    }


    @Override
    public Optional<PaymentResult> findLatestByOrderId(
            long orderId
    ) throws SQLException {

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            return session.createQuery(
                            """
                            select p
                            from Payment p
                            where p.order.id = :orderId
                            order by p.id desc
                            """,
                            Payment.class
                    )
                    .setParameter(
                            "orderId",
                            orderId
                    )
                    .setMaxResults(1)
                    .uniqueResultOptional()
                    .map(this::mapPaymentResult);

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to retrieve latest payment",
                    e
            );
        }
    }


    private PaymentResult mapPaymentResult(
            Payment payment
    ) {

        return new PaymentResult(
                payment.isSuccessful(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getMessage()
        );
    }
}