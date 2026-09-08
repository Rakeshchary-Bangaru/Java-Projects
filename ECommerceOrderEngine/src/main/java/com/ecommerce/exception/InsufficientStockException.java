package com.ecommerce.exception;

/**
 * Thrown when a requested inventory reservation cannot be completed
 * because the available stock is insufficient.
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * Creates an exception with the specified error message.
     *
     * @param message description of the inventory failure
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}