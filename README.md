# Order Notification System

A microservices-based order management system built with Spring Boot, Apache Kafka, and OAuth 2.0. When an order is placed, an event is published to Kafka and the Notification Service consumes it to send a real-time email confirmation.

---

## Architecture

```
Client (Postman)
    │
    ▼
API Gateway (:8080)          ← JWT validation, routing
    │
    ▼
Order Service (:8081)        ← REST API, MySQL, Kafka Producer
    │
    ├──► MySQL (ordersdb)     ← persists orders
    │
    └──► Kafka (order-placed-topic)
              │
              ▼
    Notification Service     ← Kafka Consumer, Email alerts
    
Eureka Server (:8761)        ← Service discovery
Keycloak (:8082)             ← OAuth 2.0 authentication
```

---

## Technologies

| Technology | Purpose |
|---|---|
| Spring Boot 3.x | Core framework for all services |
| Spring Cloud Gateway | API Gateway — single entry point |
| Netflix Eureka | Service discovery and registration |
| Apache Kafka | Async event-driven communication |
| Spring Security + OAuth 2.0 | JWT-based authentication |
| Keycloak | Identity and access management |
| OpenFeign | Inter-service HTTP communication |
| Spring Data JPA | ORM for database operations |
| MySQL 8.0 | Relational database |
| Docker | Containerization |
| Lombok | Boilerplate reduction |

---

## Services

### Eureka Server (Port 8761)
Service registry. All Spring Boot microservices register here on startup and discover each other by name instead of hardcoded URLs.

### API Gateway (Port 8080)
Single entry point for all client requests. Validates JWT tokens issued by Keycloak before forwarding requests to downstream services. Uses `lb://` prefix for Eureka-based load balancing.

### Order Service (Port 8081)
Core service that handles order lifecycle. Exposes REST endpoints for creating and managing orders. After saving an order to MySQL, it publishes an `OrderEvent` to the `order-placed-topic` Kafka topic.

### Notification Service (Port 8082)
Stateless service with no REST endpoints. Listens to the `order-placed-topic` Kafka topic via `@KafkaListener`. On receiving an event, sends an email confirmation to the customer using Spring Mail (Gmail SMTP).

---

## Kafka Flow

```
Order Service                    Kafka                    Notification Service
     │                             │                              │
     │  kafkaTemplate.send()       │                              │
     │ ──────────────────────────► │                              │
     │  topic: order-placed-topic  │   @KafkaListener             │
     │  key:   orderNumber         │ ────────────────────────────►│
     │  value: OrderEvent (JSON)   │                              │
                                                         sends email to customer
```

**Topic:** `order-placed-topic`
**Producer:** Order Service
**Consumer:** Notification Service
**Consumer Group:** `notification-group`
**Serialization:** JSON (Spring Kafka JsonSerializer / JsonDeserializer)

---

## API Endpoints

### Order Service

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/orders` | `ROLE_USER` | Place a new order |
| `GET` | `/api/orders` | `ROLE_ADMIN` | Get all orders |
| `GET` | `/api/orders/{id}` | `ROLE_USER` | Get order by ID |
| `PATCH` | `/api/orders/{id}/status` | `ROLE_ADMIN` | Update order status |

### Request Body — Place Order

```json
{
  "customerName": "John Doe",
  "customerEmail": "john@gmail.com",
  "productName": "Laptop",
  "quantity": 1,
  "totalAmount": 500.00
}
```

### Response — Order Placed

```json
{
  "orderId": 1,
  "orderNumber": "ORD-a1b2c3",
  "customerName": "John Doe",
  "productName": "Laptop",
  "quantity": 1,
  "totalAmount": 500.00,
  "status": "PLACED",
  "createdAt": "2026-05-18T10:00:00"
}
```

---

## Order Status Lifecycle

```
PLACED → CONFIRMED → CANCELLED
```

---

## Security

Authentication is handled by **Keycloak**. Every request to the API Gateway must include a valid Bearer JWT token.

### Get a Token (Postman)

```
POST http://localhost:8082/realms/order-realm/protocol/openid-connect/token

Body (x-www-form-urlencoded):
  grant_type    = password
  client_id     = ordernotification
  client_secret = <your-client-secret>
  username      = testuser
  password      = test123
