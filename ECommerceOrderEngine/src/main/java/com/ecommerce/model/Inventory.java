package com.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name ="inventory")
public class Inventory{

    @Id@Column(name="product_id")
    private long productId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id",
                nullable = false)
    private Product product;

    @Column(name = "quantity",
            nullable = false)
    private int quantity;

    protected Inventory(){

    }

    public Inventory(Product product,int quantity){
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");


        }

        if(quantity < 0){
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.product = product;
        this.quantity = quantity;
    }

    public long getProductId(){
        return productId;
    }

    public Product getProduct(){
        return product;
    }

    public int getQuantity(){
        return quantity;
    }

    public void addStock(int quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        this.quantity += quantity;
    }

    public void setStock(int quantity){
        if(quantity < 0){
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.quantity = quantity;
    }

}