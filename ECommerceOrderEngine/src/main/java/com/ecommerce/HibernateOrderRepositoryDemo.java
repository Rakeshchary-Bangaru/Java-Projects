package com.ecommerce;

import com.ecommerce.database.HibernateUtil;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;

import java.time.LocalDateTime;
import java.util.List;

public class HibernateOrderRepositoryDemo {

    public static void main(String[] args)
            throws Exception {

        ProductRepository productRepository =
                new HibernateProductRepository();

        CustomerRepository customerRepository =
                new HibernateCustomerRepository();

        OrderRepository orderRepository =
                new HibernateOrderRepository();


        long productId = 940001L;
        long customerId = 940001L;
        long orderId = 940001L;


        // ---------------------------------
        // 1. CREATE TEST PRODUCT
        // ---------------------------------

        Product product =
                new Product(
                        productId,
                        "Hibernate Order Product",
                        "Testing",
                        59.99
                );

        productRepository.save(product);

        System.out.println(
                "PRODUCT CREATED"
        );


        // ---------------------------------
        // 2. CREATE TEST CUSTOMER
        // ---------------------------------

        Customer customer =
                new Customer(
                        customerId,
                        "Hibernate Order Customer",
                        "order.customer@example.com"
                );

        customerRepository.save(customer);

        System.out.println(
                "CUSTOMER CREATED"
        );


        // ---------------------------------
        // 3. CREATE ORDER ITEM
        // ---------------------------------

        OrderItem item =
                new OrderItem(
                        product,
                        2
                );


        // Avoid assuming the exact enum names.
        OrderStatus initialStatus =
                OrderStatus.values()[0];

        PaymentType paymentType =
                PaymentType.CARD;


        // ---------------------------------
        // 4. CREATE ORDER
        // ---------------------------------

        Order order =
                Order.restore(
                        orderId,
                        customer,
                        List.of(item),
                        paymentType,
                        initialStatus,
                        LocalDateTime.now()
                );


        // ---------------------------------
        // 5. SAVE ORDER
        // ---------------------------------

        orderRepository.save(order);

        System.out.println(
                "ORDER SAVED"
        );


        // ---------------------------------
        // 6. FIND BY ID
        // ---------------------------------

        Order found =
                orderRepository
                        .findById(orderId)
                        .orElseThrow();

        System.out.println(
                "FOUND ORDER: "
                        + found.getId()
        );

        System.out.println(
                "CUSTOMER: "
                        + found.getCustomer().getName()
        );

        System.out.println(
                "ITEM COUNT: "
                        + found.getItems().size()
        );

        System.out.println(
                "PRODUCT: "
                        + found
                        .getItems()
                        .get(0)
                        .getProduct()
                        .getName()
        );

        System.out.println(
                "UNIT PRICE: "
                        + found
                        .getItems()
                        .get(0)
                        .getUnitPrice()
        );

        System.out.println(
                "TOTAL: "
                        + found.calculateTotal()
        );


        // ---------------------------------
        // 7. FIND ALL
        // ---------------------------------

        System.out.println(
                "TOTAL ORDERS: "
                        + orderRepository
                        .findAll()
                        .size()
        );


        // ---------------------------------
        // 8. UPDATE STATUS
        // ---------------------------------

        OrderStatus[] statuses =
                OrderStatus.values();

        OrderStatus updatedStatus =
                statuses.length > 1
                        ? statuses[1]
                        : statuses[0];

        orderRepository.updateStatus(
                orderId,
                updatedStatus
        );

        Order updated =
                orderRepository
                        .findById(orderId)
                        .orElseThrow();

        System.out.println(
                "UPDATED STATUS: "
                        + updated.getStatus()
        );


        // ---------------------------------
        // 9. DELETE ORDER
        // ---------------------------------

        boolean deleted =
                orderRepository.deleteById(
                        orderId
                );

        System.out.println(
                "ORDER DELETED: "
                        + deleted
        );

        System.out.println(
                "ORDER EXISTS AFTER DELETE: "
                        + orderRepository
                        .findById(orderId)
                        .isPresent()
        );


        // ---------------------------------
        // 10. CLEAN UP PRODUCT + CUSTOMER
        // ---------------------------------

        productRepository.deleteById(
                productId
        );

        customerRepository.deleteById(
                customerId
        );

        System.out.println(
                "TEST DATA CLEANED UP"
        );


        HibernateUtil.shutdown();
    }
}