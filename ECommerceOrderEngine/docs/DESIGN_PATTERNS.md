# Design Patterns

## Overview

The E-Commerce Order Processing & Analytics Engine uses design patterns where they solve a clear design problem.

Core Java V1 currently uses:

- Strategy Pattern
- Simple Factory Pattern
- Builder Pattern
- Observer Pattern

The goal is not to force patterns into the application, but to use them to improve separation of concerns, extensibility, maintainability, and testability.

---

## 1. Strategy Pattern

### Purpose

The Strategy Pattern is used for payment processing.

Different payment methods perform the same general operation:

```text
Process a payment
```

but each payment method can have a different implementation.

The common abstraction is:

```text
PaymentStrategy
```

Implementations are:

```text
CardPaymentStrategy
UpiPaymentStrategy
WalletPaymentStrategy
```

### Structure

```text
                PaymentStrategy
                      ↑
          ┌───────────┼───────────┐
          │           │           │
        Card          UPI       Wallet
       Strategy     Strategy    Strategy
          ↑           ↑           ↑
          └───────────┼───────────┘
                      │
               PaymentProcessor
```

`PaymentProcessor` depends on the `PaymentStrategy` interface rather than depending directly on a particular payment implementation.

### Example

```java
PaymentStrategy strategy =
        new CardPaymentStrategy();

PaymentProcessor processor =
        new PaymentProcessor(strategy);

PaymentResult result =
        processor.processPayment(1000.0);
```

The selected strategy can be replaced without changing the internal logic of `PaymentProcessor`.

### Benefits

- Supports interchangeable payment behavior
- Reduces conditional payment logic
- Makes new payment types easier to add
- Supports the Open/Closed Principle
- Improves testability
- Promotes programming to an abstraction

---

## 2. Simple Factory Pattern

### Purpose

The Simple Factory centralizes the creation of payment strategy objects.

Without a factory, higher-level application code might repeatedly contain:

```text
new CardPaymentStrategy();
new UpiPaymentStrategy();
new WalletPaymentStrategy();
```

Instead, object creation is handled by:

```text
PaymentFactory
```

### Structure

```text
PaymentType
    ↓
PaymentFactory
    ↓
PaymentStrategy
   /      |       \
Card     UPI     Wallet
```

### Example

```java
PaymentStrategy strategy =
        PaymentFactory.create(PaymentType.CARD);
```

The factory decides which concrete strategy should be created.

Conceptually:

```text
CARD
 ↓
PaymentFactory
 ↓
CardPaymentStrategy
```

and:

```text
UPI
 ↓
PaymentFactory
 ↓
UpiPaymentStrategy
```

### Benefits

- Centralizes object creation
- Reduces direct construction of concrete strategies
- Keeps higher-level code cleaner
- Makes payment selection easier to maintain

### Note

Simple Factory is a commonly used creational pattern, although it is not one of the original Gang of Four design patterns.

---

## 3. Builder Pattern

### Purpose

The Builder Pattern is used when creating an `Order`.

An order requires several pieces of information:

```text
Order ID
Customer
Order Items
Payment Type
```

Instead of passing all values through a long constructor call, the Builder provides a readable step-by-step construction process.

### Structure

```text
Order.Builder
     ↓
id(...)
     ↓
customer(...)
     ↓
items(...)
     ↓
paymentType(...)
     ↓
build()
     ↓
Order
```

### Example

```java
Order order = new Order.Builder()
        .id(orderId)
        .customer(customer)
        .items(orderItems)
        .paymentType(paymentType)
        .build();
```

Each builder method returns the same builder object, allowing method chaining.

The final:

```text
build()
```

creates the `Order`.

### Benefits

- Improves readability
- Avoids long constructor calls
- Keeps object construction organized
- Makes future optional properties easier to add
- Keeps validation inside the domain object

---

## 4. Observer Pattern

### Purpose

The Observer Pattern is used when multiple components need to react after an order event occurs.

For example, after an order is successfully paid:

```text
Send notification
Store order for analytics
```

`CheckoutService` should not need to directly depend on every component that wants to react to the event.

Instead, it publishes the order through:

