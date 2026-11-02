package com.ecommerce.seller.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import com.ecommerce.repository.SpringDataOrderItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/*
 * =========================================================
 * SELLER ORDER SERVICE
 * ---------------------------------------------------------
 * Builds seller-safe order views from repository rows that
 * have ALREADY been restricted to the authenticated seller.
 *
 * SECURITY:
 *
 * This service never calls:
 *
 *     orderRepository.findAll()
 *
 * and never receives sellerId from the browser.
 *
 * Ownership originates from JWT UserAccount id.
 * =========================================================
 */
@Service
public class SellerOrderService {

    private final SpringDataOrderItemRepository
            orderItemRepository;


    public SellerOrderService(
            SpringDataOrderItemRepository orderItemRepository
    ) {

        this.orderItemRepository =
                orderItemRepository;
    }


    /*
     * =========================================================
     * FIND ORDERS FOR SELLER USER
     * ---------------------------------------------------------
     *
     * Example database result:
     *
     * Order 100 -> Laptop
     * Order 100 -> Mouse
     * Order 105 -> Monitor
     *
     * becomes:
     *
     * Order 100
     *   Laptop
     *   Mouse
     *   sellerSubtotal = Laptop + Mouse
     *
     * Order 105
     *   Monitor
     *   sellerSubtotal = Monitor
     *
     * Products belonging to other sellers never enter this
     * method because the repository filters them first.
     * =========================================================
     */
    @Transactional(readOnly = true)
    public List<SellerOrderView> findOrdersForSellerUser(
            long userAccountId
    ) {

        if (userAccountId <= 0) {

            throw new IllegalArgumentException(
                    "User account id must be greater than zero"
            );
        }


        List<OrderItem> sellerItems =
                orderItemRepository
                        .findSellerOrderItemsByUserAccountId(
                                userAccountId
                        );


        /*
         * LinkedHashMap preserves the repository ordering.
         *
         * Repository currently orders:
         *
         * order id DESC
         */
        Map<Long, List<OrderItem>> itemsByOrder =
                new LinkedHashMap<>();


        for (OrderItem item : sellerItems) {

            Order order =
                    item.getOrder();


            itemsByOrder
                    .computeIfAbsent(
                            order.getId(),
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(
                            item
                    );
        }


        List<SellerOrderView> result =
                new ArrayList<>();


        for (
                List<OrderItem> orderItems
                : itemsByOrder.values()
        ) {

            /*
             * Every group contains at least one item because
             * groups are created only while processing rows.
             */
            Order order =
                    orderItems
                            .get(0)
                            .getOrder();


            BigDecimal sellerSubtotal =
                    orderItems
                            .stream()

                            .map(
                                    OrderItem
                                            ::getSubtotalAmount
                            )

                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add
                            )

                            .setScale(2);


            result.add(
                    new SellerOrderView(
                            order,
                            orderItems,
                            sellerSubtotal
                    )
            );
        }


        return List.copyOf(
                result
        );
    }
}