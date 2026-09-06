# High-Throughput E-Commerce Order Processing & Analytics Engine

## Overview

ECommerceOrderEngine is a Core Java application that models the main components of an e-commerce order-processing system.

The project demonstrates object-oriented programming, SOLID principles, design patterns, concurrency, exception handling, analytics, file persistence, serialization, and automated testing.

The system supports product and cart management, inventory reservation, multiple payment strategies, order creation, notifications, analytics, persistence, rollback handling, and concurrent checkout processing.

## Features

- Product and customer management
- Shopping cart operations
- Inventory tracking and reservation
- Thread-safe inventory updates
- Concurrent checkout processing
- Card, UPI, and Wallet payments
- Checkout rollback on failures
- Order creation using Builder
- Payment Strategy pattern
- Payment Factory
- Observer-based notifications
- Analytics processing
- CSV persistence
- Java serialization
- Custom exceptions
- Unit, concurrency, and integration testing

## Technologies and Concepts

- Java
- Object-Oriented Programming
- Java Collections Framework
- Streams API
- Lambda Expressions
- Optional
- Exception Handling
- File I/O
- Java Serialization
- ConcurrentHashMap
- ExecutorService
- CountDownLatch
- AtomicInteger
- JUnit 5
- SOLID Principles
- Design Patterns

## Project Structure

```text
src/com/ecommerce/
├── analytics/
│   ├── AnalyticsObserver.java
│   └── AnalyticsService.java
│
├── checkout/
│   └── CheckoutService.java
│
├── exception/
│   ├── InsufficientStockException.java
│   └── PaymentFailedException.java
│
├── inventory/
│   └── InventoryService.java
│
├── model/
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Customer.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   ├── PaymentType.java
│   └── Product.java
│
├── notification/
│   ├── EmailNotificationObserver.java
│   ├── OrderEventPublisher.java
│   └── OrderObserver.java
│
├── payment/
│   ├── CardPaymentStrategy.java
│   ├── PaymentFactory.java
│   ├── PaymentProcessor.java
│   ├── PaymentResult.java
│   ├── PaymentStrategy.java
│   ├── UpiPaymentStrategy.java
│   └── WalletPaymentStrategy.java
│
└── persistence/
    ├── OrderFileRepository.java
    ├── OrderSerializationRepository.java
    └── OrderSummary.java