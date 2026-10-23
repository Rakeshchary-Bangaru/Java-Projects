package com.ecommerce.service;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.repository.SpringDataOrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SpringOrderService {

    private final SpringDataOrderRepository orderRepository;

    public SpringOrderService(
            SpringDataOrderRepository orderRepository) {

        this.orderRepository = orderRepository;
    }


    @Transactional
    public Order save(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        return orderRepository.save(order);
    }


    @Transactional(readOnly = true)
    public Order findByIdOrThrow(
            long id) {

        return orderRepository
                .findDetailedById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found: " + id
                        )
                );
    }


    @Transactional(readOnly = true)
    public List<Order> findAll() {

        return orderRepository.findAllDetailed();
    }


    @Transactional
    public Order updateStatus(
            long orderId,
            OrderStatus status) {

        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Order not found: "
                                                + orderId
                                )
                        );

        order.updateStatus(status);

        return order;
    }


    @Transactional
    public boolean deleteById(long id) {

        if (!orderRepository.existsById(id)) {
            return false;
        }

        orderRepository.deleteById(id);

        return true;
    }
}