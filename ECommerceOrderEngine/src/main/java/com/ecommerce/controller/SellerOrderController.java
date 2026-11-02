package com.ecommerce.controller;

import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.SellerOrderResponse;

import com.ecommerce.model.OrderItem;

import com.ecommerce.seller.service.SellerOrderService;
import com.ecommerce.seller.service.SellerOrderView;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/*
 * =========================================================
 * SELLER ORDER CONTROLLER
 * ---------------------------------------------------------
 * SELLER_ADMIN sees only order items belonging to products
 * owned by their Seller account.
 *
 * Ownership is determined using JWT userId.
 *
 * The browser never sends sellerId.
 * =========================================================
 */
@RestController
@RequestMapping("/api/seller/orders")
@PreAuthorize("hasRole('SELLER_ADMIN')")
public class SellerOrderController {

    private final SellerOrderService
            sellerOrderService;


    public SellerOrderController(
            SellerOrderService sellerOrderService
    ) {

        this.sellerOrderService =
                sellerOrderService;
    }


    /*
     * =========================================================
     * GET /api/seller/orders
     * ---------------------------------------------------------
     *
     * JWT userId
     *      ↓
     * Seller.user
     *      ↓
     * Product.seller
     *      ↓
     * seller-owned OrderItems only
     * =========================================================
     */
    @GetMapping
    public ResponseEntity<List<SellerOrderResponse>>
    getSellerOrders(
            Authentication authentication
    ) {

        long userAccountId =
                extractUserAccountId(
                        authentication
                );


        List<SellerOrderResponse> response =
                sellerOrderService
                        .findOrdersForSellerUser(
                                userAccountId
                        )

                        .stream()

                        .map(
                                this::toResponse
                        )

                        .toList();


        return ResponseEntity.ok(
                response
        );
    }


    /*
     * =========================================================
     * JWT USER ACCOUNT ID
     * =========================================================
     */
    private long extractUserAccountId(
            Authentication authentication
    ) {

        if (authentication == null) {

            throw new IllegalStateException(
                    "Authentication is required"
            );
        }


        if (!(authentication.getPrincipal()
                instanceof Jwt jwt)) {

            throw new IllegalStateException(
                    "Authenticated principal is not a JWT"
            );
        }


        Number userIdClaim =
                jwt.getClaim(
                        "userId"
                );


        if (userIdClaim == null) {

            throw new IllegalStateException(
                    "Authenticated token does not contain userId"
            );
        }


        return userIdClaim.longValue();
    }


    /*
     * =========================================================
     * SELLER-SAFE RESPONSE MAPPING
     * ---------------------------------------------------------
     * IMPORTANT:
     *
     * We map view.items().
     *
     * We DO NOT map:
     *
     *     view.order().getItems()
     *
     * because the complete Order may contain products
     * belonging to other sellers.
     * =========================================================
     */
    private SellerOrderResponse toResponse(
            SellerOrderView view
    ) {

        List<OrderItemResponse> items =
                view.items()
                        .stream()
                        .map(
                                this::toItemResponse
                        )
                        .toList();


        return new SellerOrderResponse(

                view.order().getId(),

                view.order()
                        .getCustomer()
                        .getName(),

                view.order()
                        .getPaymentType(),

                view.order()
                        .getStatus(),

                view.order()
                        .getCreatedAt(),

                view.sellerSubtotal(),

                items
        );
    }


    private OrderItemResponse toItemResponse(
            OrderItem item
    ) {

        return new OrderItemResponse(

                item.getProduct()
                        .getId(),

                item.getProduct()
                        .getName(),

                item.getQuantity(),

                item.getSubtotal()
        );
    }
}