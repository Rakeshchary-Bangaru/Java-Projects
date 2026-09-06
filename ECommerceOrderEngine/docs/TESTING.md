# Testing

## Overview

The E-Commerce Order Processing & Analytics Engine uses automated testing to verify business logic, failure handling, concurrency behavior, persistence, and end-to-end order processing.

The Core Java V1 project uses:

```text
JUnit 5
```

The current test suite contains **23 test classes** covering the major application modules.

Current baseline:

```text
166 tests passed
0 tests failed
```

The goal of the test suite is not only to verify successful scenarios, but also to test invalid input, exceptions, rollback behavior, concurrency, and integration between components.

---

# Test Structure

```text
test/com/ecommerce/
├── analytics/
│   ├── AnalyticsObserverTest.java
│   └── AnalyticsServiceTest.java
│
├── checkout/
│   ├── CheckoutConcurrencyTest.java
│   └── CheckoutServiceTest.java
│
├── integration/
│   └── ECommerceOrderEngineIntegrationTest.java
│
├── inventory/
│   ├── InventoryConcurrencyTest.java
│   └── InventoryServiceTest.java
│
├── model/
│   ├── CartItemTest.java
│   ├── CartTest.java
│   ├── CustomerTest.java
│   ├── OrderItemTest.java
│   ├── OrderTest.java
│   └── ProductTest.java
│
├── notification/
│   ├── EmailNotificationObserverTest.java
│   └── OrderEventPublisherTest.java
│
├── payment/
│   ├── CardPaymentStrategyTest.java
│   ├── PaymentFactoryTest.java
│   ├── PaymentProcessorTest.java
│   ├── PaymentResultTest.java
│   ├── UpiPaymentStrategyTest.java
│   └── WalletPaymentStrategyTest.java
│
└── persistence/
    ├── OrderFileRepositoryTest.java
    └── OrderSerializationRepositoryTest.java
```

---

# Model Tests

The model layer tests verify domain-object construction, validation, state changes, and calculations.

## ProductTest

Tests `Product` behavior such as:

```text
Valid product creation
Invalid product ID
Invalid product name
Invalid category
Invalid price
Price updates
```

The tests help ensure invalid product objects cannot be created.

---

## CustomerTest

Tests customer creation and validation.

Examples include:

```text
Valid customer creation
Invalid customer ID
Invalid name
Invalid email
Email updates
```

---

## CartItemTest

Tests the relationship between a product and its quantity inside the shopping cart.

Important behavior includes:

```text
Quantity validation
Quantity updates
Subtotal calculation
```

The subtotal is calculated using:

```text
Product price × quantity
```

---

## CartTest

Tests shopping-cart operations including:

```text
Adding products
Merging quantities
Removing products
Updating quantities
Checking whether the cart is empty
Calculating cart total
Reading cart items
```

These tests ensure cart operations maintain a valid state.

---

## OrderItemTest

`OrderItem` represents an item that has already been purchased.

Tests verify:

```text
Product validation
Quantity validation
Captured unit price
Subtotal calculation
```

An important behavior is that `OrderItem` captures the product price when the order item is created.

This means later changes to `Product.price` do not change the historical order amount.

---

## OrderTest

Tests order creation and behavior such as:

```text
Order validation
Initial status
Order total
Status updates
Builder construction
Defensive copying of order items
```

The tests verify that `Order.Builder` creates valid orders while preserving the validation rules of the `Order` class.

---

# Inventory Tests

## InventoryServiceTest

Tests normal inventory operations such as:

```text
Adding stock
Reading stock
Checking availability
Reserving stock
Releasing stock
Rejecting invalid quantities
Rejecting insufficient stock
```

When stock is insufficient, the service throws:

```text
InsufficientStockException
```

---

# Inventory Concurrency Testing

## InventoryConcurrencyTest

Inventory operations are also tested under concurrent access.

The purpose is to verify that stock cannot be oversold when many threads attempt to reserve the same product at the same time.

The implementation uses:

```text
ConcurrentHashMap
compute()
```

The atomic `compute()` operation keeps the following sequence together:

```text
Read current stock
        +
Check available stock
        +
Update stock
```

This prevents the classic lost-update problem.

A non-atomic implementation such as:

```text
get()
check()
put()
```

could allow multiple threads to read the same stock value before either thread updates it.

The concurrency tests demonstrate why atomic compound operations are required.

---

# Payment Tests

## CardPaymentStrategyTest

Tests Card payment behavior and amount validation.

---

