package com.ecommerce.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents an immutable product entry inside an order.
 *
 * <p>The quantity and unit price are fixed when the OrderItem is created.
 * The unit price is captured from the product at purchase time so that
 * later changes to the product price do not affect historical order totals.</p>
 *
 * <p>The class implements {@link Serializable} because Order objects
 * containing OrderItem instances can be persisted using Java serialization.</p>
 */

@Entity
@Table(name = "order_items")
public  class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private OrderItemId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    @JoinColumn(name = "product_id" , nullable = false)
    private  Product product;

    @Column(name = "quantity" , nullable = false)
    private  int quantity;

    @Column(name = "unit_price" , nullable = false,precision = 10,scale = 2)
    private BigDecimal unitPrice;

    protected  OrderItem(){

    }

    /**
     * Creates an order item for the specified product and quantity.
     *
     * <p>The current product price is captured as the unit price
     * when this object is created.</p>
     *
     * @param product product being purchased
     * @param quantity quantity purchased
     * @throws IllegalArgumentException if the product is null
     *                                  or the quantity is not greater than zero
     */
    public OrderItem(Product product, int quantity) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }

        this.product = product;
        this.quantity = quantity;

        // Capture the purchase-time price so historical order totals remain stable.
        this.unitPrice = BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP);
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice.doubleValue();
    }

    /**
     * Calculates the subtotal for this order item using the
     * captured purchase-time unit price.
     *
     * @return unit price multiplied by quantity
     */
    public double getSubtotal() {
        return unitPrice.doubleValue() * quantity;
    }

    void  assignOrder(Order order){
        if(order == null){
            throw new IllegalArgumentException("Order cannot be null");
        }

        this.order = order;
        this.id = new OrderItemId(order.getId(), product.getId());
    }
}