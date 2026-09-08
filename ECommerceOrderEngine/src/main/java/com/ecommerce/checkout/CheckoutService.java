package com.ecommerce.checkout;

import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.inventory.InventoryService;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.notification.OrderEventPublisher;
import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates the complete checkout workflow.
 *
 * <p>The service validates the checkout request, reserves inventory,
 * converts cart items into order items, processes payment, creates the
 * order, updates its status, and publishes the completed order event.</p>
 *
 * <p>If checkout fails after inventory has been reserved, the service
 * restores the reserved stock before propagating the failure.</p>
 */
public final class CheckoutService {

    private final InventoryService inventoryService;
    private final PaymentProcessor paymentProcessor;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * Creates a checkout service with its required dependencies.
     *
     * @param inventoryService service responsible for inventory operations
     * @param paymentProcessor processor responsible for executing payments
     * @param orderEventPublisher publisher used to notify order observers
     * @throws IllegalArgumentException if any dependency is null
     */
    public CheckoutService(
            InventoryService inventoryService,
            PaymentProcessor paymentProcessor,
            OrderEventPublisher orderEventPublisher) {

        if (inventoryService == null) {
            throw new IllegalArgumentException(
                    "Inventory service cannot be null"
            );
        }

        if (paymentProcessor == null) {
            throw new IllegalArgumentException(
                    "Payment processor cannot be null"
            );
        }

        if (orderEventPublisher == null) {
            throw new IllegalArgumentException(
                    "Order event publisher cannot be null"
            );
        }

        this.inventoryService = inventoryService;
        this.paymentProcessor = paymentProcessor;
        this.orderEventPublisher = orderEventPublisher;
    }

    /**
     * Validates that a cart exists and contains at least one item.
     *
     * @param cart cart to validate
     * @throws IllegalArgumentException if the cart is null or empty
     */
    public void validateCart(Cart cart) {

        if (cart == null) {
            throw new IllegalArgumentException(
                    "Cart cannot be null"
            );
        }

        if (cart.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cart cannot be empty"
            );
        }
    }

    /**
     * Reserves inventory for every item in the cart.
     *
     * <p>If reservation fails part-way through the cart, only the items
     * that were successfully reserved are released before the original
     * exception is rethrown.</p>
     *
     * @param cart cart whose inventory should be reserved
     */
    private void reserveInventory(Cart cart) {

        List<CartItem> reservedItems = new ArrayList<>();

        try {
            for (CartItem item : cart.getItems()) {

                inventoryService.reserveStock(
                        item.getProduct().getId(),
                        item.getQuantity()
                );

                reservedItems.add(item);
            }

        } catch (RuntimeException e) {

            // Roll back only the items that were successfully reserved
            // before the later reservation failed.
            for (CartItem reservedItem : reservedItems) {
                inventoryService.releaseStock(
                        reservedItem.getProduct().getId(),
                        reservedItem.getQuantity()
                );
            }

            throw e;
        }
    }

    /**
     * Converts mutable cart items into order items.
     *
     * <p>Creating an {@link OrderItem} captures the product's current
     * price so the resulting order retains its purchase-time price.</p>
     *
     * @param cart cart containing the items being purchased
     * @return order items created from the cart
     */
    private List<OrderItem> createOrderItems(Cart cart) {

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {

            OrderItem orderItem = new OrderItem(
                    cartItem.getProduct(),
                    cartItem.getQuantity()
            );

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    /**
     * Releases all inventory associated with the cart.
     *
     * <p>This method is used when inventory reservation succeeded but
     * a later checkout operation fails.</p>
     *
     * @param cart cart whose reserved inventory should be restored
     */
    private void releaseInventory(Cart cart) {

        for (CartItem item : cart.getItems()) {
            inventoryService.releaseStock(
                    item.getProduct().getId(),
                    item.getQuantity()
            );
        }
    }

    /**
     * Processes a complete checkout for a customer.
     *
     * <p>The workflow is:</p>
     *
     * <pre>
     * Validate request
     *      ↓
     * Reserve inventory
     *      ↓
     * Create order items
     *      ↓
     * Calculate total
     *      ↓
     * Process payment
     *      ↓
     * Create order
     *      ↓
     * Mark order as PAID
     *      ↓
     * Publish order event
     * </pre>
     *
     * <p>If payment or another operation fails after all inventory has
     * been reserved, the reserved inventory is restored.</p>
     *
     * @param orderId unique identifier for the new order
     * @param customer customer placing the order
     * @param cart cart containing the products being purchased
     * @return successfully created and paid order
     * @throws IllegalArgumentException if the order ID is invalid,
     *                                  customer is null,
     *                                  or cart is invalid
     * @throws PaymentFailedException if payment is unsuccessful
     * @throws RuntimeException if another checkout operation fails
     */
    public Order checkout(
            long orderId,
            Customer customer,
            Cart cart) {

        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be greater than zero"
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        validateCart(cart);

        /*
         * reserveInventory() handles its own partial rollback.
         * Therefore it stays outside the try block below.
         */
        reserveInventory(cart);

        try {

            List<OrderItem> orderItems =
                    createOrderItems(cart);

            double total = orderItems.stream()
                    .mapToDouble(OrderItem::getSubtotal)
                    .sum();

            PaymentResult paymentResult =
                    paymentProcessor.processPayment(total);

            if (!paymentResult.isSuccessful()) {
                throw new PaymentFailedException(
                        paymentResult.getMessage()
                );
            }

            Order order = new Order.Builder()
                    .id(orderId)
            .customer(customer)
            .items(orderItems)
            .paymentType(paymentResult.getPaymentType())
                    .build();


            order.updateStatus(OrderStatus.PAID);

            orderEventPublisher.notifyObservers(order);

            return order;

        } catch (RuntimeException e) {

            // All inventory was already reserved successfully.
            // Restore it because a later checkout step failed.
            releaseInventory(cart);

            throw e;
        }
    }
}