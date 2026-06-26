# Architecture

## Runtime View

```mermaid
flowchart LR
  UI["React Storefront"] --> GW["API Gateway"]
  GW --> AUTH["auth-service"]
  GW --> PRODUCT["product-service"]
  GW --> CART["cart-service"]
  GW --> ORDER["order-service"]
  ORDER --> CART
  ORDER --> PRODUCT
  ORDER --> KAFKA["Kafka"]
  KAFKA --> PAYMENT["payment-service"]
  PAYMENT --> KAFKA
  KAFKA --> ORDER
  CONFIG["config-server"] --> GW
  CONFIG --> AUTH
  CONFIG --> PRODUCT
  CONFIG --> CART
  CONFIG --> ORDER
  CONFIG --> PAYMENT
  DISCOVERY["discovery-server"] --> GW
```

## Checkout Sequence

```mermaid
sequenceDiagram
  participant Customer
  participant Frontend
  participant Gateway
  participant Cart
  participant Order
  participant Product
  participant Kafka
  participant Payment

  Customer->>Frontend: Checkout cart
  Frontend->>Gateway: POST /order-service/api/orders/checkout
  Gateway->>Order: Forward checkout
  Order->>Cart: Load cart by user
  Order->>Product: Reserve inventory per line
  Order->>Order: Persist order as INVENTORY_RESERVED
  Order->>Kafka: Publish ORDER_CREATED
  Order->>Cart: Clear cart
  Kafka->>Payment: Consume ORDER_CREATED
  Payment->>Payment: Persist payment
  Payment->>Kafka: Publish PAYMENT_COMPLETED
  Kafka->>Order: Consume PAYMENT_COMPLETED
  Order->>Order: Mark order COMPLETED
```

## Service Boundaries

- Product owns catalog identity, price, active status, and inventory.
- Cart owns a user's intent to buy and stores price/product snapshots for UX.
- Order owns checkout, reservation, order totals, and order lifecycle.
- Payment owns payment records and emits payment result events.
- Auth owns customer identity and signed bearer tokens.

## Remaining Production Enhancements

- Replace H2 with PostgreSQL per service and Flyway migrations.
- Add Testcontainers for Kafka and database integration tests.
- Move token validation to the gateway or a shared auth filter.
- Add Prometheus/Grafana dashboards and distributed tracing.
- Implement a transactional outbox for event publishing.
