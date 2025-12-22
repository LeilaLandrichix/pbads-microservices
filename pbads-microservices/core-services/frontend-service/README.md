# PBADS Frontend Service

Spring Boot-based frontend service using Thymeleaf templates for server-side rendering.

## Features

- **Server-Side Rendering**: Uses Thymeleaf templates instead of React
- **Session Management**: HTTP sessions for authentication
- **Auto-Login**: Automatically logs in with default credentials (admin/admin123)
- **Dashboard**: Displays behavior trends with Chart.js
- **Data Entry**: Form for submitting daily behavior data
- **Alerts**: View anomaly alerts and notifications

## Technology Stack

- Spring Boot 3.4.12
- Thymeleaf (Template Engine)
- Spring Cloud Netflix Eureka (Service Discovery)
- RestTemplate (API Client)
- Chart.js (Client-side charts)

## Running the Service

```bash
cd pbads-microservices/core-services/frontend-service
mvn spring-boot:run
```

The service will start on port **3000** (same as the React frontend).

## Access

- **Frontend**: http://localhost:3000
- **Login**: http://localhost:3000/login (auto-login enabled)

## Default Credentials

- Username: `admin`
- Password: `admin123`

## API Integration

The frontend service communicates with backend services through the API Gateway (port 8080):
- Auth Service: `/api/auth/login`
- Data Service: `/api/data/daily/**`
- Alert Service: `/api/alerts`

## Pages

- `/` - Dashboard (requires authentication)
- `/login` - Login page (auto-submits with default credentials)
- `/data` - Data Entry form (requires authentication)
- `/alerts` - Alerts & Notifications (requires authentication)
- `/logout` - Logout and redirect to login

## Configuration

Configuration is managed through:
- `application.yml` (local)
- Config Server (if available)

Key configuration:
- `server.port`: 3000
- `api.gateway.url`: http://localhost:8080

