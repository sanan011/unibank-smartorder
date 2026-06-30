# Unibank SmartOrder

Unibank SmartOrder is a modern, scalable, and robust order management system built with a microservices architecture. It demonstrates best practices in backend development, distributed systems, and modern frontend design, making it a comprehensive full-stack portfolio project.

## 🏗️ Architecture Overview

The system is designed around three core microservices communicating asynchronously via RabbitMQ:

- **Order Service**: Handles order creation, processing, and management.
- **Payment Service**: Processes payment transactions securely and reliably.
- **Notification Service**: Manages email and system notifications for users.

## ✨ New Features

- **Email Verification**: Secure user onboarding with verification tokens.
- **Password Reset**: Automated password recovery flow using Gmail SMTP.
- **DLQ Admin API**: Dead Letter Queue (DLQ) administration for monitoring and managing failed messages in RabbitMQ.

## 💻 Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.3**
- **PostgreSQL** (Relational Data)
- **MongoDB** (Document Storage)
- **Redis** (Caching)
- **RabbitMQ** (Message Broker)
- **Docker** (Containerization)

### Frontend
- **React**
- **TypeScript**
- **Tailwind CSS**

## 🚀 How to Run Locally

Follow these steps to run the application locally on your machine:

1. **Set up environment variables:**
   ```bash
   cp deployment/.env.example deployment/.env
   ```
2. **Configure SMTP credentials:**
   Open `deployment/.env` and add your Gmail credentials for the Notification Service:
   ```env
   GMAIL_USERNAME=your_email@gmail.com
   GMAIL_APP_PASSWORD=your_app_password
   ```
3. **Start the backend infrastructure:**
   ```bash
   docker compose up -d
   ```
4. **Start the frontend application:**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## 🔌 API Endpoints Overview

The backend exposes several key API domains:

- **Authentication (`/api/auth/**`)**: Registration, login, email verification, and password reset.
- **Products (`/api/products/**`)**: Catalog management and product retrieval.
- **Orders (`/api/orders/**`)**: Order placement, tracking, and history.
- **DLQ Admin (`/api/dlq/**`)**: Administrative endpoints for managing RabbitMQ Dead Letter Queues.

## 📊 Monitoring URLs

Once the infrastructure is running, you can access the following monitoring and management tools:

- **Grafana**: [http://localhost:3000](http://localhost:3000) (Metrics Dashboard)
- **Prometheus**: [http://localhost:9090](http://localhost:9090) (Time-series Database)
- **RabbitMQ Management UI**: [http://localhost:15672](http://localhost:15672) (Message Broker Admin)
- **MailHog**: [http://localhost:8025](http://localhost:8025) (Local Email Testing)

## 🔄 CI/CD Pipeline

The project utilizes GitHub Actions for continuous integration and continuous deployment, ensuring high code quality and security:

- **Build & Test (`build-test`)**: Compiles code and runs all automated tests.
- **Code Quality (`code-quality`)**: Static analysis and linting.
- **Security Scan (`security-scan`)**: Dependency scanning and vulnerability checks.

## 🧪 Testing

Quality assurance is a primary focus of this project:

- **Unit & Integration Tests**: Comprehensive coverage across all microservices using JUnit and Testcontainers.
- **Performance Tests**: Load and stress testing implemented with **k6** to ensure system stability under heavy traffic.
