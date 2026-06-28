# Unibank-SmartOrder

**Unibank-SmartOrder** is a production-grade, event-driven Order Management System (OMS). It manages the full lifecycle of customer orders — from product discovery and order placement through payment processing and customer notification. The system is designed for high concurrency, strict data consistency, fault tolerance, and operational observability.

---

## 🏗️ Architecture & Project Overview

The system is decomposed into three primary bounded contexts, implemented as independently deployable microservices following **Clean Architecture**, **Domain Driven Design (DDD)**, and **Hexagonal Architecture** principles.

Inter-service communication is handled via an event-driven architecture using the **Outbox Pattern** and a message broker to guarantee eventual consistency without tight coupling.

### Microservices
1. **Order Service (`:8080`)**: Manages the product catalog, handles order creation with atomic stock decrements, and deals with identity and authentication.
2. **Payment Service (`:8081`)**: Processes payments via an external (emulated) gateway. Handles retries, circuit breaking, and idempotency.
3. **Notification Service (`:8082`)**: Dispatches structured notifications to customers on order events and stores an auditable history in a document store.

---

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot 3.3
- **Architecture**: Microservices, Hexagonal Architecture, Domain Driven Design (DDD), Event-Driven Architecture (Outbox Pattern)
- **Message Broker**: RabbitMQ
- **Databases**:
  - PostgreSQL (Relational data for Orders & Payments)
  - MongoDB (Document store for Notifications)
  - Redis (Caching and distributed rate limiting)
- **Observability**: OpenTelemetry, Prometheus, Grafana
- **Deployment**: Docker, Docker Compose

---

## 🚀 How to Run Locally with Docker

The entire stack can be launched via Docker Compose with a single command. It includes the microservices, databases, message broker, and observability tools.

1. **Navigate to the deployment directory**:
   ```bash
   cd deployment
   ```

2. **Setup environment variables**:
   Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
   *(Optional) You can edit `.env` to customize database credentials or JWT secrets.*

3. **Start the infrastructure and services**:
   ```bash
   docker-compose up -d
   ```
   *(Or use `docker compose up -d` depending on your Docker version).*

4. **Verify it's running**:
   - Order Service: `http://localhost:8080`
   - Payment Service: `http://localhost:8081`
   - Notification Service: `http://localhost:8082`
   - RabbitMQ Management: `http://localhost:15672` (admin / supersecret_rmq_password)
   - Grafana: `http://localhost:3000` (admin / admin)
   - Prometheus: `http://localhost:9090`

To tear down the environment:
```bash
docker-compose down -v
```

---

## 📡 API Endpoints Overview

All APIs are exposed under the `/api/v1` context path.

### Identity & Authentication (Order Service)
- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - Login and receive JWT tokens
- `POST /api/v1/auth/refresh` - Refresh JWT token
- `POST /api/v1/auth/logout` - Logout and blocklist the token

### Catalog & Products (Order Service)
- `GET /api/v1/products` - List products with pagination and caching
- `GET /api/v1/products/{id}` - Get product details
- `PATCH /api/v1/products/{id}/stock` - Update product stock (Admin only)

### Order Management (Order Service)
- `POST /api/v1/orders` - Place a new order
- `GET /api/v1/orders` - List orders for the authenticated user
- `GET /api/v1/orders/{id}` - Get order details
- `POST /api/v1/orders/{id}/cancel` - Cancel an order

### Notifications (Notification Service)
- `GET /api/v1/notifications` - Get notifications for a user
- `GET /api/v1/notifications/{id}` - Get notification details

---

## 🔐 Environment Variables

The project uses `.env` files for configuration. Here is a breakdown of the core environment variables available in `deployment/.env.example`:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `DB_HOST` | Database host | `localhost` |
| `DB_PORT` | Database port | `5432` |
| `DB_NAME` | Main order database name | `order_db` |
| `DB_USER` | PostgreSQL username | `smartorder` |
| `DB_PASSWORD` | PostgreSQL password | `supersecret_db_password` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | `supersecret_redis_password` |
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ AMQP port | `5672` |
| `RABBITMQ_USER` | RabbitMQ username | `admin` |
| `RABBITMQ_PASSWORD`| RabbitMQ password | `supersecret_rmq_password` |
| `JWT_SECRET` | Secret key used to sign JWTs (Must be 512+ bits) | `this-is-a-very-secret-key...` |
