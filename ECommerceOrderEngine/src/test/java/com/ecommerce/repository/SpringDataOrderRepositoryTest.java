package com.ecommerce.repository;

import com.ecommerce.EcommerceApplication;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EcommerceApplication.class)
@Transactional
class SpringDataOrderRepositoryTest {

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private SpringDataProductRepository productRepository;

    @Autowired
    private SpringDataCustomerRepository customerRepository;

    @Autowired
    private EntityManager entityManager;


    @Test
    void shouldSaveAndRetrieveDetailedOrderGraph() {

        long productId = 990004L;
        long customerId = 990005L;
        long orderId = 990006L;


        Product product =
                new Product(
                        productId,
                        "Order Test Keyboard",
                        "Electronics",
                        100.00
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Order Test Customer",
                        "ordertest@test.com"
                );


        productRepository.save(product);
        customerRepository.save(customer);


        OrderItem item =
                new OrderItem(
                        product,
                        2
                );


        /*
         * We only need a valid PaymentType for this test.
         * This avoids assuming a particular enum constant name.
         */
        PaymentType paymentType =
                PaymentType.values()[0];


        Order order =
                new Order.Builder()
                        .id(orderId)
                        .customer(customer)
                        .items(List.of(item))
                        .paymentType(paymentType)
                        .build();


        // CREATE ORDER + ORDER ITEM
        orderRepository.saveAndFlush(order);


        /*
         * Clear the persistence context.
         *
         * This is important:
         * we do not want the next lookup simply returning
         * objects that are already managed in memory.
         */
        entityManager.clear();


        // LOAD COMPLETE GRAPH
        Order found =
                orderRepository
                        .findDetailedById(orderId)
                        .orElseThrow();


        /*
         * Detach everything again.
         *
         * Now these assertions prove the fetch join
         * actually loaded the relationships.
         */
        entityManager.clear();


        assertEquals(
                orderId,
                found.getId()
        );

        assertEquals(
                customerId,
                found.getCustomer().getId()
        );

        assertEquals(
                1,
                found.getItems().size()
        );

        OrderItem foundItem =
                found.getItems().get(0);

        assertEquals(
                productId,
                foundItem.getProduct().getId()
        );

        assertEquals(
                2,
                foundItem.getQuantity()
        );

        assertEquals(
                200.00,
                found.calculateTotal(),
                0.001
        );
    }
}