```

### Use the Token

```
Authorization: Bearer <access_token>
```

### Keycloak Setup

| Item | Value |
|---|---|
| Realm | `order-realm` |
| Client ID | `ordernotification` |
| Client Type | Confidential |
| Grant Type | Direct Access Grants (password) |
| Roles | `ROLE_USER`, `ROLE_ADMIN` |

### JWT Role Extraction

Keycloak embeds roles inside `realm_access.roles` in the JWT. A custom `KeycloakRoleConverter` extracts them and maps to Spring Security `GrantedAuthority` with `ROLE_` prefix.

---

## Running the Project

### Prerequisites

- Docker Desktop installed and running
- At least 6GB RAM allocated to Docker

### Start all services

```bash
docker-compose up -d
```

### Check all containers are running

```bash
docker-compose ps
```

### Stop all services

```bash
docker-compose down
```

### View logs for a specific service

```bash
docker logs order-service -f
docker logs notification -f
docker logs apigateway -f
```

---

## Docker Services

| Container | Image | Port |
|---|---|---|
| `eureka` | `jaswanth6300/eureka:v1` | 8761 |
| `orderservice` | `jaswanth6300/orderservice:v1` | — (internal) |
| `notification` | `jaswanth6300/notification:v1` | — (internal) |
| `apigateway` | `jaswanth6300/apigateway:v1` | 8080 |
| `keycloak` | `keycloak/keycloak:latest` | 8082 |
| `kafka` | `confluentinc/cp-kafka:7.5.0` | 9092 |
| `zookeeper` | `confluentinc/cp-zookeeper:7.5.0` | — (internal) |
| `ordersdb` | `mysql:8.0` | 3307 |

---

## Keycloak First-Time Setup

After running `docker-compose up`, Keycloak starts fresh. Complete this setup once:

1. Open `http://localhost:8082` → login with `admin / admin`
2. Create realm → `order-realm`
3. Create client → `ordernotification` (confidential, Direct Access Grants ON)
4. Create roles → `ROLE_USER`, `ROLE_ADMIN`
5. Create user → `testuser`, set password, assign `ROLE_USER`
6. Copy client secret from Credentials tab

---

## Environment Variables

### Order Service

| Variable | Value |
|---|---|
| `spring.datasource.url` | `jdbc:mysql://ordersdb:3306/orderdb` |
| `spring.kafka.producer.bootstrap-servers` | `kafka:29092` |
| `eureka.client.service-url.defaultZone` | `http://eureka:8761/eureka` |
| `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` | `http://keycloak:8080/realms/order-realm/protocol/openid-connect/certs` |

### Notification Service

| Variable | Value |
|---|---|
| `spring.kafka.consumer.bootstrap-servers` | `kafka:29092` |
| `spring.kafka.consumer.group-id` | `notification-group` |
| `spring.mail.host` | `smtp.gmail.com` |
| `spring.mail.port` | `587` |

---

## Project Structure

```
order-notification-system/
├── apigateway/
│   ├── src/main/java/com/order/apigateway/
│   │   └── config/
│   │       ├── SecurityConfig.java
│   │       └── KeycloakRoleConverter.java
│   ├── src/main/resources/application.properties
│   └── Dockerfile
├── eurekaserver/
│   ├── src/main/java/com/order/eureka/
│   ├── src/main/resources/application.properties
│   └── Dockerfile
├── order/
│   ├── src/main/java/com/order/
│   │   ├── controller/    OrderController.java
│   │   ├── service/       OrderService.java, OrderServiceImpl.java
│   │   ├── producer/      OrderEventProducer.java
│   │   ├── model/         Order.java, OrderEvent.java
│   │   ├── dto/           OrderRequestDTO.java, OrderResponseDTO.java
│   │   └── repository/    OrderRepository.java
│   ├── src/main/resources/application.properties
│   └── Dockerfile
├── notification/
│   ├── src/main/java/com/order/notification/
│   │   ├── consumer/      OrderEventConsumer.java
│   │   └── model/         OrderEvent.java
│   ├── src/main/resources/application.properties
│   └── Dockerfile
└── docker-compose.yml
```

---

## Common Issues and Fixes

**Eureka connection refused**
Order Service inside Docker trying to reach `localhost:8761`. Fix: use container name `eureka:8761` in `defaultZone`.

**Kafka deserialization error**
`ClassNotFoundException` for `OrderEvent`. Fix: add `spring.json.use.type.headers=false` and `spring.json.value.default.type` in Notification Service properties.

**Keycloak 401 Unauthorized**
Wrong realm in `jwk-set-uri`. Fix: ensure it points to `order-realm` not `master`.

**MySQL connection refused**
Using `localhost` instead of container name. Fix: use `ordersdb:3306` in datasource URL.

**API Gateway Tomcat instead of Netty**
`spring-boot-starter-web` present in Gateway pom.xml. Fix: remove it, keep only `spring-boot-starter-webflux`.

---

