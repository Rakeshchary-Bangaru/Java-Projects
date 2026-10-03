package com.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderItemId implements Serializable{

    @Column(name = "order_id")
    private long orderId;

    @Column(name = "product_id")
    private long productId;

    protected OrderItemId(){

    }

    public OrderItemId(long orderId, long productId){
        this.orderId = orderId;
        this.productId = productId;
    }

    public long getOrderId(){
        return orderId;
    }

    public long getProductId(){
        return productId;
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if(!(o instanceof OrderItemId that)){
            return false;
        }

        return orderId == that.orderId && productId == that.productId ;
    }

    @Override
    public int hashCode(){
        return Objects.hash(orderId,productId);
    }
}
