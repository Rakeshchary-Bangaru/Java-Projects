package com.ecommerce.model;

import jakarta.persistence.*;

import com.ecommerce.security.model.UserAccount;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer in the e-commerce system.
 *
 * <p>A customer has an immutable identifier and name, while the email
 * address can be updated when required.</p>
 *
 * <p>The class implements {@link Serializable} so customer information
 * can be included when an {@link Order} is serialized.</p>
 */

@Entity
@Table(name = "customers")
public  class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    private  long id;

    @Column(name = "name" , nullable = false)
    private  String name;

    @Column(name="email" , nullable = false,unique = true)
    private String email;

    /*
     * Links this customer profile to the authenticated user account.
     *
     * Nullable for legacy customers that existed before V7 security.
     * New V7 customer accounts will always receive this relationship.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_account_id",
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_customers_user_account"
            )
    )
    private UserAccount userAccount;

    @OneToMany(mappedBy = "customer")
    private List<Order> orders = new ArrayList<>();

    protected Customer(){

    }

    /**
     * Creates a customer with the specified identifier, name, and email.
     *
     * @param id unique customer identifier
     * @param name customer name
     * @param email customer email address
     * @throws IllegalArgumentException if the id is not greater than zero,
     *                                  the name is null or blank,
     *                                  or the email is null or blank
     */
    public Customer(long id, String name, String email) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Customer id must be greater than zero"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer name cannot be empty"
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        this.id = id;
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    /*
     * Associates this customer profile with its authenticated account.
     *
     * A customer profile must never be linked to a null user when
     * establishing the V7 authentication relationship.
     */
    public void assignUserAccount(
            UserAccount userAccount) {

        if (userAccount == null) {
            throw new IllegalArgumentException(
                    "User account cannot be null"
            );
        }

        this.userAccount = userAccount;
    }

    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    /**
     * Updates the customer's email address.
     *
     * @param newEmail new email address
     * @throws IllegalArgumentException if the email is null or blank
     */
    public void updateEmail(String newEmail) {

        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        this.email = newEmail;
    }

    public void updateDetails(
            String name,
            String email) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer name cannot be empty"
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer email cannot be empty"
            );
        }

        this.name = name;
        this.email = email;
    }

    public void addOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        orders.add(order);
        order.assignCustomer(this);
    }
}