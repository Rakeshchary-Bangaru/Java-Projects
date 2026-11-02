package com.ecommerce.seller.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


/*
 * =========================================================
 * SELLER ORDER VIEW
 * ---------------------------------------------------------
 * Represents one customer order as visible to ONE seller.
 *
 * items:
 * Only products belonging to that seller.
 *
 * sellerSubtotal:
 * Sum of only that seller's items.
 *
 * It intentionally does not expose the complete Order.items
 * collection.
 * =========================================================
 */
public record SellerOrderView(
        Order order,
        List<OrderItem> items,
        BigDecimal sellerSubtotal
) {

    public SellerOrderView {

        Objects.requireNonNull(
                order,
                "Order is required"
        );

        Objects.requireNonNull(
                items,
                "Seller order items are required"
        );

        Objects.requireNonNull(
                sellerSubtotal,
                "Seller subtotal is required"
        );


        /*
         * Prevent callers from modifying the collection after
         * the view has been created.
         */
        items =
                List.copyOf(
                        items
                );
    }
}