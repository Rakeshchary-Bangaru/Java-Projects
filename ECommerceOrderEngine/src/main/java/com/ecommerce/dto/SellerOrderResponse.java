package com.ecommerce.dto;

import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


/*
 * =========================================================
 * SELLER ORDER RESPONSE
 * ---------------------------------------------------------
 * Represents one customer order as visible to ONE seller.
 *
 * IMPORTANT:
 *
 * items contains only products owned by that seller.
 *
 * sellerSubtotal contains only revenue belonging to that
 * seller.
 * =========================================================
 */
public record SellerOrderResponse(

        long orderId,

        String customerName,

        PaymentType paymentType,

        OrderStatus status,

        LocalDateTime createdAt,

        BigDecimal sellerSubtotal,

        List<OrderItemResponse> items

) {
}