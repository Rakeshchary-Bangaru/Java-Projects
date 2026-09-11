# High-Throughput E-Commerce Order Processing & Analytics Engine

## Overview

`ECommerceOrderEngine` is a Java-based e-commerce backend project designed to demonstrate the progressive development of an order-processing system from Core Java to database-backed application architecture.

The project currently contains two major versions:

- **V1 – Core Java:** In-memory inventory, payment strategies, concurrent checkout processing, notifications, analytics, file persistence, and serialization.
- **V2 – Maven + JDBC + MySQL:** Relational database persistence, repository pattern, JDBC transactions, atomic inventory reservation, rollback handling, and database-level concurrency control.

The project focuses on object-oriented design, SOLID principles, design patterns, concurrency, database programming, transaction management, exception handling, and automated testing.

---

## V1 – Core Java

V1 implements the order-processing system using Core Java and in-memory data structures.

### Features

- Product and customer management
- Shopping cart operations
- In-memory inventory management
- Thread-safe inventory reservation
- Concurrent checkout processing
- Card, UPI, and Wallet payment strategies
- Checkout rollback handling
- Order creation using Builder pattern
- Payment Strategy pattern
- Payment Factory
- Observer-based order notifications
- Analytics processing
- CSV persistence
- Java serialization
- Custom exceptions
- Unit and concurrency testing

### Core Java Concepts

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

---

# V2 – Maven + JDBC + MySQL

V2 extends the Core Java application by introducing persistent relational storage and transaction-based checkout processing.

Instead of storing application state only in memory, products, customers, inventory, orders, order items, and payments are now persisted in MySQL.

## V2 Features

- Maven-based project structure
- MySQL database integration
- JDBC connectivity
- Repository pattern
- Product CRUD operations
- Customer CRUD operations
- Persistent inventory management
- Atomic stock reservation
- Persistent orders and order items
- Payment persistence
- SQL JOIN queries
- Batch insertion of order items
- JDBC transaction management
- Commit and rollback handling
- Foreign key constraints
- Database validation constraints
- Concurrent database checkout processing
- Prevention of inventory overselling
- Environment-variable based database credentials
- Database integration testing
- 206 automated tests

---

## V2 Architecture

```text
                    Application
                         |
                         v
                JdbcCheckoutService
                         |
          +--------------+---------------+
          |              |               |
          v              v               v
 InventoryRepository  OrderRepository  PaymentRepository
          |              |               |
          v              v               v
 JdbcInventoryRepo   JdbcOrderRepo   JdbcPaymentRepo
          |              |               |
          +--------------+---------------+
                         |
                         v
                        JDBC
                         |
                         v
                       MySQL
```

The checkout service contains the business workflow, while repositories handle SQL and persistence operations.

```text
Business Logic
    |
    v
JdbcCheckoutService

Persistence Logic
    |
    v
JDBC Repositories

Database
    |
    v
MySQL
```

---

## Transactional Checkout

V2 performs checkout using a single JDBC connection and database transaction.

```text
BEGIN TRANSACTION
       |
       v
Reserve Inventory
       |
       v
Calculate Order Total
       |
       v
Process Payment
       |
       v
Create Order
       |
       v
Save Order
       |
       v
Save Order Items
       |
       v
Save Payment
       |
       v
COMMIT
```

If any step fails:

```text
Failure
   |
   v
ROLLBACK
```

This prevents partial updates such as inventory being reduced without an order being created.

---

## ACID Transaction Concepts

The checkout implementation demonstrates important ACID transaction properties.

```text
Atomicity
→ Checkout operations succeed together or are rolled back together.

Consistency
→ Foreign keys, CHECK constraints, and validation keep the database valid.

Isolation
→ Concurrent checkout transactions do not oversell inventory.

Durability
→ Successfully committed orders and payments remain persisted in MySQL.
```

---

## Concurrency and Atomic Inventory Reservation

Inventory reservation is performed using an atomic SQL statement:

```sql
UPDATE inventory
SET quantity = quantity - ?
WHERE product_id = ?
  AND quantity >= ?;
```

This prevents multiple concurrent checkout requests from purchasing more stock than is available.

A concurrency integration test executes:

```text
Initial inventory: 50

100 concurrent checkout attempts
20 worker threads

Result:

50 successful checkouts
50 rejected due to insufficient stock
0 unexpected errors

Final inventory: 0
Orders committed: 50
Payments committed: 50
```

---

## Repository Pattern

V2 separates business logic from database persistence using repository interfaces and JDBC implementations.

```text
ProductRepository
    |
    └── JdbcProductRepository

CustomerRepository
    |
    └── JdbcCustomerRepository

InventoryRepository
    |
    └── JdbcInventoryRepository

OrderRepository
    |
    └── JdbcOrderRepository

PaymentRepository
    |
    └── JdbcPaymentRepository
```

Repositories are responsible for operations such as:

```text
INSERT
SELECT
UPDATE
DELETE
ResultSet mapping
PreparedStatement handling
```

The checkout service focuses on the business workflow instead of containing SQL directly.

---

## Design Patterns

The project applies several design patterns.

```text
Repository Pattern
→ Separates persistence logic from application/business logic.

Strategy Pattern
→ Card, UPI, and Wallet payment implementations.

Factory Pattern
→ PaymentFactory selects the appropriate payment strategy.

Builder Pattern
→ Used to construct Order objects.

Observer Pattern
→ Used in V1 for notifications and analytics.

Dependency Injection
→ Repositories and payment processor factories are supplied to services.

Functional Factory Injection
→ Function<PaymentType, PaymentProcessor> improves payment testability.
```

---

## SOLID Principles

The project applies SOLID principles where appropriate.