```text
OrderEventPublisher
```

### Structure

```text
CheckoutService
      ↓
OrderEventPublisher
      ↓
OrderObserver
   /             \
  ↓               ↓
Email          Analytics
Observer       Observer
```

The observer abstraction is:

```text
OrderObserver
```

Current implementations are:

```text
EmailNotificationObserver
AnalyticsObserver
```

### Event Flow

```text
Checkout succeeds
      ↓
Order status = PAID
      ↓
OrderEventPublisher
      ↓
notifyObservers(order)
      ↓
 ┌────────────────────┐
 ↓                    ↓
EmailNotification   AnalyticsObserver
Observer                 ↓
                    Store Order
```

### Why this is useful

Without Observer, `CheckoutService` could become tightly coupled to classes such as:

```text
EmailNotificationObserver
AnalyticsObserver
Future SMS service
Future audit logger
Future Kafka publisher
```

With Observer, checkout only communicates through the common abstraction.

### Future Extensions

New observers could later be added for:

```text
SMS notifications
Audit logging
Kafka events
Loyalty points
Inventory reporting
```

without significantly changing `CheckoutService`.

### Benefits

- Loose coupling
- Easy extension
- Clear event-based architecture
- Checkout remains focused on checkout
- Notification and analytics remain independent
- Improves testing

---

# How the Patterns Work Together

The patterns are not isolated. They cooperate during checkout.

```text
Customer Checkout
       ↓
CheckoutService
       ↓
PaymentFactory
       ↓
creates PaymentStrategy
       ↓
PaymentProcessor
       ↓
Strategy executes payment
       ↓
Order.Builder
       ↓
creates Order
       ↓
OrderEventPublisher
       ↓
Observer notifications
```

So each pattern has a different responsibility:

```text
Strategy
→ How payment is performed

Factory
→ Which payment strategy is created

Builder
→ How an Order is constructed

Observer
→ Who reacts after an order event
```

---

# Relationship with SOLID Principles

The patterns also support several SOLID principles.

## Single Responsibility Principle

Each class has a focused responsibility.

Examples:

```text
InventoryService
→ manages inventory

PaymentProcessor
→ executes payments

PaymentFactory
→ creates payment strategies

OrderEventPublisher
→ distributes events

AnalyticsService
→ performs analytics calculations
```

---

## Open/Closed Principle

The payment system can be extended by creating another implementation of `PaymentStrategy`.

For example:

```java
public class BankTransferPaymentStrategy
        implements PaymentStrategy {

    @Override
    public PaymentResult pay(double amount) {
        // payment implementation
        return null;
    }
}
```

The existing `PaymentProcessor` does not need to be redesigned.

---

## Dependency Inversion Principle

Higher-level components depend on abstractions where appropriate.

For example:

```text
PaymentProcessor
       ↓
PaymentStrategy
```

rather than:

```text
PaymentProcessor
       ↓
CardPaymentStrategy
```

This reduces coupling between the payment processor and concrete payment implementations.

Constructor dependency injection is also used in components such as `CheckoutService`.

---

# Patterns Considered but Not Used

Not every design pattern is appropriate for the current version.

## State Pattern

The application currently represents order state using:

```text
OrderStatus
```

with values such as:

```text
CREATED
PAID
PROCESSING
SHIPPED
DELIVERED
CANCELLED
```

A full State Pattern is not currently necessary because the behavior associated with each state is still relatively simple.

It may become useful later if order states develop complex transition rules and state-specific behavior.

---

## Memento Pattern

The Memento Pattern is commonly used for:

```text
Undo
Restore previous state
Snapshots
```

The current cart and checkout requirements do not require this functionality.

Adding it would introduce unnecessary complexity.

---

## Abstract Factory

The application currently creates individual payment strategies rather than families of related objects.

Therefore, a Simple Factory is sufficient.

An Abstract Factory would add complexity without providing a meaningful benefit to the current requirements.

---

# Design Principle

The project follows this general rule:

```text
Use a design pattern when it solves a real design problem.

Do not add a pattern simply to increase the number
of patterns used in the project.
```

This keeps the architecture practical, understandable, and maintainable.