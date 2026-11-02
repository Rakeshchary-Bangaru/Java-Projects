package com.ecommerce.repository;

import com.ecommerce.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface SpringDataOrderRepository
        extends JpaRepository<Order, Long> {


    /*
     * =========================================================
     * FIND ONE DETAILED ORDER
     * =========================================================
     */
    @Query("""
            select distinct o
            from com.ecommerce.model.Order o
            join fetch o.customer
            left join fetch o.items i
            left join fetch i.product
            where o.id = :id
            """)
    Optional<Order> findDetailedById(
            @Param("id") long id
    );


    /*
     * =========================================================
     * ROOT / GLOBAL ORDER LIST
     * ---------------------------------------------------------
     * Returns all orders.
     *
     * This must NOT be used for CUSTOMER "My Orders".
     * =========================================================
     */
    @Query("""
            select distinct o
            from com.ecommerce.model.Order o
            join fetch o.customer
            left join fetch o.items i
            left join fetch i.product
            order by o.id
            """)
    List<Order> findAllDetailed();


    /*
     * =========================================================
     * CUSTOMER-OWNED ORDERS
     * ---------------------------------------------------------
     * Returns only orders that belong to one Customer.
     *
     * The customer id will be resolved from the authenticated
     * JWT user account on the backend.
     *
     * The browser will never choose this customer id.
     * =========================================================
     */
    @Query("""
            select distinct o
            from com.ecommerce.model.Order o
            join fetch o.customer
            left join fetch o.items i
            left join fetch i.product
            where o.customer.id = :customerId
            order by o.id desc
            """)
    List<Order> findDetailedByCustomerId(
            @Param("customerId") long customerId
    );
}