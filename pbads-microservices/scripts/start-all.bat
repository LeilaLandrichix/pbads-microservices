@echo off
echo ========================================
echo Starting PBADS - Complete System
echo ========================================
echo.
echo This script will start:
echo   1. All Backend Services (9 services)
echo   2. Frontend Application
echo.
echo Make sure you have:
echo   - JDK 17+ installed
echo   - Maven installed and in PATH
echo   - Node.js 18+ installed and in PATH
echo   - PostgreSQL, Kafka, and Redis running (if needed)
echo.
pause

REM Start backend services
echo.
echo Starting Backend Services...
call "%~dp0start-backend.bat"

REM Wait a bit for backend to initialize
echo.
echo Waiting for backend services to initialize...
timeout /t 30 /nobreak >nul

REM Start frontend
echo.
echo Starting Frontend...
start "Frontend - Port 3000" cmd /k "%~dp0start-frontend.bat"

echo.
echo ========================================
echo All services are starting!
echo ========================================
echo.
echo Backend Services:
echo   - Eureka Dashboard: http://localhost:8761
echo   - API Gateway: http://localhost:8080
echo.
echo Frontend:
echo   - React App: http://localhost:3000
echo.
echo Check the service windows for startup status.
echo Use scripts\verify-services.bat to check if all services are running.
echo.
pause

