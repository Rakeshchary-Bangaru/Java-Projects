package com.ecommerce.repository;

import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderItemId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


/*
 * =========================================================
 * SPRING DATA ORDER ITEM REPOSITORY
 * ---------------------------------------------------------
 * Used for ownership-sensitive order-item queries.
 *
 * Seller order visibility is based on:
 *
 * JWT UserAccount id
 *        ↓
 * Seller.user
 *        ↓
 * Product.seller
 *        ↓
 * OrderItem.product
 *
 * This prevents SELLER_ADMIN from receiving order items
 * owned by another seller.
 * =========================================================
 */
public interface SpringDataOrderItemRepository
        extends JpaRepository<OrderItem, OrderItemId> {


    /*
     * =========================================================
     * SELLER ORDER ITEMS
     * ---------------------------------------------------------
     * Returns ONLY order items whose product belongs to the
     * Seller account associated with the authenticated
     * UserAccount.
     *
     * IMPORTANT:
     *
     * We deliberately query OrderItem rather than returning
     * complete Orders.
     *
     * An Order may contain products belonging to multiple
     * sellers. Returning the complete Order.items collection
     * could expose another seller's products and revenue.
     *
     * No sellerId comes from the browser.
     * =========================================================
     */
    @Query("""
        select i
        from OrderItem i
        join fetch i.order o
        join fetch o.customer
        join fetch i.product p
        where i.sellerId in (
            select s.id
            from Seller s
            where s.user.id = :userAccountId
        )
        order by o.id desc, p.id
        """)
    List<OrderItem> findSellerOrderItemsByUserAccountId(
            @Param("userAccountId")
            long userAccountId
    );
}