## UpiPaymentStrategyTest

Tests UPI payment behavior and amount validation.

---

## WalletPaymentStrategyTest

Tests Wallet payment behavior and amount validation.

---

## PaymentResultTest

Tests the payment result object.

Important validation includes:

```text
Payment type
Amount
Message
Successful / unsuccessful result
```

---

## PaymentProcessorTest

Tests the Strategy Pattern integration.

`PaymentProcessor` receives a `PaymentStrategy` through constructor dependency injection.

Tests verify that:

```text
PaymentProcessor
      ↓
PaymentStrategy
      ↓
pay()
```

is executed correctly.

A custom test strategy can also be injected to simulate payment failure without modifying production payment implementations.

---

## PaymentFactoryTest

Tests the Simple Factory used for payment strategy creation.

The factory is tested for:

```text
CARD
UPI
WALLET
null PaymentType
```

Expected mappings include:

```text
CARD
→ CardPaymentStrategy

UPI
→ UpiPaymentStrategy

WALLET
→ WalletPaymentStrategy
```

---

# Checkout Tests

## CheckoutServiceTest

`CheckoutServiceTest` verifies the main order-processing workflow.

Important successful flow:

```text
Valid cart
    ↓
Reserve inventory
    ↓
Process payment
    ↓
Create order
    ↓
Set order PAID
    ↓
Publish event
```

Tests also cover validation and failure scenarios.

Examples include:

```text
Invalid order ID
Null customer
Null cart
Empty cart
Insufficient inventory
Payment failure
Successful checkout
Correct inventory reduction
Correct order total
Correct order status
Observer notification
```

---

# Rollback Testing

Rollback behavior is an important part of the checkout test suite.

## Partial Inventory Reservation Failure

Example:

```text
Cart contains:
Product A
Product B
Product C
```

Suppose:

```text
A reserved 
B reserved 
C fails 
```

Expected behavior:

```text
Release A
Release B
Do not release C
Throw exception
```

The tests verify that partially reserved stock does not remain reserved after checkout fails.

---

## Payment Failure Rollback

Another scenario occurs when all stock has already been reserved but payment fails.

```text
Reserve all inventory 
        ↓
Process payment 
        ↓
Release reserved inventory
        ↓
Throw PaymentFailedException
```

Tests verify that inventory returns to its previous quantity.

This simulates transaction-style rollback behavior before database transactions are introduced.

---

# Concurrent Checkout Testing

## CheckoutConcurrencyTest

This test verifies the complete checkout workflow under concurrent demand.

A representative scenario is:

```text
Available stock = 50

Concurrent customers = 100
```

Multiple threads attempt checkout at approximately the same time.

The test uses concurrency utilities such as:

```text
ExecutorService
CountDownLatch
AtomicInteger
```

`CountDownLatch` allows worker threads to begin their checkout attempts together, increasing contention on the inventory.

`AtomicInteger` safely records successful and failed checkout counts.

Expected result:

```text
Successful checkouts = 50
Rejected checkouts   = 50
Remaining stock      = 0
```

Most importantly:

```text
No overselling
```

This verifies that the thread-safe inventory implementation remains correct when used through the full checkout workflow.

---

# Notification Tests

## OrderEventPublisherTest

Tests observer registration and event publishing.

The publisher maintains observers implementing:

```text
OrderObserver
```

and notifies them when an order event occurs.

Tests cover behavior such as:

```text
Adding observers
Rejecting null observers
Notifying registered observers
Rejecting invalid order events
```

---

## EmailNotificationObserverTest

Tests the email notification observer's handling of order events and input validation.

The current implementation demonstrates notification behavior without connecting to an external email provider.

---

# Analytics Tests

## AnalyticsObserverTest

Tests the Observer Pattern connection between order events and analytics.

Flow:

```text
OrderEventPublisher
        ↓
AnalyticsObserver
        ↓
Stored Order
```

Tests verify that received orders are stored and invalid input is rejected.

---

## AnalyticsServiceTest

Tests analytics calculations performed using Java Streams.

The current analytics operations include:

### Total Revenue

Calculates revenue from paid orders.

```text
Paid Orders
    ↓
calculateTotalRevenue()
```

---

### Average Order Value

Calculates the average value of paid orders.

If there are no qualifying orders, the result safely defaults instead of producing an invalid value.

---

### Paid Order Count

Counts orders whose status represents successful payment.

---

### Orders by Payment Type

Groups paid orders by:

```text
CARD
UPI
WALLET
```

