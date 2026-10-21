package com.ecommerce.checkout;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Payment;
import com.ecommerce.model.PaymentType;


import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentProcessorProvider;
import com.ecommerce.payment.PaymentResult;

import com.ecommerce.repository.SpringDataInventoryRepository;
import com.ecommerce.repository.SpringDataOrderRepository;
import com.ecommerce.repository.SpringDataPaymentRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
public class SpringCheckoutService {

    private final SpringDataInventoryRepository inventoryRepository;
    private final SpringDataOrderRepository orderRepository;
    private final SpringDataPaymentRepository paymentRepository;

    private final PaymentProcessorProvider
            paymentProcessorProvider;




    /*
     * Package-private constructor for deterministic testing.
     */
    public SpringCheckoutService(
            SpringDataInventoryRepository inventoryRepository,
            SpringDataOrderRepository orderRepository,
            SpringDataPaymentRepository paymentRepository,
            PaymentProcessorProvider paymentProcessorProvider) {

        if (inventoryRepository == null) {
            throw new IllegalArgumentException(
                    "Inventory repository cannot be null"
            );
        }

        if (orderRepository == null) {
            throw new IllegalArgumentException(
                    "Order repository cannot be null"
            );
        }

        if (paymentRepository == null) {
            throw new IllegalArgumentException(
                    "Payment repository cannot be null"
            );
        }

        if (paymentProcessorProvider == null) {
            throw new IllegalArgumentException(
                    "Payment processor provider cannot be null"
            );
        }

        this.inventoryRepository =
                inventoryRepository;

        this.orderRepository =
                orderRepository;

        this.paymentRepository =
                paymentRepository;

        this.paymentProcessorProvider =
                paymentProcessorProvider;
    }


    @Transactional
    public Order checkout(
            long orderId,
            Customer customer,
            Cart cart,
            PaymentType paymentType) {

        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order id must be greater than 0"
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart cannot be null or empty"
            );
        }

        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Payment type cannot be null"
            );
        }


        // Convert CartItems into historical OrderItems.
        List<OrderItem> orderItems =
                cart.getItems()
                        .stream()
                        .map(item ->
                                new OrderItem(
                                        item.getProduct(),
                                        item.getQuantity()
                                )
                        )
                        .toList();


        /*
         * Atomically reserve every product.
         *
         * If any reservation fails, an exception is thrown.
         * Because checkout() is @Transactional,
         * previous successful reservations are rolled back.
         */
        for (CartItem item : cart.getItems()) {

            int rowsAffected =
                    inventoryRepository.reserveStock(
                            item.getProduct().getId(),
                            item.getQuantity()
                    );

            if (rowsAffected == 0) {

                throw new InsufficientStockException(
                        "Insufficient stock for product "
                                + item.getProduct().getId()
                );
            }
        }


        double total =
                orderItems.stream()
                        .mapToDouble(
                                OrderItem::getSubtotal
                        )
                        .sum();


        PaymentProcessor paymentProcessor =
                paymentProcessorProvider.get(
                        paymentType
                );
        if (paymentProcessor == null) {
            throw new IllegalStateException(
                    "Payment processor factory returned null"
            );
        }


        PaymentResult paymentResult =
                paymentProcessor.processPayment(
                        total
                );


        if (paymentResult.getPaymentType()
                != paymentType) {

            throw new IllegalStateException(
                    "Payment result type does not match requested payment type"
            );
        }


        if (!paymentResult.isSuccessful()) {

            throw new PaymentFailedException(
                    paymentResult.getMessage()
            );
        }


        Order order =
                new Order.Builder()
                        .id(orderId)
                        .customer(customer)
                        .items(orderItems)
                        .paymentType(paymentType)
                        .build();

        order.updateStatus(
                OrderStatus.PAID
        );


        /*
         * save() may use merge because Order has
         * an application-assigned ID.
         *
         * Therefore use the returned managed Order.
         */
        Order savedOrder =
                orderRepository.save(order);


        Payment payment =
                new Payment(
                        savedOrder,
                        paymentResult.getPaymentType(),
                        paymentResult.getAmount(),
                        paymentResult.isSuccessful(),
                        paymentResult.getMessage()
                );

        paymentRepository.save(payment);


        return savedOrder;
    }
}