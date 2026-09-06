# Architecture

## Overview

The E-Commerce Order Processing & Analytics Engine is organized into independent modules for domain models, inventory management, payment processing, checkout orchestration, notifications, analytics, exception handling, and persistence.

The central component of the application is `CheckoutService`.

`CheckoutService` coordinates the checkout workflow while delegating specialized responsibilities to other components such as `InventoryService`, `PaymentProcessor`, and `OrderEventPublisher`.

This separation keeps the application modular, testable, and easier to extend.

---

## High-Level Architecture

```text
Customer
   ↓
Cart
   ↓
CheckoutService
   |
   ├── InventoryService
   |
   ├── PaymentProcessor
   |        ↓
   |   PaymentStrategy
   |    /     |      \
   |  Card   UPI    Wallet
   |
   └── Order.Builder
            ↓
          Order
            ↓
     OrderEventPublisher
        /          \
       ↓            ↓
EmailNotification  AnalyticsObserver
Observer                 ↓
                   AnalyticsService
```

---

## Main Modules

### Model Layer

The model package contains the core domain objects of the application.

```text
model/
├── Product
├── Customer
├── Cart
├── CartItem
├── Order
├── OrderItem
├── OrderStatus
└── PaymentType
```

These classes represent the business data used throughout the system.

For example:

```text
Product
   ↓
CartItem
   ↓
Cart
   ↓
Checkout
   ↓
OrderItem
   ↓
Order
```

`CartItem` represents a product currently inside a customer's cart.

`OrderItem` captures the product price at the time the order is created so historical order totals are not affected by future product price changes.

---

## Inventory Architecture

Inventory management is handled by:

```text
InventoryService
```

The service stores product stock using:

```text
ConcurrentHashMap<Long, Integer>
```

The product ID is used as the key and the available quantity is stored as the value.

Conceptually:

```text
Product ID      Stock
---------------------
101             20
102             15
103             50
```

Inventory reservation uses an atomic `compute()` operation.

```text
Check stock
     +
Update stock
     ↓
Atomic operation
```

This prevents multiple checkout threads from reading the same stock value and overselling inventory.

---

## Checkout Architecture

`CheckoutService` acts as the main coordinator for the order-processing workflow.

It depends on:

```text
InventoryService
PaymentProcessor
OrderEventPublisher
```

These dependencies are supplied through constructor dependency injection.

Conceptually:

```text
CheckoutService
      |
      ├── InventoryService
      ├── PaymentProcessor
      └── OrderEventPublisher
```

This avoids creating dependencies directly inside `CheckoutService` and makes the class easier to test.

---

## Checkout Flow

The normal checkout flow is:

```text
Customer
   ↓
Cart
   ↓
Validate request
   ↓
Validate cart
   ↓
Reserve inventory
   ↓
Create OrderItems
   ↓
Calculate total
   ↓
Process payment
   ↓
Create Order
   ↓
Update status to PAID
   ↓
Publish order event
   ↓
Return Order
```

The main orchestration can be viewed as:

```text
CheckoutService
      ↓
Inventory Reservation
      ↓
Payment Processing
      ↓
Order Creation
      ↓
Event Publishing
```

---

## Inventory Rollback

A checkout may contain multiple products.

For example:

```text
Cart

Product A → quantity 2
Product B → quantity 3
Product C → quantity 1
```

Suppose:

```text
Product A reserved 
Product B reserved 
Product C unavailable 
```

The system releases only the inventory that was successfully reserved:

```text
Product A → release
Product B → release
Product C → nothing to release
```

This prevents partial inventory reservations from remaining after checkout failure.

---

## Payment Failure Rollback

Another failure can occur after all inventory has already been reserved.

```text
Reserve inventory 
        ↓
Process payment 
        ↓
Release inventory
        ↓
Throw PaymentFailedException
```

This creates transaction-like behavior even though Core Java V1 does not yet use database transactions.

Later, JDBC will provide similar behavior through:

```text
BEGIN
   ↓
Database operations
   ↓
COMMIT
```

or:

```text
Failure
   ↓
ROLLBACK
```

---

## Payment Architecture

Payment processing is separated from checkout logic.

```text
CheckoutService
      ↓
PaymentProcessor
      ↓
PaymentStrategy
```

`PaymentStrategy` defines the common payment contract.

Implementations are:

```text
PaymentStrategy
      |
      ├── CardPaymentStrategy
      ├── UpiPaymentStrategy
      └── WalletPaymentStrategy
```

`PaymentProcessor` does not need to know the internal implementation of each payment type.

It simply executes:

```text
PaymentStrategy.pay()
```

This allows payment implementations to be replaced or extended without redesigning checkout processing.

---

## Payment Creation

The creation of payment strategies is centralized inside:

```text
PaymentFactory
```

Flow:

```text
PaymentType.CARD
       ↓
PaymentFactory
       ↓
CardPaymentStrategy
```

Similarly:

```text
PaymentType.UPI
       ↓
PaymentFactory
       ↓
UpiPaymentStrategy
```

This separates object creation from payment execution.

---

