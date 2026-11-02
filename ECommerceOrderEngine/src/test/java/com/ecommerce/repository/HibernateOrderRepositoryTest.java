
package com.ecommerce.repository;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HibernateOrderRepositoryTest {

    @Test
    void shouldSaveFindUpdateAndDeleteOrder()
            throws Exception {

        ProductRepository productRepository =
                new HibernateProductRepository();

        CustomerRepository customerRepository =
                new HibernateCustomerRepository();

        OrderRepository orderRepository =
                new HibernateOrderRepository();

        /*
         * Only the product ID is generated automatically.
         * Customer and order IDs retain their current
         * manually assigned values.
         */
        long customerId = 980301L;
        long orderId = 980301L;

        Long productId = null;

        /*
         * Cleanup leftover order/customer records from
         * an interrupted previous test run.
         */
        orderRepository.deleteById(orderId);
        customerRepository.deleteById(customerId);

        /*
         * Create product WITHOUT a manually assigned ID.
         */
        Product product = new Product(
                "Hibernate Order Product",
                "Testing",
                new BigDecimal("59.99"),
                "Product used for Hibernate order repository testing",
                "TestBrand"
        );

        Customer customer = new Customer(
                customerId,
                "Hibernate Order Customer",
                "hibernate.order@example.com"
        );

        try {

            /*
             * =================================================
             * SAVE PRODUCT AND CUSTOMER
             * =================================================
             */
            productRepository.save(product);

            productId = product.getId();

            assertNotNull(productId);
            assertTrue(productId > 0);

            customerRepository.save(customer);

            /*
             * =================================================
             * CREATE ORDER ITEM
             * =================================================
             */
            OrderItem item = new OrderItem(
                    product,
                    2
            );

            OrderStatus initialStatus =
                    OrderStatus.values()[0];

            /*
             * =================================================
             * CREATE ORDER
             * =================================================
             */
            Order order = Order.restore(
                    orderId,
                    customer,
                    List.of(item),
                    PaymentType.CARD,
                    initialStatus,
                    LocalDateTime.now()
            );

            /*
             * =================================================
             * SAVE ORDER
             * =================================================
             */
            orderRepository.save(order);

            /*
             * =================================================
             * FIND ORDER
             * =================================================
             */
            Order found = orderRepository
                    .findById(orderId)
                    .orElseThrow();

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

            /*
             * Verify the order item references
             * the database-generated product ID.
             */
            assertEquals(
                    productId.longValue(),
                    found.getItems()
                            .get(0)
                            .getProduct()
                            .getId()
            );

            assertEquals(
                    59.99,
                    found.getItems()
                            .get(0)
                            .getUnitPrice(),
                    0.001
            );

            assertEquals(
                    119.98,
                    found.calculateTotal(),
                    0.001
            );

            /*
             * =================================================
             * FIND ALL ORDERS
             * =================================================
             */
            assertTrue(
                    orderRepository
                            .findAll()
                            .stream()
                            .anyMatch(existingOrder ->
                                    existingOrder.getId() == orderId
                            )
            );

            /*
             * =================================================
             * UPDATE ORDER STATUS
             * =================================================
             */
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

            Order updated = orderRepository
                    .findById(orderId)
                    .orElseThrow();

            assertEquals(
                    updatedStatus,
                    updated.getStatus()
            );

            /*
             * =================================================
             * DELETE ORDER
             * =================================================
             */
            boolean deleted =
                    orderRepository.deleteById(orderId);

            assertTrue(deleted);

            assertTrue(
                    orderRepository
                            .findById(orderId)
                            .isEmpty()
            );

        } finally {

            /*
             * Cleanup order first because order_items
             * references the product.
             */
            orderRepository.deleteById(orderId);

            /*
             * Delete only this test's generated product.
             */
            if (productId != null) {
                productRepository.deleteById(productId);
            }

            customerRepository.deleteById(customerId);
        }
    }
}
