package com.ecommerce.repository;

import com.ecommerce.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataOrderRepository
        extends JpaRepository<Order, Long> {

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


    @Query("""
            select distinct o
            from com.ecommerce.model.Order o
            join fetch o.customer
            left join fetch o.items i
            left join fetch i.product
            order by o.id
            """)
    List<Order> findAllDetailed();
}