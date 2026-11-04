# E-Commerce Hybrid Architecture Platform

## 1. Project Overview

A Java 21 and Spring Boot-based E-Commerce application demonstrating a hybrid architecture combining:

- Modular Monolith
- Independent Microservices
- Spring Cloud API Gateway

The project demonstrates service separation, REST communication, authentication, containerization and integration workflows.

## 2. Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 |
| Cloud | Spring Cloud 2025.0.3 |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security, JWT |
| Relational Database | MySQL 8 |
| NoSQL Database | MongoDB 8 |
| Cache | Redis 7 |
| Messaging | Apache Kafka 4.4 |
| Resilience | Resilience4j |
| Database Migration | Flyway |
| Build | Maven |
| Containerization | Docker, Docker Compose |

## 3. Architecture

### Modular Monolith
Contains Customer, Product, Inventory, Order, Payment, Invoice, Notification and Authentication modules.

### Independent Microservices
- Customer Service
- Product Service
- Inventory Service
- Order Service
- Payment Service
- Invoice Service
- Notification Service

### API Gateway
Central entry point for API requests. Handles routing, JWT authentication, authorization and Redis-based rate limiting.

## 4. Service Ports

| Application | Port |
|---|---:|
| API Gateway | 8090 |
| Modular Monolith | 8091 (host) |
| Customer Service | 8081 |
| Product Service | 8082 |
| Order Service | 8083 |
| Inventory Service | 8084 |
| Payment Service | 8085 |
| Invoice Service | 8086 |
| Notification Service | 8087 |
| Kafka | 9092 |
| MongoDB | 27018 |
| MySQL | 3307 |
| Redis | 6379 |

## 5. Order Processing Workflow

1. Client authenticates through the Gateway.
2. Gateway validates the JWT.
3. Order Service receives an order request.
4. Product Service retrieves product details.
5. Inventory Service reserves stock.
6. Order Service persists the order.
7. Payment Service processes simulated payment.
8. Order Service updates the order status.

## 6. Security

- JWT-based authentication.
- Spring Security authorization.
- Role-based access control.
- Public authentication endpoints.
- Protected business APIs.
- Gateway-level rate limiting using Redis.

## 7. Docker Deployment

Build and start the application:

```powershell
docker compose up -d --build
```

Check containers:

```powershell
docker compose ps
```

View Gateway logs:

```powershell
docker logs ecommerce-gateway --tail 100
```

Stop containers:

```powershell
docker compose down
```

## 8. Environment Configuration

Create a `.env` file in the project root containing:

- DB_PASSWORD
- JWT_SECRET
- MONGO_PASSWORD
- MONGO_PASSWORD_URLENCODED

Do not commit `.env` or expose credentials in source control.

MySQL and Redis are currently accessed through the host machine. MongoDB and Kafka run through Docker Compose.

## 9. Known Learning Limitations

- Inventory stock is currently maintained in memory.
- Payment processing is simulated.
- Invoice and Notification microservices expose independent REST APIs; automatic order-event integration between these services has not been implemented.
- The independent microservices do not yet reproduce every event-driven capability of the modular monolith.
- Distributed transaction compensation is not implemented.

## 10. Project Objective

Demonstrate practical understanding of modular monolith architecture, microservices, API Gateway, JWT security, synchronous service communication, messaging, caching, resilience and Docker-based deployment.