```text
Single Responsibility Principle
→ Repositories, checkout services, payment processors, and database
  connection utilities have separate responsibilities.

Open/Closed Principle
→ New payment strategies can be added without rewriting PaymentProcessor.

Liskov Substitution Principle
→ Repository implementations follow repository contracts.

Interface Segregation Principle
→ Product, Customer, Inventory, Order, and Payment persistence use
  separate focused interfaces.

Dependency Inversion Principle
→ Partially applied through abstractions and dependency injection.
  JdbcCheckoutService currently uses concrete JDBC repositories because
  shared Connection objects are required for manual JDBC transaction control.
```

A future Spring version can improve this further using framework-managed transactions such as `@Transactional`.

---

## Database Schema

The database schema is stored in:

```text
src/main/resources/schema.sql
```

The schema contains the following tables:

```text
products
customers
inventory
orders
order_items
payments
```

Main relationships:

```text
customers
    |
    v
orders
    |
    +-----------> order_items
    |
    +-----------> payments

products
    |
    +-----------> inventory
    |
    +-----------> order_items
```

`order_items` and `payments` use `ON DELETE CASCADE` when their associated order is deleted.

---

## Project Structure

```text
ECommerceOrderEngine/
│
├── pom.xml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecommerce/
│   │   │       │
│   │   │       ├── analytics/
│   │   │       │
│   │   │       ├── checkout/
│   │   │       │   ├── CheckoutService.java
│   │   │       │   └── JdbcCheckoutService.java
│   │   │       │
│   │   │       ├── database/
│   │   │       │   └── DatabaseConnection.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── InsufficientStockException.java
│   │   │       │   └── PaymentFailedException.java
│   │   │       │
│   │   │       ├── inventory/
│   │   │       │
│   │   │       ├── model/
│   │   │       │   ├── Cart.java
│   │   │       │   ├── CartItem.java
│   │   │       │   ├── Customer.java
│   │   │       │   ├── Order.java
│   │   │       │   ├── OrderItem.java
│   │   │       │   ├── OrderStatus.java
│   │   │       │   ├── PaymentType.java
│   │   │       │   └── Product.java
│   │   │       │
│   │   │       ├── notification/
│   │   │       │
│   │   │       ├── payment/
│   │   │       │   ├── CardPaymentStrategy.java
│   │   │       │   ├── PaymentFactory.java
│   │   │       │   ├── PaymentProcessor.java
│   │   │       │   ├── PaymentResult.java
│   │   │       │   ├── PaymentStrategy.java
│   │   │       │   ├── UpiPaymentStrategy.java
│   │   │       │   └── WalletPaymentStrategy.java
│   │   │       │
│   │   │       ├── persistence/
│   │   │       │
│   │   │       └── repository/
│   │   │           ├── ProductRepository.java
│   │   │           ├── JdbcProductRepository.java
│   │   │           ├── CustomerRepository.java
│   │   │           ├── JdbcCustomerRepository.java
│   │   │           ├── InventoryRepository.java
│   │   │           ├── JdbcInventoryRepository.java
│   │   │           ├── OrderRepository.java
│   │   │           ├── JdbcOrderRepository.java
│   │   │           ├── PaymentRepository.java
│   │   │           └── JdbcPaymentRepository.java
│   │   │
│   │   └── resources/
│   │       └── schema.sql
│   │
│   └── test/
│       └── java/
│           └── com/ecommerce/
│
└── README.md
```

---

## Database Configuration

The application uses environment variables for database configuration.

The database password must not be committed to source control.

Example:

```bash
read -s "ECOMMERCE_DB_PASSWORD?MySQL password: "
export ECOMMERCE_DB_PASSWORD
```

The application also supports:

```text
ECOMMERCE_DB_URL
ECOMMERCE_DB_USER
ECOMMERCE_DB_PASSWORD
```

Default database configuration:

```text
Database: ecommerce_db
Host: localhost
Port: 3306
```

---

## Running Tests

Run the complete test suite with:

```bash
mvn test
```

Current V2 test status:

```text
Tests: 206
Failures: 0
Errors: 0
```

The test suite includes:

```text
Unit tests
Repository integration tests
Database transaction tests
Rollback tests
Inventory concurrency tests
Concurrent checkout tests
Payment failure tests
Insufficient stock tests
```

---

## Key V2 Concepts Demonstrated

```text
Maven
JDBC
MySQL
PreparedStatement
ResultSet
Optional
Repository Pattern
CRUD
SQL JOINs
Foreign Keys
Database Constraints
Batch Operations
Transactions
Commit / Rollback
ACID Properties
Atomic SQL Updates
Concurrency Control
ExecutorService
CountDownLatch
AtomicInteger
Integration Testing
Dependency Injection
SOLID Principles
Design Patterns
```

---

## Future Improvements

Future versions of the project will progressively introduce:

```text
Servlets and JSP
Tomcat
Hibernate
JPA
Spring Core
Spring Boot
REST APIs
Spring Data JPA
Spring Security
Redis
Kafka
Docker
Microservices
Cloud Deployment
CI/CD
```

Other technical improvements may include:

```text
BigDecimal for monetary values
Connection pooling
Optimized order fetching
Improved transaction abstraction
External payment idempotency
Outbox / Saga patterns
Testcontainers
```

---

## Version Roadmap

```text
V1  Core Java                     ✅
V2  Maven + JDBC + MySQL          ✅
V3  Servlets + JSP + Tomcat       ⏭️
V4  Hibernate + JPA
V5  Spring Core + Spring Boot
V6  REST APIs + Spring Data JPA
V7  Spring Security
V8  Advanced Integration Testing
V9  Docker
V10 Redis + Kafka + Microservices
V11 Cloud + CI/CD
```