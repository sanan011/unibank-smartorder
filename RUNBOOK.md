# Unibank-SmartOrder Runbook

This runbook provides operational instructions for managing the Unibank-SmartOrder system locally and in production-like environments using Docker Compose.

---

## 🚀 1. Starting and Stopping Services

The entire infrastructure and application stack is managed via Docker Compose in the `deployment` directory.

### Start All Services
To start all databases, message brokers, monitoring tools, and microservices in the background:
```bash
cd deployment
docker-compose up -d
```

### Stop All Services
To stop all services and remove the containers (data in named volumes will persist):
```bash
docker-compose down
```
To stop all services and **delete all persistent data** (databases, message queues):
```bash
docker-compose down -v
```

### Restart a Specific Service
If a specific service (e.g., `order-service`) is misbehaving or needs to pick up environment variable changes:
```bash
docker-compose restart order-service
```

### View Logs
To tail the logs of all services:
```bash
docker-compose logs -f
```
To tail logs for a specific service:
```bash
docker-compose logs -f payment-service
```

---

## 🩺 2. Checking Service Health

### Docker Container Status
Check if all containers are running and passing their health checks:
```bash
docker-compose ps
```
*Healthy containers will show `(healthy)` next to their status.*

### Spring Boot Actuator Endpoints
Each microservice exposes an actuator health endpoint. If the service is running properly, it will return `{"status":"UP"}`.
- **Order Service**: `curl http://localhost:8080/actuator/health`
- **Payment Service**: `curl http://localhost:8081/actuator/health`
- **Notification Service**: `curl http://localhost:8082/actuator/health`

### Monitoring Dashboards
- **RabbitMQ Management**: `http://localhost:15672` (Check connections, queues, and message rates)
- **Grafana**: `http://localhost:3000` (View application metrics, JVM stats, and API latencies)
- **Prometheus**: `http://localhost:9090` (Query raw metrics and check target health)

---

## 🚨 3. Common Errors and Fixes

### Error: `Connection refused` (Database or RabbitMQ)
- **Symptom**: Microservices crash on startup with `java.net.ConnectException` or `AmqpConnectException`.
- **Cause**: The service started before the database or RabbitMQ was fully initialized and ready to accept connections.
- **Fix**: Docker Compose handles this via `depends_on` and `condition: service_healthy`. If it still occurs, simply restart the failing service after the infrastructure is up: `docker-compose restart <service-name>`.

### Error: `Bind for 0.0.0.0:8080 failed: port is already allocated`
- **Symptom**: Docker fails to start a container because a port is taken.
- **Cause**: Another application on your host machine (or a zombie Docker container) is using the required port.
- **Fix**: Identify the process using the port (e.g., `lsof -i :8080` on Mac/Linux or `netstat -ano | findstr :8080` on Windows) and kill it.

### Error: `429 Too Many Requests`
- **Symptom**: API endpoints suddenly start returning HTTP 429.
- **Cause**: The distributed rate limiter (backed by Redis) has been triggered by too many requests from your IP or User ID.
- **Fix**: Wait for the rate limit window to expire (typically 60 seconds). If testing locally, you can clear Redis:
  ```bash
  docker exec -it redis redis-cli FLUSHALL
  ```

---

## 📬 4. DLQ (Dead Letter Queue) Monitoring

The event-driven architecture uses RabbitMQ. If a message cannot be processed after multiple retries (e.g., due to a bug or a persistent downstream failure), it is routed to a Dead Letter Queue (DLQ).

### How to Monitor DLQs
1. Open the RabbitMQ Management UI at `http://localhost:15672`.
2. Login using the credentials specified in your `.env` file (Default: `admin` / `supersecret_rmq_password`).
3. Navigate to the **Queues** tab.
4. Look for queues ending in `.dlq` (e.g., `payment.events.dlq` or `notification.events.dlq`).
5. If the `Ready` column is greater than 0, there are dead-lettered messages requiring manual intervention.

### How to Inspect and Replay Dead Letters
1. Click on the specific DLQ name in the Management UI.
2. Scroll down to **Get messages** to view the payload and headers of the failed message. The `x-death` header will contain the reason for the failure and the original queue name.
3. **To replay**: If the underlying issue has been fixed, you can move messages back to their original queue using the RabbitMQ Shovel plugin, or via a custom admin API endpoint built into the service.
4. **To discard**: If the messages are invalid and should be dropped, you can purge the DLQ from the RabbitMQ UI.
