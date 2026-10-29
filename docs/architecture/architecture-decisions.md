
# E-Commerce Platform — Architecture Decisions

## 1. Architecture Style

The platform uses a hybrid architecture combining a modular monolith
with independently deployable microservices.

Business capabilities are separated according to ownership, deployment
needs, data consistency, and scaling requirements.

## 2. Technology Stack

- Java 21
- Spring Boot 3.5.7 baseline
- Maven multi-module build
- Spring Security and Keycloak
- REST APIs using Spring RestClient or OpenFeign where appropriate
- Apache Kafka for asynchronous events
- MySQL for relational and transactional data
- MongoDB for the product catalogue where document storage is useful
- Docker Compose for local development

## 3. Application Boundaries

### Modular Monolith
- Customer account management and profile preferences
- Shopping cart
- Closely related application workflows

### Independent Microservices
- Product Service
- Inventory Service
- Order Service
- Payment Service
- Invoice Service
- Notification Service
- API Gateway

Customer Service is initially developed as an independent
Spring Boot application.

## 4. Data Ownership

Each service owns its data and controls access through its APIs
or published events.

Services must not directly modify another service's database tables.

The modular monolith may use one database initially, with clear
module boundaries and controlled data access.

## 5. Communication

- Synchronous communication: REST over HTTP.
- Asynchronous communication: Kafka events.
- Use asynchronous messaging where independent processing,
  decoupling, or eventual consistency provides a benefit.

## 6. Security

- OAuth 2.0 and OpenID Connect through Keycloak.
- Spring Security for API authorization.
- Validate tokens and enforce permissions at appropriate boundaries.
- Never store credentials, secrets, or access tokens in source control.

## 7. Reliability and Observability

- Actuator health endpoints.
- Consistent API error responses.
- Input validation.
- Automated unit and integration tests.
- Timeouts and resilience patterns for remote calls.
- Structured logging, metrics, and distributed tracing as the
  platform evolves.

## 8. Deployment

Applications run independently in Docker containers.
Docker Compose orchestrates local development dependencies.
Kubernetes and CI/CD are introduced after the core platform is stable.

## 9. Design Principles

- High cohesion and low coupling.
- Explicit module and service boundaries.
- Independent data ownership.
- Secure defaults.
- Automated testing.
- Avoid premature infrastructure complexity.
