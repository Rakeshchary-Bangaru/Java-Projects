package com.ecommerce.controller;

import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.OrderStatusUpdateRequest;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import com.ecommerce.service.SpringOrderService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/*
 * =========================================================
 * ROOT ORDER ADMINISTRATION
 * ---------------------------------------------------------
 * ROOT has global visibility across all customer orders.
 *
 * SELLER_ADMIN must NOT use this controller because sellers
 * will receive only the portions of orders containing their
 * own products.
 * =========================================================
 */
@RestController
@RequestMapping("/api/root/orders")
@PreAuthorize("hasRole('ROOT')")
public class RootOrderController {

    private final SpringOrderService orderService;


    public RootOrderController(
            SpringOrderService orderService
    ) {

        this.orderService =
                orderService;
    }


    /*
     * =========================================================
     * GET ALL ORDERS
     * ---------------------------------------------------------
     * ROOT only.
     * =========================================================
     */
    @GetMapping
    public List<OrderResponse> getAllOrders() {

        return orderService
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    /*
     * =========================================================
     * GET ONE ORDER
     * ---------------------------------------------------------
     * ROOT may inspect any order.
     * =========================================================
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable long id
    ) {

        Order order =
                orderService
                        .findByIdOrThrow(
                                id
                        );


        return ResponseEntity.ok(
                toResponse(order)
        );
    }


    /*
     * =========================================================
     * UPDATE ORDER STATUS
     * ---------------------------------------------------------
     * Global order-status administration is currently reserved
     * for ROOT.
     *
     * Seller-specific status permissions can be designed
     * separately once seller ownership filtering is in place.
     * =========================================================
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable long id,

            @Valid
            @RequestBody
            OrderStatusUpdateRequest request
    ) {

        Order order =
                orderService
                        .updateStatus(
                                id,
                                request.getStatus()
                        );


        return ResponseEntity.ok(
                toResponse(order)
        );
    }


    /*
     * =========================================================
     * RESPONSE MAPPING
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