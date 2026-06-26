
# E-Commerce Full Stack Microservices

This project is a full-stack ecommerce system built with Spring Boot microservices, Kafka, service discovery, centralized configuration, and a React storefront. It is designed as a portfolio project that demonstrates real ecommerce workflows instead of shallow CRUD-only endpoints.

## Services

- `auth-service` - customer registration, login, signed bearer tokens, user roles
- `product-service` - catalog, SKU uniqueness, categories, product status, inventory reservation/restock
- `cart-service` - product-backed cart with price snapshots, quantity merge/update/remove, totals
- `order-service` - checkout from cart, inventory reservation, order lifecycle, Kafka order events
- `payment-service` - Kafka payment processing, idempotent consumers, retry/DLT handling
- `api-gateway` - single external API entrypoint
- `config-server` - centralized service config
- `discovery-server` - Eureka service registry
- `frontend` - React + TypeScript storefront connected to gateway APIs

## Main Checkout Flow

1. Customer registers or logs in through `auth-service`.
2. Customer browses active products from `product-service`.
3. Customer adds products to `cart-service`; cart validates product availability and stores price snapshots.
4. Customer checks out through `order-service`.
5. `order-service` reads the cart, reserves inventory in `product-service`, creates an order, publishes `ORDER_CREATED`, and clears the cart.
6. `payment-service` consumes the order event, creates a payment, publishes `PAYMENT_COMPLETED`.
7. `order-service` consumes the payment event and marks the order `COMPLETED`.

## Reliability Features

- Idempotent event consumers with `ProcessedEvent` tables and unique event IDs
- Kafka retry topics with DLT handlers
- Correlation IDs on order/payment events
- DTO/service layers instead of controller-to-repository business logic
- Centralized validation error responses per service
- Inventory reservation before payment flow starts
- Focused tests for token integrity, duplicate SKU handling, and stock reservation rules

## Run Locally

Build backend modules:

```bash
mvn clean test
```

Build frontend:

```bash
cd frontend
npm install
npm run build
```

Run all services:

```bash
docker compose up --build
```

Useful URLs:

- Frontend: `http://localhost:5173`
- Gateway: `http://localhost:8080`
- Eureka: `http://localhost:8761`
- Auth Swagger: `http://localhost:8085/swagger-ui/index.html`
- Product Swagger: `http://localhost:8081/swagger-ui/index.html`
- Cart Swagger: `http://localhost:8082/swagger-ui/index.html`
- Order Swagger: `http://localhost:8083/swagger-ui/index.html`
- Payment Swagger: `http://localhost:8084/swagger-ui/index.html`

## Gateway Paths

- `/auth-service/api/auth/register`
- `/auth-service/api/auth/login`
- `/product-service/api/products`
- `/cart-service/api/cart/{userId}`
- `/order-service/api/orders/checkout`

## Portfolio Talking Points

This project demonstrates service decomposition, async payment orchestration, idempotent event processing, inventory consistency, API gateway routing, customer identity, a connected frontend, Dockerized local orchestration, and tests around business-critical rules.
