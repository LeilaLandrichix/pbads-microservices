# Dashboard Error Fix

## Issue
The dashboard was showing the error: "Failed to load dashboard data. Please check if services are running."

## Root Cause
The API Gateway was configured with `StripPrefix=1` for the data-service route, which stripped the `/api` prefix from requests. However, the data-service controller expects the full `/api/data` path.

**Example:**
- Frontend requests: `/api/data/daily/user/1`
- Gateway with StripPrefix=1 forwards: `/data/daily/user/1` ❌
- Controller expects: `/api/data/daily/user/1` ❌
- Result: 404 Not Found

## Solution

### 1. Fixed Gateway Configuration
**File:** `config-repo/gateway.yml`

Removed `StripPrefix=1` from the data-service route filter. Now the full path `/api/data/daily/user/1` is forwarded to the data-service, matching what the controller expects.

**Before:**
```yaml
filters:
  - StripPrefix=1
  - name: CircuitBreaker
```

**After:**
```yaml
filters:
  - name: CircuitBreaker
```

### 2. Enhanced Error Handling
**File:** `frontend/src/components/Dashboard.js`

Added detailed error messages that help diagnose the issue:
- Timeout errors → Check API Gateway
- 503/502 errors → Check Data Service and Eureka
- Connection errors → Check service availability
- 404 errors → No data found

### 3. Global Axios Configuration
**File:** `frontend/src/config/axiosConfig.js`

Created a centralized axios configuration with:
- Default 10-second timeout
- Automatic token injection from localStorage
- Enhanced error logging for debugging

## Verification Steps

1. **Restart API Gateway** to pick up the new configuration:
   ```bash
   # Stop the gateway (Ctrl+C)
   # Then restart:
   cd gateway/api-gateway
   mvn spring-boot:run
   ```

2. **Verify services are running:**
   ```bash
   scripts\verify-services.bat
   ```

3. **Check Eureka Dashboard:**
   - Open http://localhost:8761
   - Verify `data-service` is registered

4. **Test the API endpoint directly:**
   ```bash
   curl http://localhost:8080/api/data/daily/user/1
   ```

5. **Check browser console:**
   - Open browser DevTools (F12)
   - Check Network tab for API requests
   - Check Console for error messages

## Required Services

For the dashboard to work, ensure these services are running:

1. ✅ **Eureka Server** (port 8761) - Service discovery
2. ✅ **Config Server** (port 8888) - Configuration management
3. ✅ **API Gateway** (port 8080) - Request routing
4. ✅ **Data Service** (port 8082) - Data retrieval
5. ✅ **PostgreSQL** (port 5432) - Database
6. ✅ **Frontend** (port 3000) - React app

## Additional Notes

- The gateway configuration change requires a restart of the API Gateway service
- Other services (auth, alerts, etc.) still use `StripPrefix=1` because their controllers don't include `/api` in their path mappings
- If you still see errors after restarting, check:
  - Service logs for detailed error messages
  - Eureka dashboard to verify service registration
  - Database connectivity
  - Network connectivity between services

