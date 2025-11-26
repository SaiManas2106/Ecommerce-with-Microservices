
# E-Commerce Backend – Microservices with Kafka

Backend for a simple e-commerce system built using a microservices architecture with:

- Java 17, Spring Boot 3
- Spring Cloud (Eureka, Config Server, Gateway)
- Apache Kafka for async messaging
- H2 in-memory databases (per service)
- Docker Compose for local orchestration

## Architecture

Services:

- `discovery-server` – Eureka service registry (`:8761`)
- `config-server` – Centralized config (`:8888`)
- `api-gateway` – Single entrypoint and routing (`:8080`)
- `product-service` – Product catalog (`:8081`)
- `cart-service` – User carts (`:8082`)
- `order-service` – Order creation & status (`:8083`)
- `payment-service` – Payment processing (`:8084`)

Async flow (Kafka):

- `order-service` publishes **order-created** events
- `payment-service` consumes them, processes payment, publishes **payment-completed**
- `order-service` consumes **payment-completed** and updates order status

Idempotency & reliability:

- Both `order-service` and `payment-service` keep a `ProcessedEvent` table to ignore duplicate events
- Simple retry loops around Kafka handlers
- A scheduler cancels long-pending orders (timeout handling)

## Tech Stack

- **Backend:** Java 17, Spring Boot, Spring Web, Spring Data JPA, Validation
- **Microservices:** Spring Cloud Eureka, Config, Gateway
- **Messaging:** Apache Kafka (Spring Kafka)
- **Database:** H2 (per service)
- **Build:** Maven
- **Containers:** Docker, Docker Compose
- **Docs:** springdoc-openapi (Swagger UI on each HTTP service)

## Getting Started

### Prerequisites

- Java 17
- Maven 3.8+
- Docker & Docker Compose

### Build

From project root:

```bash
mvn clean package
````

### Run everything with Docker Compose

```bash
docker compose up --build
```

### Useful URLs

* Eureka Dashboard: `http://localhost:8761`
* API Gateway: `http://localhost:8080`
* Product Service Swagger: `http://localhost:8081/swagger-ui/index.html`
* Cart Service Swagger: `http://localhost:8082/swagger-ui/index.html`
* Order Service Swagger: `http://localhost:8083/swagger-ui/index.html`
* Payment Service Swagger: `http://localhost:8084/swagger-ui/index.html`

## Example Flow

1. Create a product:

   ```bash
   POST http://localhost:8080/product-service/api/products
   ```

2. Add product to a user’s cart:

   ```bash
   POST http://localhost:8080/cart-service/api/cart/user123/items
   ```

3. Create an order:

   ```bash
   POST http://localhost:8080/order-service/api/orders
   ```

4. Order is created as `PENDING`, Kafka triggers payment flow, and:

   * `payment-service` processes payment
   * `order-service` updates status to `COMPLETED`, `FAILED`, or `CANCELLED` (timeout)


