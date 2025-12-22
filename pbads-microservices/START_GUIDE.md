# PBADS Startup Guide

This guide explains how to start all backend and frontend services.

## Prerequisites

Before starting, ensure you have:

1. **JDK 17+** installed and configured
2. **Maven** installed and in PATH
3. **Node.js 18+** installed and in PATH
4. **PostgreSQL** running (if services use databases)
5. **Kafka** running (if services use messaging)
6. **Redis** running (if services use caching)

## Quick Start

### Option 1: Start Everything at Once
```bash
scripts\start-all.bat
```

This will start all backend services and frontend in separate windows.

### Option 2: Start Separately

#### Start Backend Services:
```bash
scripts\start-backend.bat
```

#### Start Frontend:
```bash
scripts\start-frontend.bat
```

## Manual Startup (Step by Step)

If you prefer to start services manually or troubleshoot:

### 1. Start Infrastructure Services First

**Eureka Server** (Port 8761):
```bash
cd service-discovery\eureka-server
mvn spring-boot:run
```

Wait ~15 seconds, then start:

**Config Server** (Port 8888):
```bash
cd configuration\config-server
mvn spring-boot:run
```

Wait ~15 seconds, then start:

**API Gateway** (Port 8080):
```bash
cd gateway\api-gateway
mvn spring-boot:run
```

### 2. Start Core Services

These can be started in parallel (each in a separate terminal):

**Auth Service** (Port 8081):
```bash
cd core-services\auth-service
mvn spring-boot:run
```

**Data Service** (Port 8082):
```bash
cd core-services\data-service
mvn spring-boot:run
```

**Model Service** (Port 8083):
```bash
cd core-services\model-service
mvn spring-boot:run
```

**Inference Service** (Port 8084):
```bash
cd core-services\inference-service
mvn spring-boot:run
```

**Alert Service** (Port 8085):
```bash
cd core-services\alert-service
mvn spring-boot:run
```

**Monitoring Service** (Port 8086):
```bash
cd observability\monitoring-service
mvn spring-boot:run
```

### 3. Start Frontend

```bash
cd frontend
npm install  # First time only
npm start
```

## Service Startup Order

**Critical Order:**
1. ✅ **Eureka Server** - Must start first (Service Discovery)
2. ✅ **Config Server** - Must start second (Configuration)
3. ✅ **API Gateway** - Can start after Eureka and Config
4. ✅ **Core Services** - Can start in parallel after Gateway

**Frontend:**
- Can start anytime, but backend should be running for full functionality

## Verification

After starting services, verify they're running:

```bash
# Quick verification
scripts\verify-services.bat

# Test API endpoints
scripts\test-api-endpoints.bat
```

Or check manually:
- **Eureka Dashboard**: http://localhost:8761
- **Frontend**: http://localhost:3000

## Stopping Services

### Stop All Services:
```bash
scripts\stop-all.bat
```

### Stop Individual Services:
- Press `Ctrl+C` in each service window
- Or use Task Manager to kill Java/Node processes

## Troubleshooting

### Service Won't Start

1. **Check Port Availability:**
   ```bash
   netstat -an | findstr "PORT_NUMBER"
   ```
   If port is in use, stop the process using it or change the port in `application.yml`

2. **Check Maven Installation:**
   ```bash
   mvn --version
   ```

3. **Check Java Version:**
   ```bash
   java -version
   ```
   Should be JDK 17 or higher

4. **Check Service Logs:**
   Look at the console output for each service to see error messages

### Service Not Registering with Eureka

1. Verify Eureka Server is running: http://localhost:8761
2. Check service configuration has correct Eureka URL
3. Wait a few seconds - registration takes time
4. Check service logs for registration errors

### Frontend Not Connecting to Backend

1. Verify API Gateway is running: http://localhost:8080
2. Check browser console (F12) for errors
3. Verify proxy in `frontend/package.json` points to `http://localhost:8080`
4. Check CORS configuration in gateway

### Database Connection Errors

1. Verify PostgreSQL is running:
   ```bash
   netstat -an | findstr "5432"
   ```

2. Check database credentials in `config-repo/*-service.yml`
3. Ensure databases are created:
   - `pbads_auth`
   - `pbads_data`
   - `pbads_model`
   - `pbads_alert`
   - `pbads_monitoring`

### Kafka Connection Errors

1. Verify Kafka is running:
   ```bash
   netstat -an | findstr "9092"
   ```

2. Check Kafka bootstrap servers in service configs
3. Ensure Kafka topics are created (if needed)

## Development Tips

### Running in IDE

You can also run services directly from your IDE (Eclipse, IntelliJ, VS Code):

1. Open each service project
2. Run the main application class:
   - `EurekaServerApplication.java`
   - `ConfigServerApplication.java`
   - `GatewayApplication.java`
   - etc.

### Building Services

To build all services:
```bash
# Build individual service
cd service-discovery\eureka-server
mvn clean install

# Or build from root (if parent POM exists)
cd pbads-microservices
mvn clean install
```

### Hot Reload

Spring Boot DevTools enables hot reload. Changes to Java files will automatically restart the service.

## Service URLs Reference

| Service | Port | URL |
|---------|------|-----|
| Eureka Server | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| API Gateway | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |
| Data Service | 8082 | http://localhost:8082 |
| Model Service | 8083 | http://localhost:8083 |
| Inference Service | 8084 | http://localhost:8084 |
| Alert Service | 8085 | http://localhost:8085 |
| Monitoring Service | 8086 | http://localhost:8086 |
| Frontend | 3000 | http://localhost:3000 |

## Next Steps

After all services are running:

1. ✅ Verify services: `scripts\verify-services.bat`
2. ✅ Check Eureka Dashboard: http://localhost:8761
3. ✅ Test API Gateway: http://localhost:8080/actuator/health
4. ✅ Open Frontend: http://localhost:3000
5. ✅ Test authentication and data entry

