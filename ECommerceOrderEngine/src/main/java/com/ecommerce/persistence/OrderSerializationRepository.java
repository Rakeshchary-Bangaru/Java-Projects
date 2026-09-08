package com.ecommerce.persistence;

import com.ecommerce.model.Order;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Provides persistence for {@link Order} objects using Java object
 * serialization.
 *
 * <p>The repository supports saving and reading both individual orders
 * and lists of orders.</p>
 *
 * <p>This implementation is included to demonstrate Core Java
 * serialization concepts. It is not intended to be the long-term
 * production persistence mechanism. JDBC and MySQL will be introduced
 * in a later version.</p>
 *
 * <p>Serialized data should only be read from trusted sources.</p>
 */
public final class OrderSerializationRepository {

    /**
     * Serializes a single order to the specified file.
     *
     * @param filePath path of the file to write
     * @param order order to serialize
     * @throws IllegalArgumentException if the file path or order is null
     * @throws IOException if the order cannot be written
     */
    public void saveOrder(
            Path filePath,
            Order order) throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(
                             Files.newOutputStream(filePath)
                     )) {

            outputStream.writeObject(order);
        }
    }

    /**
     * Reads and deserializes a single order from the specified file.
     *
     * @param filePath path of the serialized order file
     * @return deserialized order
     * @throws IllegalArgumentException if the file path is null
     * @throws IOException if the file cannot be read
     * @throws ClassNotFoundException if the serialized object's class
     *                                cannot be resolved
     */
    public Order readOrder(Path filePath)
            throws IOException, ClassNotFoundException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }

        try (ObjectInputStream inputStream =
                     new ObjectInputStream(
                             Files.newInputStream(filePath)
                     )) {

            return (Order) inputStream.readObject();
        }
    }

    /**
     * Serializes a list of orders to the specified file.
     *
     * @param filePath path of the file to write
     * @param orders orders to serialize
     * @throws IllegalArgumentException if the file path or orders list is null
     * @throws IOException if the orders cannot be written
     */
    public void saveOrders(
            Path filePath,
            List<Order> orders) throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }

        if (orders == null) {
            throw new IllegalArgumentException(
                    "Orders cannot be null"
            );
        }

        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(
                             Files.newOutputStream(filePath)
                     )) {

            outputStream.writeObject(orders);
        }
    }

    /**
     * Reads and deserializes a list of orders from the specified file.
     *
     * <p>The unchecked cast warning is suppressed because Java
     * serialization restores the list as an {@code Object}, requiring
     * an explicit cast to {@code List<Order>}.</p>
     *
     * @param filePath path of the serialized orders file
     * @return deserialized list of orders
     * @throws IllegalArgumentException if the file path is null
     * @throws IOException if the file cannot be read
     * @throws ClassNotFoundException if a serialized object's class
     *                                cannot be resolved
     */
    @SuppressWarnings("unchecked")
    public List<Order> readOrders(Path filePath)
            throws IOException, ClassNotFoundException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }

        try (ObjectInputStream inputStream =
                     new ObjectInputStream(
                             Files.newInputStream(filePath)
                     )) {

            return (List<Order>) inputStream.readObject();
        }
    }
}