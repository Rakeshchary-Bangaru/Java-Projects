# Project Roadmap

## Overview

The E-Commerce Order Processing & Analytics Engine will be developed incrementally.

The goal is to evolve the current Core Java application into a database-backed web application and later into a Spring Boot backend.

Each stage builds on the previous one so that the same project demonstrates increasing backend engineering depth.

---

## Phase 1 - Core Java V1 

Status: Completed

Technologies and concepts:

- Core Java
- Object-Oriented Programming
- Collections Framework
- Streams API
- Exception Handling
- Generics
- Enums
- File I/O
- Java Serialization
- Concurrency
- ConcurrentHashMap
- ExecutorService
- CountDownLatch
- AtomicInteger
- JUnit 5
- SOLID Principles
- Design Patterns

Implemented features:

- Product and customer models
- Shopping cart
- Inventory management
- Thread-safe stock reservation
- Payment processing
- Card, UPI, and Wallet strategies
- Checkout orchestration
- Rollback handling
- Order creation
- Notifications
- Analytics
- File persistence
- Java serialization
- Concurrency testing
- End-to-end integration testing

Current baseline:

```text
166 tests passed
0 tests failed
```

---

## Phase 2 - Maven

Status: Next

Goal:

Convert the current IntelliJ-based project into a standard Maven project.

Technologies:

```text
Maven
pom.xml
Dependency Management
Maven Lifecycle
Plugins
JUnit Dependencies
```

Planned structure:

```text
src/
├── main/
│   ├── java/
│   └── resources/
│
└── test/
    └── java/
```

Main tasks:

- Create `pom.xml`
- Move production code to `src/main/java`
- Move tests to `src/test/java`
- Replace manually downloaded JUnit JAR
- Configure JUnit through Maven
- Run tests with Maven

Expected command:

```text
mvn test
```

All existing tests should continue to pass after migration.

---

## Phase 3 - JDBC + MySQL

Goal:

Replace temporary in-memory/file persistence with relational database persistence.

Technologies:

```text
MySQL
SQL
JDBC
MySQL Connector/J
PreparedStatement
ResultSet
Transactions
```

Planned database tables:

```text
products
customers
inventory
orders
order_items
payments
```

Planned repository layer:

```text
ProductRepository
CustomerRepository
InventoryRepository
OrderRepository
```

Possible implementations:

```text
JdbcProductRepository
JdbcCustomerRepository
JdbcInventoryRepository
JdbcOrderRepository
```

Architecture:

```text
Service
   ↓
Repository
   ↓
JDBC
   ↓
MySQL
```

---

## Phase 4 - Database Transactions

Goal:

Replace manual rollback behavior where appropriate with real database transactions.

Example flow:

```text
BEGIN TRANSACTION
      ↓
Update Inventory
      ↓
Insert Order
      ↓
Insert Order Items
      ↓
COMMIT
```

If an operation fails:

```text
Failure
   ↓
ROLLBACK
```

Concepts:

- `setAutoCommit(false)`
- `commit()`
- `rollback()`
- Transaction boundaries
- Data consistency

---

## Phase 5 - Servlets + JSP + Tomcat

Goal:

Turn the backend engine into a Java web application.

Technologies:

```text
Jakarta Servlets
JSP
JSTL
Apache Tomcat
HTML
CSS
HTTP
Sessions
Cookies
```

Planned controllers:

```text
ProductServlet
CartServlet
CheckoutServlet
OrderServlet
```

Possible JSP pages:

```text
products.jsp
cart.jsp
checkout.jsp
orders.jsp
```

Architecture:

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
   ↓
Browser
```

This stage will demonstrate the MVC pattern.

---

## Phase 6 - Hibernate + JPA

Goal:

Replace much of the manual JDBC persistence code with ORM-based persistence.

Technologies:

```text
JPA
Hibernate
ORM
EntityManager
JPQL
```

Concepts:

```text
@Entity
@Table
@Id
@OneToMany
@ManyToOne
@OneToOne
```

Architecture:

```text
Java Entity
     ↓
JPA / Hibernate
     ↓
