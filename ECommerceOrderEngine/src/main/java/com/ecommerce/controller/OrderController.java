package com.ecommerce.controller;

import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.OrderStatusUpdateRequest;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

import com.ecommerce.service.SpringOrderService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final SpringOrderService orderService;

    public OrderController(
            SpringOrderService orderService) {

        this.orderService = orderService;
    }


    @GetMapping
    public List<OrderResponse> getAllOrders() {

        return orderService
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable long id) {

        Order order =
                orderService.findByIdOrThrow(id);

        return ResponseEntity.ok(
                toResponse(order)
        );
    }


    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable long id,
            @Valid
            @RequestBody
            OrderStatusUpdateRequest request) {

        Order order =
                orderService.updateStatus(
                        id,
                        request.getStatus()
                );

        return ResponseEntity.ok(
                toResponse(order)
        );
    }


    private OrderResponse toResponse(
            Order order) {

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
            OrderItem item) {

        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}