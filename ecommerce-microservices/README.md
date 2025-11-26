# E-commerce Backend with Microservices

This repository contains a small e-commerce backend built as a microservices architecture using:

- Java 17, Spring Boot 3, Spring Cloud
- Service discovery with Eureka
- API Gateway with Spring Cloud Gateway
- Centralized configuration with Spring Cloud Config
- Async messaging with Apache Kafka
- H2 in-memory databases for each service
- Docker Compose orchestration

## Modules

- discovery-server
- config-server
- api-gateway
- product-service
- cart-service
- order-service
- payment-service

## Quick start

Build all services:

```bash
mvn clean package
```

Run the full stack with Docker Compose:

```bash
docker compose up --build
```

Key entry points:

- Eureka dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Product service (through gateway): http://localhost:8080/product-service/api/products
