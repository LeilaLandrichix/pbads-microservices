@echo off
echo ========================================
echo Stopping PBADS Services
echo ========================================
echo.

echo Stopping Java processes (Spring Boot services)...
taskkill /F /IM java.exe 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Java processes stopped.
) else (
    echo No Java processes found or already stopped.
)

echo.
echo Stopping Node.js processes (Frontend)...
taskkill /F /IM node.exe 2>nul
if %ERRORLEVEL% EQU 0 (
    echo Node.js processes stopped.
) else (
    echo No Node.js processes found or already stopped.
)

echo.
echo ========================================
echo All services stopped!
echo ========================================
echo.
pause

