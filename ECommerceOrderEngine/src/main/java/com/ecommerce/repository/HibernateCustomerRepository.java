package com.ecommerce.repository;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.Customer;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class HibernateCustomerRepository
        implements CustomerRepository {

    @Override
    public void save(Customer customer)
            throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            session.persist(customer);

            transaction.commit();

        } catch (HibernateException e) {

            if (transaction != null &&
                    transaction.isActive()) {
                transaction.rollback();
            }

            throw new SQLException(
                    "Failed to save customer",
                    e
            );
        }
    }

    @Override
    public Optional<Customer> findById(long id)
            throws SQLException {

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            Customer customer =
                    session.find(
                            Customer.class,
                            id
                    );

            return Optional.ofNullable(customer);

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to find customer",
                    e
            );
        }
    }

    @Override
    public List<Customer> findAll()
            throws SQLException {

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            return session
                    .createQuery(
                            "from Customer",
                            Customer.class
                    )
                    .getResultList();

        } catch (HibernateException e) {

            throw new SQLException(
                    "Failed to retrieve customers",
                    e
            );
        }
    }

    @Override
    public void update(Customer customer)
            throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            Customer managedCustomer =
                    session.find(
                            Customer.class,
                            customer.getId()
                    );

            if (managedCustomer == null) {
                throw new IllegalArgumentException(
                        "Customer not found: "
                                + customer.getId()
                );
            }

            managedCustomer.updateEmail(
                    customer.getEmail()
            );

            transaction.commit();

        } catch (HibernateException e) {

            if (transaction != null &&
                    transaction.isActive()) {
                transaction.rollback();
            }

            throw new SQLException(
                    "Failed to update customer",
                    e
            );
        }
    }

    @Override
    public boolean deleteById(long id)
            throws SQLException {

        Transaction transaction = null;

        try (Session session =
                     HibernateUtil
                             .getSessionFactory()
                             .openSession()) {

            transaction =
                    session.beginTransaction();

            Customer customer =
                    session.find(
                            Customer.class,
                            id
                    );

            if (customer == null) {
                transaction.commit();
                return false;
            }

            session.remove(customer);

            transaction.commit();

            return true;

        } catch (HibernateException e) {

            if (transaction != null &&
                    transaction.isActive()) {
                transaction.rollback();
            }

            throw new SQLException(
                    "Failed to delete customer",
                    e
            );
        }
    }
}