package com.ecommerce.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an order created during the checkout process.
 *
 * <p>An order contains a customer, purchased items, payment type,
 * creation time, and current {@link OrderStatus}.</p>
 *
 * <p>The order starts with the {@link OrderStatus#CREATED} status.
 * Its status can later be updated as the order progresses through
 * the order lifecycle.</p>
 *
 * <p>The list of order items is defensively copied when the order
 * is created so external code cannot directly modify the internal
 * list structure.</p>
 *
 * <p>The class implements {@link Serializable} so orders can be
 * persisted using Java object serialization in Core Java V1.</p>
 */
@Entity
@Table(name = "orders")
public  class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private  long id;

    @ManyToOne
    @JoinColumn(name="customer_id",nullable = false)
    private  Customer customer;

    @OneToMany(mappedBy = "order",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
    private  List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name="payment_type" , nullable = false)
    private  PaymentType paymentType;

    @Column(name = "created_at" , nullable = false)
    private  LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name="status" , nullable = false)
    private OrderStatus status;

    protected Order(){

    }

    /**
     * Creates an order with the supplied customer, items, and payment type.
     *
     * <p>The order is initially assigned the
     * {@link OrderStatus#CREATED} status and the current date and time.</p>
     *
     * @param id unique identifier of the order
     * @param customer customer placing the order
     * @param items items included in the order
     * @param paymentType payment method selected for the order
     * @throws IllegalArgumentException if the id is not greater than zero,
     *                                  customer is null,
     *                                  items are null or empty,
     *                                  or payment type is null
     */



    public Order(
            long id,
            Customer customer,
            List<OrderItem> items,
            PaymentType paymentType) {

        this(
                id,
                customer,
                items,
                paymentType,
                OrderStatus.CREATED,
                LocalDateTime.now()
        );
    }
    private Order(
            long id,
            Customer customer,
            List<OrderItem> items,
            PaymentType paymentType,
            OrderStatus status,
            LocalDateTime createdAt) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Order id must be greater than zero"
            );
        }

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order must contain at least one item"
            );
        }

        if (paymentType == null) {
            throw new IllegalArgumentException(
                    "Payment type cannot be null"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        if (createdAt == null) {
            throw new IllegalArgumentException(
                    "Created time cannot be null"
            );
        }

        this.id = id;
        this.customer = customer;
        this.items = new ArrayList<>();
        for(OrderItem item : items){
            addItem(item);
        }
        this.paymentType = paymentType;
        this.status = status;
        this.createdAt = createdAt;
    }
    private void addItem(OrderItem item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Order item cannot be null"
            );
        }

        item.assignOrder(this);
        this.items.add(item);
    }
    public long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    /**
     * Returns an unmodifiable copy of the order items so callers
     * cannot directly modify the internal Hibernate-managed collection.
     */

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Updates the current status of the order.
     *
     * @param newStatus new order status
     * @throws IllegalArgumentException if the new status is null
     */
    public void updateStatus(OrderStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Order status cannot be null"
            );
        }

        this.status = newStatus;
    }

    /**
     * Calculates the total monetary value of the order.
     *
     * @return sum of all {@link OrderItem} subtotals
     */
    public double calculateTotal() {
        return items.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }
    /**
     * Reconstructs an existing order from persisted state.
     *
     * <p>Unlike the public constructor and {@link Builder}, this method
     * preserves the stored order status and creation time instead of
     * assigning {@link OrderStatus#CREATED} and the current time.</p>
     *
     * <p>This method is primarily used by database repositories when
     * rebuilding an order retrieved from persistent storage.</p>
     *
     * @param id unique identifier of the order
     * @param customer customer who placed the order
     * @param items persisted order items
     * @param paymentType payment method used for the order
     * @param status persisted order status
     * @param createdAt persisted creation time
     * @return reconstructed order
     * @throws IllegalArgumentException if any required order data is invalid
     */

    public static Order restore(
            long id,
            Customer customer,
            List<OrderItem> items,
            PaymentType paymentType,
            OrderStatus status,
            LocalDateTime createdAt) {

        return new Order(
                id,
                customer,
                items,
                paymentType,
                status,
                createdAt
        );
    }

    /**
     * Builder used to construct {@link Order} objects in a readable
     * step-by-step manner.
     *
     * <p>The final validation is performed by the {@link Order}
     * constructor when {@link #build()} is called.</p>
     */
    public static class Builder {

        private long id;
        private Customer customer;
        private List<OrderItem> items;
        private PaymentType paymentType;

        /**
         * Sets the order identifier.
         *
         * @param id order identifier
         * @return this builder
         */
        public Builder id(long id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the customer placing the order.
         *
         * @param customer customer placing the order
         * @return this builder
         */
        public Builder customer(Customer customer) {
            this.customer = customer;
            return this;
        }

        /**
         * Sets the items included in the order.
         *
         * @param items order items
         * @return this builder
         */
        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        /**
         * Sets the payment type used for the order.
         *
         * @param paymentType payment type
         * @return this builder
         */
        public Builder paymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        /**
         * Creates the final {@link Order}.
         *
         * @return newly constructed order
         * @throws IllegalArgumentException if any required order data
         *                                  is invalid or missing
         */
        public Order build() {
            return new Order(
                    id,
                    customer,
                    items,
                    paymentType
            );
        }
    }

    void assignCustomer(Customer customer) {
        this.customer = customer;
    }
}