## Order Construction

Orders are constructed using:

```text
Order.Builder
```

Conceptually:

```text
Order.Builder
      ↓
id
      ↓
customer
      ↓
items
      ↓
paymentType
      ↓
build()
      ↓
Order
```

The Builder keeps order construction readable while allowing the `Order` class to maintain its validation rules.

---

## Event Architecture

Once an order is successfully paid, `CheckoutService` publishes an event.

```text
Order becomes PAID
       ↓
OrderEventPublisher
       ↓
OrderObserver
```

Current observers are:

```text
OrderObserver
     |
     ├── EmailNotificationObserver
     └── AnalyticsObserver
```

The publisher communicates only through the `OrderObserver` interface.

Therefore `CheckoutService` does not need direct dependencies on:

```text
EmailNotificationObserver
AnalyticsObserver
```

This reduces coupling between modules.

---

## Observer Flow

```text
CheckoutService
      ↓
OrderEventPublisher
      ↓
notifyObservers(order)
      ↓
 ┌───────────────┬─────────────────┐
 ↓               ↓
EmailNotificationObserver    AnalyticsObserver
                                  ↓
                              Store Order
```

This architecture can later support additional observers such as:

```text
AuditLogObserver
KafkaEventObserver
SMSNotificationObserver
LoyaltyPointsObserver
```

without changing the core checkout workflow significantly.

---

## Analytics Architecture

`AnalyticsObserver` receives completed order events.

```text
Paid Order
    ↓
OrderEventPublisher
    ↓
AnalyticsObserver
    ↓
Stored completed orders
    ↓
AnalyticsService
```

`AnalyticsService` performs calculations such as:

- Total revenue
- Average order value
- Number of paid orders
- Orders grouped by payment type
- Best-selling product
- Revenue by category
- Top paid orders

The analytics calculations use Java Streams rather than being embedded inside checkout processing.

---

## Persistence Architecture

Core Java V1 demonstrates two persistence approaches.

### File-Based Persistence

```text
Order
   ↓
OrderFileRepository
   ↓
CSV-style file
```

`OrderFileRepository` supports saving, appending, and reading order summaries.

---

### Java Serialization

```text
Order
   ↓
OrderSerializationRepository
   ↓
ObjectOutputStream
   ↓
Serialized file
```

The repository can later reconstruct the stored `Order` using:

```text
ObjectInputStream
```

Java serialization is included for learning purposes and is not intended to become the long-term production persistence mechanism.

---

## Exception Architecture

The application contains domain-specific runtime exceptions.

```text
InsufficientStockException
PaymentFailedException
```

Example failure flow:

```text
InventoryService
      ↓
Insufficient stock
      ↓
InsufficientStockException
```

Payment failure:

```text
PaymentProcessor
      ↓
Payment unsuccessful
      ↓
CheckoutService
      ↓
PaymentFailedException
```

Using domain-specific exceptions makes failures easier to understand and test.

---

## Concurrency Architecture

The application includes dedicated concurrency tests for both inventory and checkout behavior.

A representative checkout scenario is:

```text
Available Stock = 50

100 concurrent checkout attempts
            ↓
      Multiple Threads
            ↓
      InventoryService
            ↓
ConcurrentHashMap.compute()
```

Expected result:

```text
50 successful checkouts
50 rejected checkouts
0 inventory remaining
No overselling
```

This verifies that stock reservation remains correct even when many threads attempt to purchase the same product simultaneously.

---

## End-to-End Architecture

The complete Core Java V1 flow can be represented as:

```text
Product
   ↓
Inventory
   ↓
Cart
   ↓
CheckoutService
   ↓
PaymentFactory
   ↓
PaymentStrategy
   ↓
PaymentProcessor
   ↓
Order.Builder
   ↓
Order
   ↓
PAID
   ↓
OrderEventPublisher
   ├───────────────┐
   ↓               ↓
Email          Analytics
                   ↓
            AnalyticsService
                   ↓
              Persistence
```

---

## Architectural Principles

The project architecture focuses on:

- Separation of concerns
- Single responsibility
- Loose coupling
- Constructor dependency injection
- Programming to abstractions
- Encapsulation
- Immutability where appropriate
- Defensive copying
- Thread safety
- Explicit failure handling
- Testability
- Extensibility

---

## Future Architecture

Core Java V1 currently uses in-memory objects and file-based persistence.

The planned evolution is:

```text
Current

CheckoutService
      ↓
Java Objects
      ↓
File Persistence
```

Next:

```text
CheckoutService
      ↓
Repository Layer
      ↓
JDBC
      ↓
MySQL
```

Then:

```text
Browser
   ↓
Servlet
   ↓
Service
   ↓
Repository
   ↓
JDBC
   ↓
MySQL
   ↓
JSP
```

Eventually:

```text
Frontend
   ↓
REST API
   ↓
Spring Boot Controller
   ↓
Service
   ↓
Spring Data JPA
   ↓
Hibernate
   ↓
MySQL
```

The same project will therefore evolve from a Core Java application into a database-backed web application and later into a Spring Boot backend.