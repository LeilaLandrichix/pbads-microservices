@echo off
echo ========================================
echo PBADS Services Verification Script
echo ========================================
echo.

echo Checking Infrastructure Services...
echo.

echo [1] Eureka Server (Port 8761)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8761' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Eureka Server is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Eureka Server is NOT running' -ForegroundColor Red }"
echo.

echo [2] Config Server (Port 8888)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8888/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Config Server is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Config Server is NOT running' -ForegroundColor Red }"
echo.

echo [3] API Gateway (Port 8080)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ API Gateway is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ API Gateway is NOT running' -ForegroundColor Red }"
echo.

echo Checking Core Services...
echo.

echo [4] Auth Service (Port 8081)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8081/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Auth Service is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Auth Service is NOT running' -ForegroundColor Red }"
echo.

echo [5] Data Service (Port 8082)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8082/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Data Service is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Data Service is NOT running' -ForegroundColor Red }"
echo.

echo [6] Model Service (Port 8083)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8083/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Model Service is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Model Service is NOT running' -ForegroundColor Red }"
echo.

echo [7] Inference Service (Port 8084)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8084/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Inference Service is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Inference Service is NOT running' -ForegroundColor Red }"
echo.

echo [8] Alert Service (Port 8085)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8085/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Alert Service is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Alert Service is NOT running' -ForegroundColor Red }"
echo.

echo [9] Monitoring Service (Port 8086)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8086/actuator/health' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Monitoring Service is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Monitoring Service is NOT running' -ForegroundColor Red }"
echo.

echo [10] Frontend (Port 3000)...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:3000' -UseBasicParsing -TimeoutSec 2; Write-Host '  ✓ Frontend is RUNNING' -ForegroundColor Green } catch { Write-Host '  ✗ Frontend is NOT running' -ForegroundColor Red }"
echo.

echo ========================================
echo Verification Complete!
echo ========================================
echo.
echo To view Eureka Dashboard, open: http://localhost:8761
echo To view Frontend, open: http://localhost:3000
echo.
pause

