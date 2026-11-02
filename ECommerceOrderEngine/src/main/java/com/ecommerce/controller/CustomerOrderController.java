package com.ecommerce.controller;

import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderResponse;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import com.ecommerce.service.CustomerService;
import com.ecommerce.service.SpringOrderService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/customer/orders")
public class CustomerOrderController {

    private final CustomerService customerService;

    private final SpringOrderService orderService;


    public CustomerOrderController(
            CustomerService customerService,
            SpringOrderService orderService
    ) {

        this.customerService =
                customerService;

        this.orderService =
                orderService;
    }


    /*
     * =========================================================
     * GET /api/customer/orders
     * ---------------------------------------------------------
     * Returns only orders belonging to the currently
     * authenticated CUSTOMER.
     *
     * SECURITY:
     *
     * The browser does NOT send:
     *
     * customerId
     *
     * Instead:
     *
     * JWT userId
     *      ↓
     * UserAccount
     *      ↓
     * linked Customer
     *      ↓
     * customer's orders
     * =========================================================
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ORDER_READ_OWN')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication
    ) {

        long userAccountId =
                extractUserAccountId(
                        authentication
                );


        /*
         * Resolve the Customer profile that belongs to
         * the authenticated UserAccount.
         */
        Customer customer =
                customerService
                        .findByUserAccountIdOrThrow(
                                userAccountId
                        );


        /*
         * Repository query is filtered by the resolved
         * Customer id.
         */
        List<OrderResponse> orders =
                orderService
                        .findByCustomerId(
                                customer.getId()
                        )
                        .stream()
                        .map(this::toResponse)
                        .toList();


        return ResponseEntity.ok(
                orders
        );
    }


    /*
     * =========================================================
     * JWT USER ID
     * ---------------------------------------------------------
     * Extracts the stable UserAccount identifier from the
     * authenticated JWT.
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
     * ORDER RESPONSE MAPPING
     * =========================================================
     */
    private OrderResponse toResponse(
            Order order
    ) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();


        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getPaymentType(),
                order.getStatus(),
                order.getCreatedAt(),
                order.calculateTotal(),
                items
        );
    }


    private OrderItemResponse toItemResponse(
            OrderItem item
    ) {

        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}