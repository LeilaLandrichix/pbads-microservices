# PBADS Services Verification Guide

This guide provides commands and methods to verify that all backend and frontend services are running properly.

## Quick Verification Script

Run the automated verification script:
```bash
scripts\verify-services.bat
```

## Manual Verification Commands

### 1. Check if Services are Running (Port Check)

#### Using PowerShell:
```powershell
# Check Eureka Server
Invoke-WebRequest -Uri "http://localhost:8761" -UseBasicParsing

# Check Config Server
Invoke-WebRequest -Uri "http://localhost:8888/actuator/health" -UseBasicParsing

# Check API Gateway
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing

# Check Auth Service
Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing

# Check Data Service
Invoke-WebRequest -Uri "http://localhost:8082/actuator/health" -UseBasicParsing

# Check Model Service
Invoke-WebRequest -Uri "http://localhost:8083/actuator/health" -UseBasicParsing

# Check Inference Service
Invoke-WebRequest -Uri "http://localhost:8084/actuator/health" -UseBasicParsing

# Check Alert Service
Invoke-WebRequest -Uri "http://localhost:8085/actuator/health" -UseBasicParsing

# Check Monitoring Service
Invoke-WebRequest -Uri "http://localhost:8086/actuator/health" -UseBasicParsing

# Check Frontend
Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing
```

#### Using Command Prompt (netstat):
```cmd
netstat -an | findstr "8761"
netstat -an | findstr "8888"
netstat -an | findstr "8080"
netstat -an | findstr "8081"
netstat -an | findstr "8082"
netstat -an | findstr "8083"
netstat -an | findstr "8084"
netstat -an | findstr "8085"
netstat -an | findstr "8086"
netstat -an | findstr "3000"
```

### 2. Check Eureka Service Registry

Open in browser:
```
http://localhost:8761
```

Or using PowerShell:
```powershell
# Get all registered services
Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Method Get

# Get specific service (e.g., auth-service)
Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps/AUTH-SERVICE" -Method Get
```

### 3. Test API Gateway Routes

#### Health Check via Gateway:
```powershell
# Gateway itself
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health"

# Auth Service via Gateway
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/actuator/health"

# Data Service via Gateway
Invoke-RestMethod -Uri "http://localhost:8080/api/data/actuator/health"
```

#### Test Authentication Endpoint:
```powershell
# Login (replace with actual credentials)
$body = @{
    username = "testuser"
    password = "testpass"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $body -ContentType "application/json"
```

### 4. Check Service Health Endpoints

All Spring Boot services expose health endpoints:

```powershell
# Infrastructure Services
http://localhost:8761/actuator/health  # Eureka (if actuator enabled)
http://localhost:8888/actuator/health  # Config Server
http://localhost:8080/actuator/health  # API Gateway

# Core Services
http://localhost:8081/actuator/health  # Auth Service
http://localhost:8082/actuator/health  # Data Service
http://localhost:8083/actuator/health  # Model Service
http://localhost:8084/actuator/health  # Inference Service
http://localhost:8085/actuator/health  # Alert Service
http://localhost:8086/actuator/health  # Monitoring Service
```

### 5. Frontend Verification

#### Check if Frontend is Running:
```powershell
Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing
```

#### Open in Browser:
```
http://localhost:3000
```

#### Check Frontend Console:
- Open browser Developer Tools (F12)
- Check Console for errors
- Check Network tab for API calls

### 6. Check Service Logs

Each service should show startup logs indicating:
- ✅ Connection to Config Server
- ✅ Registration with Eureka
- ✅ Service started on correct port

Example log messages to look for:
```
- "Started ConfigServerApplication"
- "Started EurekaServerApplication"
- "Started GatewayApplication"
- "DiscoveryClient_XXX/XXX:XXX - registration status: 204"
- "Fetching config from server at: http://localhost:8888"
```

### 7. Verify Database Connections

Check if services can connect to PostgreSQL:
```powershell
# Check PostgreSQL is running
netstat -an | findstr "5432"
```

### 8. Verify Kafka Connection

Check if Kafka is running:
```powershell
# Check Kafka port
netstat -an | findstr "9092"
```

### 9. Verify Redis Connection

Check if Redis is running:
```powershell
# Check Redis port
netstat -an | findstr "6379"
```

## Expected Service Status

### Infrastructure Layer (Start First):
1. ✅ **Eureka Server** - Port 8761 - Service Discovery
2. ✅ **Config Server** - Port 8888 - Configuration Management

### Gateway Layer:
3. ✅ **API Gateway** - Port 8080 - Entry Point

### Core Services (Can start in parallel):
4. ✅ **Auth Service** - Port 8081 - Authentication
5. ✅ **Data Service** - Port 8082 - Data Management
6. ✅ **Model Service** - Port 8083 - ML Models
7. ✅ **Inference Service** - Port 8084 - Anomaly Detection
8. ✅ **Alert Service** - Port 8085 - Notifications
9. ✅ **Monitoring Service** - Port 8086 - Observability

### Frontend:
10. ✅ **React Frontend** - Port 3000 - User Interface

## Troubleshooting

### Service Not Starting:
1. Check if port is already in use: `netstat -an | findstr "PORT_NUMBER"`
2. Check service logs for errors
3. Verify dependencies (PostgreSQL, Kafka, Redis) are running
4. Check Config Server is accessible
5. Verify Eureka Server is running before starting other services

### Service Not Registering with Eureka:
1. Verify Eureka Server is running on port 8761
2. Check service configuration has correct Eureka URL
3. Check network connectivity
4. Verify `@EnableEurekaClient` annotation is present

### API Gateway Not Routing:
1. Verify service is registered in Eureka
2. Check gateway.yml routes configuration
3. Verify service name matches exactly (case-sensitive)
4. Check gateway logs for routing errors

### Frontend Not Connecting:
1. Verify API Gateway is running on port 8080
2. Check browser console for CORS errors
3. Verify proxy configuration in package.json
4. Check network tab for failed API calls

## Quick Health Check Script

Run this PowerShell one-liner to check all services:

```powershell
$services = @(
    @{Name="Eureka Server"; Port=8761; Path="/"},
    @{Name="Config Server"; Port=8888; Path="/actuator/health"},
    @{Name="API Gateway"; Port=8080; Path="/actuator/health"},
    @{Name="Auth Service"; Port=8081; Path="/actuator/health"},
    @{Name="Data Service"; Port=8082; Path="/actuator/health"},
    @{Name="Model Service"; Port=8083; Path="/actuator/health"},
    @{Name="Inference Service"; Port=8084; Path="/actuator/health"},
    @{Name="Alert Service"; Port=8085; Path="/actuator/health"},
    @{Name="Monitoring Service"; Port=8086; Path="/actuator/health"},
    @{Name="Frontend"; Port=3000; Path="/"}
)

foreach ($service in $services) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$($service.Port)$($service.Path)" -UseBasicParsing -TimeoutSec 2
        Write-Host "✓ $($service.Name) - RUNNING" -ForegroundColor Green
    } catch {
        Write-Host "✗ $($service.Name) - NOT RUNNING" -ForegroundColor Red
    }
}
```

## Browser URLs

- **Eureka Dashboard**: http://localhost:8761
- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Config Server**: http://localhost:8888