and counts them.

---

### Best-Selling Product

Uses order items and quantities to determine which product has sold the highest total quantity.

The result is represented with:

```text
Optional<Product>
```

because there may be no qualifying product.

---

### Revenue by Category

Groups order-item revenue by product category.

Example:

```text
Electronics → revenue
Books       → revenue
Clothing    → revenue
```

---

### Top Paid Orders

Filters paid orders, sorts them by total amount in descending order, and returns a requested number of top orders.

---

# Persistence Tests

## OrderFileRepositoryTest

Tests file-based persistence using temporary test files.

Operations include:

```text
Saving orders
Reading orders
Appending orders
Input validation
```

JUnit's temporary-directory support is used so tests do not depend on permanent local files.

This keeps persistence tests isolated and repeatable.

---

## OrderSerializationRepositoryTest

Tests Java object serialization.

The repository supports:

```text
Save one Order
Read one Order

Save multiple Orders
Read multiple Orders
```

Tests verify that serialized order information can be written and restored correctly.

Java serialization is included to demonstrate Core Java persistence concepts rather than as the long-term production storage mechanism.

---

# Integration Testing

## ECommerceOrderEngineIntegrationTest

The integration test verifies that the major modules work together as one complete workflow.

The flow includes:

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
OrderEventPublisher
   ↓
AnalyticsObserver
   ↓
AnalyticsService
   ↓
File Persistence
```

A representative scenario includes multiple products in a cart.

For example:

```text
Product 1:
Price    = 10,000
Quantity = 2

Product 2:
Price    = 1,000
Quantity = 3
```

Expected total:

```text
20,000 + 3,000
= 23,000
```

The integration test verifies behavior such as:

```text
Correct inventory reduction
Successful payment
Order status = PAID
Correct order total
Observer notification
Analytics collection
Revenue calculation
Persistence and read-back
```

This demonstrates that the independently tested modules also work correctly when combined.

---

# Testing Levels Used

The project currently demonstrates several testing levels.

```text
Unit Tests
    ↓
Component Tests
    ↓
Concurrency Tests
    ↓
Integration Test
```

## Unit Tests

Test individual classes in isolation.

Examples:

```text
ProductTest
CartTest
PaymentResultTest
AnalyticsServiceTest
```

## Component Tests

Test collaboration between several related classes.

Example:

```text
CheckoutService
+
InventoryService
+
PaymentProcessor
```

## Concurrency Tests

Test correctness when multiple threads operate simultaneously.

Examples:

```text
InventoryConcurrencyTest
CheckoutConcurrencyTest
```

## Integration Test

Tests the complete application workflow across multiple modules.

```text
ECommerceOrderEngineIntegrationTest
```

---

# Testing Philosophy

The project follows several testing principles.

## Test Successful Behavior

Verify that correct input produces the expected result.

## Test Invalid Input

Verify validation rules and exceptions.

## Test Failure Paths

A checkout system must work correctly not only when everything succeeds, but also when:

```text
Stock is unavailable
Payment fails
Partial reservation occurs
Invalid data is supplied
```

## Test State Restoration

Rollback tests verify that failed operations do not leave the system in an inconsistent state.

## Test Concurrency

Thread-safe code is tested under concurrent execution rather than assuming that using a concurrent collection automatically makes all application logic safe.

## Test Integration

Individual classes may work correctly in isolation while failing when connected together.

The end-to-end integration test reduces this risk.

---

# Current Test Baseline

Core Java V1 currently has:

```text
23 test classes
166 passing tests
0 failing tests
```

This baseline should remain green during future refactoring and technology migrations.

Before introducing a major change, the existing test suite provides a regression safety net.

---

# Future Testing Roadmap

As the application evolves, testing will also evolve.

## Maven

JUnit dependencies and test execution will be managed through Maven.

```text
mvn test
```

---

## JDBC + MySQL

Repository integration tests will verify:

```text
INSERT
SELECT
UPDATE
DELETE
Transactions
Rollback
```

---

## Hibernate / JPA

Tests will verify entity mapping, relationships, queries, and persistence behavior.

---

## Spring Boot

Additional technologies may include:

```text
Mockito
Spring Boot Test
MockMvc
Testcontainers
```

Testing layers will evolve toward:

```text
Controller Tests
      ↓
Service Tests
      ↓
Repository Tests
      ↓
Database Integration Tests
      ↓
End-to-End Tests
```

The goal is to preserve the strong regression coverage of Core Java V1 while expanding testing as the architecture grows.