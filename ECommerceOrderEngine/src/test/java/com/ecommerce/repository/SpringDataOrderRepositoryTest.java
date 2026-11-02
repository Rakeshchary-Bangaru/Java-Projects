
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

import java.math.BigDecimal;
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

        /*
         * Only product IDs are generated automatically.
         *
         * Customer and Order IDs retain their
         * manually assigned values.
         */
        long customerId = 990005L;
        long orderId = 990006L;

        /*
         * =====================================================
         * CREATE PRODUCT WITHOUT ID
         * =====================================================
         */
        Product product = new Product(
                "Order Test Keyboard",
                "Electronics",
                new BigDecimal("100.00"),
                "Keyboard used for order repository integration testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Order Test Customer",
                "ordertest@test.com"
        );

        /*
         * =====================================================
         * SAVE PRODUCT AND CUSTOMER
         * =====================================================
         *
         * saveAndFlush() ensures the Product row exists
         * before creating an OrderItem referencing it.
         */
        Product savedProduct =
                productRepository.saveAndFlush(product);

        assertNotNull(savedProduct);
        assertNotNull(savedProduct.getId());
        assertTrue(savedProduct.getId() > 0);

        long productId = savedProduct.getId();

        customerRepository.saveAndFlush(customer);

        /*
         * =====================================================
         * CREATE ORDER ITEM
         * =====================================================
         */
        OrderItem item = new OrderItem(
                savedProduct,
                2
        );

        /*
         * Use a valid payment type from the enum.
         * This preserves the original test behavior.
         */
        PaymentType paymentType =
                PaymentType.values()[0];

        /*
         * =====================================================
         * CREATE ORDER
         * =====================================================
         */
        Order order = new Order.Builder()
                .id(orderId)
                .customer(customer)
                .items(List.of(item))
                .paymentType(paymentType)
                .build();

        /*
         * =====================================================
         * SAVE ORDER AND ORDER ITEM
         * =====================================================
         */
        orderRepository.saveAndFlush(order);

        /*
         * =====================================================
         * CLEAR PERSISTENCE CONTEXT
         * =====================================================
         *
         * Force Hibernate to load the Order graph
         * from the database rather than returning
         * an already managed entity.
         */
        entityManager.clear();

        /*
         * =====================================================
         * LOAD COMPLETE ORDER GRAPH
         * =====================================================
         */
        Order found = orderRepository
                .findDetailedById(orderId)
                .orElseThrow();

        /*
         * Initialize the product ID while the persistence
         * context is available.
         *
         * The ID of a proxy is generally accessible without
         * initialization, but explicitly capturing it before
         * detachment makes the assertion less dependent
         * on proxy behavior.
         */
        assertEquals(
                1,
                found.getItems().size()
        );

        OrderItem foundItem =
                found.getItems().get(0);

        long foundProductId =
                foundItem.getProduct().getId();

        /*
         * =====================================================
         * DETACH THE LOADED GRAPH
         * =====================================================
         *
         * The following assertions are performed after
         * clearing the EntityManager.
         */
        entityManager.clear();

        /*
         * =====================================================
         * VERIFY ORDER
         * =====================================================
         */
        assertEquals(
                orderId,
                found.getId()
        );

        /*
         * =====================================================
         * VERIFY CUSTOMER
         * =====================================================
         */
        assertEquals(
                customerId,
                found.getCustomer().getId()
        );

        /*
         * =====================================================
         * VERIFY ORDER ITEMS
         * =====================================================
         */
        assertEquals(
                1,
                found.getItems().size()
        );

        foundItem = found.getItems().get(0);

        /*
         * Verify that the OrderItem references
         * the database-generated product ID.
         */
        assertEquals(
                productId,
                foundProductId
        );

        assertEquals(
                productId,
                foundItem.getProduct().getId()
        );

        assertEquals(
                2,
                foundItem.getQuantity()
        );

        /*
         * =====================================================
         * VERIFY TOTAL
         * =====================================================
         *
         * Unit price = 100.00
         * Quantity = 2
         * Total = 200.00
         */
        assertEquals(
                200.00,
                found.calculateTotal(),
                0.001
        );
    }
}
