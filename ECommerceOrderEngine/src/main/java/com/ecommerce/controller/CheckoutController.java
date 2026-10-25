package com.ecommerce.controller;

import com.ecommerce.checkout.SpringCheckoutService;

import com.ecommerce.dto.CheckoutItemRequest;
import com.ecommerce.dto.CheckoutRequest;
import com.ecommerce.dto.OrderItemResponse;
import com.ecommerce.dto.OrderResponse;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;

import com.ecommerce.service.CustomerService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.SpringOrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final SpringCheckoutService checkoutService;
    private final CustomerService customerService;
    private final ProductService productService;
    private final SpringOrderService orderService;


    public CheckoutController(
            SpringCheckoutService checkoutService,
            CustomerService customerService,
            ProductService productService,
            SpringOrderService orderService) {

        this.checkoutService = checkoutService;
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
    }


    @PostMapping
    public ResponseEntity<OrderResponse> checkout(
            @Valid
            @RequestBody CheckoutRequest request) {

        Customer customer =
                customerService.findByIdOrThrow(
                        request.getCustomerId()
                );


        Cart cart = new Cart();

        for (CheckoutItemRequest item :
                request.getItems()) {

            Product product =
                    productService.findByIdOrThrow(
                            item.getProductId()
                    );

            cart.addProduct(
                    product,
                    item.getQuantity()
            );
        }


        Order order =
                checkoutService.checkout(
                        request.getOrderId(),
                        customer,
                        cart,
                        request.getPaymentType()
                );


        /*
         * Reload using the detailed fetch query so customer,
         * items and products are ready for response mapping.
         */
        Order detailedOrder =
                orderService.findByIdOrThrow(
                        order.getId()
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        toResponse(detailedOrder)
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