# Login Troubleshooting Guide

## Common Issues and Solutions

### Issue: "Login failed. Please check your credentials."

This error can occur for several reasons. Follow these steps to diagnose:

#### 1. Check Service Status

Verify all required services are running:

```bash
# Check Eureka Dashboard
# Open: http://localhost:8761
# Verify "AUTH-SERVICE" is registered

# Check API Gateway health
curl http://localhost:8080/actuator/health

# Check Auth Service health (direct)
curl http://localhost:8081/actuator/health

# Check Auth Service login endpoint (direct)
curl http://localhost:8081/api/auth/health
```

#### 2. Verify User Creation

The default user should be created automatically when the auth service starts. Check the auth service logs for:

```
========================================
Default user created:
Username: admin
Password: admin123
========================================
```

If you don't see this message:
- The auth service might not have started properly
- The database connection might be failing
- The user might already exist

#### 3. Test Login Endpoint Directly

Test the login endpoint through the API Gateway:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Expected success response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "message": "Login successful"
}
```

If you get an error, check:
- **401 Unauthorized**: Invalid credentials or user doesn't exist
- **503 Service Unavailable**: Auth service not registered with Eureka
- **502 Bad Gateway**: Gateway can't reach auth service
- **Connection refused**: Service not running

#### 4. Check Database Connection

Verify PostgreSQL is running and the database exists:

```bash
# Check if PostgreSQL is running
netstat -an | findstr "5432"

# Connect to PostgreSQL and check users table
psql -U pbads -d pbads_auth
\dt  # List tables
SELECT * FROM users;  # Check if admin user exists
```

#### 5. Check Browser Console

Open browser DevTools (F12) and check:
- **Console tab**: Look for JavaScript errors
- **Network tab**: Check the login request:
  - Status code
  - Response body
  - Request URL (should be `/api/auth/login`)

#### 6. Verify Gateway Configuration

Ensure the gateway route is correct:
- Route path: `/api/auth/**`
- No `StripPrefix=1` (we removed it)
- Service name: `auth-service`
- Eureka registration: `lb://auth-service`

## Manual User Creation

If the default user wasn't created, you can create it manually:

### Option 1: Restart Auth Service
The `DataInitializer` will create the user on startup.

### Option 2: SQL Insert (if database access available)
```sql
INSERT INTO users (username, password, email, first_name, last_name, enabled, created_at, updated_at)
VALUES (
  'admin',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- BCrypt hash of 'admin123'
  'admin@pbads.com',
  'Admin',
  'User',
  true,
  NOW(),
  NOW()
);
```

**Note:** The password hash above is for `admin123`. To generate a new hash, use:
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("admin123");
```

## Quick Fix Checklist

- [ ] Eureka Server is running (port 8761)
- [ ] Config Server is running (port 8888)
- [ ] API Gateway is running (port 8080)
- [ ] Auth Service is running (port 8081)
- [ ] PostgreSQL is running (port 5432)
- [ ] Database `pbads_auth` exists
- [ ] Auth service is registered in Eureka
- [ ] Default user was created (check logs)
- [ ] No Redis required (rate limiter disabled)

## Still Having Issues?

1. **Check all service logs** for error messages
2. **Verify network connectivity** between services
3. **Check firewall settings** if services are on different machines
4. **Restart services in order**:
   - Eureka Server
   - Config Server
   - API Gateway
   - Auth Service

## Testing Without Frontend

You can test authentication directly using curl:

```bash
# 1. Test health endpoint
curl http://localhost:8080/api/auth/health

# 2. Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 3. Use the token (if login succeeds)
TOKEN="your-token-here"
curl http://localhost:8080/api/data/daily/user/1 \
  -H "Authorization: Bearer $TOKEN"
```

