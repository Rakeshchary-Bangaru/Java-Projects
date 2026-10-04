package com.ecommerce.repository;

import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;

import org.junit.jupiter.api.Test;

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

        long productId = 980301L;
        long customerId = 980301L;
        long orderId = 980301L;

        /*
         * Cleanup in case a previous interrupted
         * run left test data behind.
         */
        orderRepository.deleteById(orderId);
        productRepository.deleteById(productId);
        customerRepository.deleteById(customerId);

        Product product =
                new Product(
                        productId,
                        "Hibernate Order Product",
                        "Testing",
                        59.99
                );

        Customer customer =
                new Customer(
                        customerId,
                        "Hibernate Order Customer",
                        "hibernate.order@example.com"
                );

        try {

            productRepository.save(product);
            customerRepository.save(customer);

            OrderItem item =
                    new OrderItem(
                            product,
                            2
                    );

            OrderStatus initialStatus =
                    OrderStatus.values()[0];

            Order order =
                    Order.restore(
                            orderId,
                            customer,
                            List.of(item),
                            PaymentType.CARD,
                            initialStatus,
                            LocalDateTime.now()
                    );

            // SAVE
            orderRepository.save(order);

            // FIND
            Order found =
                    orderRepository
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

            assertEquals(
                    productId,
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


            // FIND ALL
            assertTrue(
                    orderRepository
                            .findAll()
                            .stream()
                            .anyMatch(
                                    existingOrder ->
                                            existingOrder.getId()
                                                    == orderId
                            )
            );


            // UPDATE STATUS
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

            assertEquals(
                    updatedStatus,
                    updated.getStatus()
            );


            // DELETE
            boolean deleted =
                    orderRepository.deleteById(
                            orderId
                    );

            assertTrue(deleted);

            assertTrue(
                    orderRepository
                            .findById(orderId)
                            .isEmpty()
            );

        } finally {

            orderRepository.deleteById(
                    orderId
            );

            productRepository.deleteById(
                    productId
            );

            customerRepository.deleteById(
                    customerId
            );
        }
    }
}