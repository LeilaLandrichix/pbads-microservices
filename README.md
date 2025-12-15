# Personal Behavior Anomaly Detection System (PBADS)

A comprehensive microservices architecture for detecting anomalies in personal daily behaviors using machine learning.

## Ì≥ã Project Overview

PBADS is a full-stack system that analyzes personal habit data (sleep, diet, activity, mood) to detect behavioral anomalies and provide insights. The system uses a 12-service microservices architecture with real-time inference capabilities.

## ÌøóÔ∏è Architecture

The system follows a modular microservices architecture with the following layers:

### **Infrastructure Layer**
- **Eureka Server** (8761) - Service discovery and registry
- **Config Server** (8888) - Centralized configuration management
- **API Gateway** (8080) - Single entry point with routing, authentication, and rate limiting
- **Kafka** - Asynchronous message broker
- **PostgreSQL** - Relational databases (one per service)
- **Redis** - Caching and session management

### **Core Services Layer**
- **Auth Service** (8081) - JWT/OAuth2 authentication & user management
- **Data Service** (8082) - Habits data CRUD operations and validation
- **Model Service** (8083) - ML model training and management (Java/Python hybrid)
- **Inference Service** (8084) - Real-time anomaly detection
- **Alert Service** (8085) - Notification management with WebSocket support
- **Monitoring Service** (8086) - Observability with Prometheus/Grafana

### **Supporting Components**
- **Frontend** (3000) - React application with real-time dashboards
- **Model Registry** (MLflow on 5000) - ML experiment tracking and model versioning
- **Shared Libraries** - Common DTOs, exceptions, and utilities

## Ì∫Ä Quick Start

### Prerequisites
- Docker & Docker Compose
- JDK 17+
- Python 3.9+ (for ML components)
- Node.js 18+ (for frontend)

### Installation & Running

```bash
# 1. Clone the repository
git clone <repository-url>
cd pbads-microservices

# 2. Set up environment variables
cp .env.example .env
# Edit .env with your configuration

# 3. Start infrastructure services
docker-compose -f docker-compose-infra.yml up -d

# 4. Build and start core services
make build-all
make start-all

# 5. Start frontend
cd frontend
npm install
npm start
