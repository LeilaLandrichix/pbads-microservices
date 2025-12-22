@echo off
echo ========================================
echo PBADS API Endpoints Test Script
echo ========================================
echo.

echo Testing API Gateway Endpoints...
echo.

echo [1] Testing Gateway Health...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get; Write-Host '  ✓ Gateway Health:' $response.status -ForegroundColor Green } catch { Write-Host '  ✗ Gateway not responding' -ForegroundColor Red }"
echo.

echo [2] Testing Auth Service via Gateway (GET /api/auth/health)...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/actuator/health' -Method Get -ErrorAction SilentlyContinue; Write-Host '  ✓ Auth Service reachable via Gateway' -ForegroundColor Green } catch { Write-Host '  ✗ Auth Service not reachable via Gateway' -ForegroundColor Yellow }"
echo.

echo [3] Testing Data Service via Gateway (GET /api/data/actuator/health)...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/data/actuator/health' -Method Get -ErrorAction SilentlyContinue; Write-Host '  ✓ Data Service reachable via Gateway' -ForegroundColor Green } catch { Write-Host '  ✗ Data Service not reachable via Gateway' -ForegroundColor Yellow }"
echo.

echo [4] Testing Eureka Service Discovery...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8761/eureka/apps' -Method Get; Write-Host '  ✓ Eureka Service Discovery is working' -ForegroundColor Green; Write-Host '  Registered services:' -ForegroundColor Cyan; $response.applications.application | ForEach-Object { Write-Host ('    - ' + $_.name + ' (' + $_.instance.Count + ' instance(s))') } } catch { Write-Host '  ✗ Cannot access Eureka' -ForegroundColor Red }"
echo.

echo ========================================
echo API Testing Complete!
echo ========================================
echo.
pause

