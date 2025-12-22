@echo off
echo ========================================
echo Starting PBADS Backend Services
echo ========================================
echo.

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven (mvn) is not found in PATH
    echo Please install Maven or add it to your PATH
    pause
    exit /b 1
)

echo Maven found. Starting services...
echo.
echo IMPORTANT: Services must be started in order:
echo   1. Eureka Server (8761)
echo   2. Config Server (8888)
echo   3. API Gateway (8080)
echo   4. Core Services (8081-8086)
echo.
echo Each service will open in a new window.
echo.
pause

REM Change to project root directory
cd /d "%~dp0.."

echo [1/9] Starting Eureka Server...
start "Eureka Server - Port 8761" cmd /k "cd service-discovery\eureka-server && mvn spring-boot:run"

echo Waiting for Eureka Server to start...
timeout /t 15 /nobreak >nul

echo [2/9] Starting Config Server...
start "Config Server - Port 8888" cmd /k "cd configuration\config-server && mvn spring-boot:run"

echo Waiting for Config Server to start...
timeout /t 15 /nobreak >nul

echo [3/9] Starting API Gateway...
start "API Gateway - Port 8080" cmd /k "cd gateway\api-gateway && mvn spring-boot:run"

echo Waiting for API Gateway to start...
timeout /t 10 /nobreak >nul

echo [4/9] Starting Auth Service...
start "Auth Service - Port 8081" cmd /k "cd core-services\auth-service && mvn spring-boot:run"

echo [5/9] Starting Data Service...
start "Data Service - Port 8082" cmd /k "cd core-services\data-service && mvn spring-boot:run"

echo [6/9] Starting Model Service...
start "Model Service - Port 8083" cmd /k "cd core-services\model-service && mvn spring-boot:run"

echo [7/9] Starting Inference Service...
start "Inference Service - Port 8084" cmd /k "cd core-services\inference-service && mvn spring-boot:run"

echo [8/9] Starting Alert Service...
start "Alert Service - Port 8085" cmd /k "cd core-services\alert-service && mvn spring-boot:run"

echo [9/9] Starting Monitoring Service...
start "Monitoring Service - Port 8086" cmd /k "cd observability\monitoring-service && mvn spring-boot:run"

echo.
echo ========================================
echo All backend services are starting!
echo ========================================
echo.
echo Services will be available at:
echo   - Eureka Dashboard: http://localhost:8761
echo   - Config Server: http://localhost:8888
echo   - API Gateway: http://localhost:8080
echo   - Auth Service: http://localhost:8081
echo   - Data Service: http://localhost:8082
echo   - Model Service: http://localhost:8083
echo   - Inference Service: http://localhost:8084
echo   - Alert Service: http://localhost:8085
echo   - Monitoring Service: http://localhost:8086
echo.
echo Wait for all services to fully start before using them.
echo Check Eureka Dashboard to verify all services are registered.
echo.
pause

