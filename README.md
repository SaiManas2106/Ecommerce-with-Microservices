# E-Commerce Full Stack Microservices

A full-stack ecommerce platform built with Spring Boot microservices, Kafka, service discovery, centralized configuration, and a React storefront. The project demonstrates real ecommerce workflows including customer authentication, product catalog management, product-backed carts, checkout, inventory reservation, asynchronous payment processing, idempotent event consumers, and retry/DLT handling.

The application source lives in [`ecommerce-microservices`](ecommerce-microservices).

## Highlights

- React + TypeScript storefront connected to the API Gateway
- Customer registration and login through `auth-service`
- Product catalog with SKU, category, status, and inventory APIs
- Cart service that validates products, stores price snapshots, merges quantities, and calculates totals
- Checkout flow that reserves inventory before publishing order events
- Kafka-based order/payment workflow with correlation IDs, retries, DLT handlers, and idempotency
- Eureka discovery, Config Server, API Gateway, Docker Compose, Swagger UI, and focused tests

## Services

- `auth-service` - customer identity, roles, signed bearer tokens
- `product-service` - catalog, pricing, product status, inventory reservation/restock
- `cart-service` - user cart, product validation, price snapshots, totals
- `order-service` - checkout, inventory reservation, order lifecycle, Kafka order events
- `payment-service` - payment records, Kafka payment events, idempotent processing
- `api-gateway` - single external entrypoint
- `config-server` - centralized configuration
- `discovery-server` - Eureka registry
- `frontend` - ecommerce storefront

## Run Locally

```bash
cd ecommerce-microservices
mvn clean test
docker compose up --build
```

Frontend only:

```bash
cd ecommerce-microservices/frontend
npm install
npm run build
npm run dev
```

Useful URLs:

- Frontend: `http://localhost:5173`
- API Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`
- Auth Swagger: `http://localhost:8085/swagger-ui/index.html`
- Product Swagger: `http://localhost:8081/swagger-ui/index.html`
- Cart Swagger: `http://localhost:8082/swagger-ui/index.html`
- Order Swagger: `http://localhost:8083/swagger-ui/index.html`
- Payment Swagger: `http://localhost:8084/swagger-ui/index.html`

## Documentation

- [Application README](ecommerce-microservices/README.md)
- [Architecture and checkout sequence](ecommerce-microservices/docs/architecture.md)

## Portfolio Focus

This repository is intended to show production-style backend thinking: service boundaries, authoritative pricing, inventory consistency, asynchronous workflows, idempotency, retry/DLT behavior, customer identity, typed frontend integration, Dockerized local orchestration, and tests around critical business rules.