MySQL
```

JDBC knowledge will still remain important because Hibernate ultimately works with relational database operations underneath.

---

## Phase 7 - Spring Core

Goal:

Introduce Spring's IoC container and dependency injection.

Technologies and concepts:

```text
Spring Core
IoC
Dependency Injection
Beans
@Component
@Service
@Repository
@Configuration
```

Existing constructor dependency injection concepts will be moved under Spring management.

Example:

```text
Current

Application
   ↓
new CheckoutService(...)
```

Later:

```text
Spring Container
      ↓
CheckoutService
      ↓
Dependencies injected automatically
```

---

## Phase 8 - Spring Boot

Goal:

Convert the application into a production-style backend service.

Technologies:

```text
Spring Boot
Spring MVC
Embedded Tomcat
Configuration
Jackson
REST
JSON
```

Architecture:

```text
Client
   ↓
REST Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

---

## Phase 9 - REST APIs

Planned APIs may include:

```text
GET    /products
GET    /products/{id}

POST   /cart/items
DELETE /cart/items/{id}

POST   /checkout

GET    /orders/{id}
GET    /orders

POST   /products
PUT    /products/{id}
DELETE /products/{id}
```

Concepts:

- HTTP methods
- Status codes
- Request bodies
- Response bodies
- JSON
- DTOs
- Validation
- Global exception handling

---

## Phase 10 - Spring Data JPA

Goal:

Simplify repository implementations.

Technologies:

```text
Spring Data JPA
JpaRepository
Derived Queries
JPQL
Pagination
Sorting
```

Example concept:

```text
ProductRepository
        ↓
JpaRepository<Product, Long>
```

---

## Phase 11 - Spring Security

Goal:

Add authentication and authorization.

Technologies and concepts:

```text
Spring Security
JWT
Password Hashing
Authentication
Authorization
Role-Based Access Control
```

Possible roles:

```text
CUSTOMER
ADMIN
```

Example permissions:

```text
CUSTOMER
→ browse products
→ manage cart
→ checkout
→ view own orders

ADMIN
→ manage products
→ manage inventory
→ manage orders
```

---

## Phase 12 - Advanced Testing

Technologies:

```text
JUnit 5
Mockito
Spring Boot Test
MockMvc
Testcontainers
```

Testing layers:

```text
Unit Tests
     ↓
Service Tests
     ↓
Repository Tests
     ↓
Controller Tests
     ↓
Integration Tests
```

The goal is to preserve and expand the strong automated test coverage established in Core Java V1.

---

## Phase 13 - Docker

Goal:

Containerize the application and database.

Technologies:

```text
Docker
Dockerfile
Docker Compose
```

Architecture:

```text
Docker Compose
├── E-Commerce Backend
└── MySQL
```

---

## Phase 14 - Advanced Backend Technologies

After the monolithic application is stable, advanced backend concepts can be introduced where appropriate.

Possible technologies:

```text
Redis
Kafka
Microservices
Docker
Kubernetes
CI/CD
Cloud
```

Potential uses:

```text
Redis
→ caching

Kafka
→ order events

Microservices
→ payment, inventory, order services

Docker/Kubernetes
→ deployment and scaling

CI/CD
→ automated build, test, and deployment
```

These technologies will only be added when they solve a meaningful architectural requirement.

---

# Evolution of the Project

```text
Core Java V1 
      ↓
Maven
      ↓
JDBC + MySQL
      ↓
Database Transactions
      ↓
Servlet + JSP + Tomcat
      ↓
Hibernate / JPA
      ↓
Spring Core
      ↓
Spring Boot
      ↓
REST APIs
      ↓
Spring Data JPA
      ↓
Spring Security
      ↓
Advanced Testing
      ↓
Docker
      ↓
Redis / Kafka
      ↓
Microservices
      ↓
Cloud + CI/CD
```

---

## Final Goal

The final project should demonstrate the evolution from:

```text
Core Java application
```

to:

```text
Database-backed Java application
```

to:

```text
Java web application
```

to:

```text
Spring Boot REST backend
```

and eventually:

```text
Scalable production-style backend architecture
```

The emphasis is on learning each layer properly and applying it to the same evolving project rather than creating disconnected tutorial applications.