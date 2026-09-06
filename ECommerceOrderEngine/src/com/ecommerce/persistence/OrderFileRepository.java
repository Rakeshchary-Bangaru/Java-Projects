package com.ecommerce.persistence;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.PaymentType;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides simple file-based persistence for order summaries.
 *
 * <p>Orders are stored in a CSV-style text format containing the order ID,
 * status, payment type, and total value.</p>
 *
 * <p>This repository is intended to demonstrate Core Java file I/O.
 * Database persistence using JDBC and MySQL is planned for a later version.</p>
 */
public final class OrderFileRepository {

    /**
     * Writes the supplied orders to a file.
     *
     * <p>Existing file content is replaced. A header row is written before
     * the order data.</p>
     *
     * @param filePath path of the file to write
     * @param orders orders to persist
     * @throws IllegalArgumentException if the file path or orders list is null
     * @throws IOException if the file cannot be written
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

        try (PrintWriter writer =
                     new PrintWriter(Files.newBufferedWriter(filePath))) {

            // Write the CSV header before storing order records.
            writer.println(
                    "orderId,status,paymentType,total"
            );

            for (Order order : orders) {
                writer.println(
                        order.getId()
                                + ","
                                + order.getStatus()
                                + ","
                                + order.getPaymentType()
                                + ","
                                + order.calculateTotal()
                );
            }
        }
    }

    /**
     * Reads order summaries from a previously written CSV-style file.
     *
     * <p>The first line is treated as the header and skipped.
     * Each remaining line is converted into an {@link OrderSummary}.</p>
     *
     * @param filePath path of the file to read
     * @return order summaries read from the file
     * @throws IllegalArgumentException if the file path is null
     * @throws IOException if the file cannot be read
     */
    public List<OrderSummary> readOrders(
            Path filePath) throws IOException {

        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }

        List<String> lines =
                Files.readAllLines(filePath);

        List<OrderSummary> orders =
                new ArrayList<>();

        // Index 0 contains the CSV header, so data starts from index 1.
        for (int i = 1; i < lines.size(); i++) {

            String[] parts =
                    lines.get(i).split(",");

            long orderId =
                    Long.parseLong(parts[0]);

            OrderStatus status =
                    OrderStatus.valueOf(parts[1]);

            PaymentType paymentType =
                    PaymentType.valueOf(parts[2]);

            double total =
                    Double.parseDouble(parts[3]);

            orders.add(
                    new OrderSummary(
                            orderId,
                            status,
                            paymentType,
                            total
                    )
            );
        }

        return orders;
    }

    /**
     * Appends a single order to the persistence file.
     *
     * <p>If the file does not yet exist, it is created and the CSV header
     * is written before the first order record.</p>
     *
     * @param filePath path of the file
     * @param order order to append
     * @throws IllegalArgumentException if the file path or order is null
     * @throws IOException if the file cannot be written
     */
    public void appendOrder(
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

        boolean fileExists =
                Files.exists(filePath);

        try (PrintWriter writer =
                     new PrintWriter(
                             Files.newBufferedWriter(
                                     filePath,
                                     StandardOpenOption.CREATE,
                                     StandardOpenOption.APPEND
                             )
                     )) {

            // A newly created file needs the header before its first record.
            if (!fileExists) {
                writer.println(
                        "orderId,status,paymentType,total"
                );
            }

            writer.println(
                    order.getId()
                            + ","
                            + order.getStatus()
                            + ","
                            + order.getPaymentType()
                            + ","
                            + order.calculateTotal()
            );
        }
    }
}