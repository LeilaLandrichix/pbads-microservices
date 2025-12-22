# Login Credentials

## Default User Credentials

The system has been configured with a default user that is automatically created when the auth service starts.

### Credentials:
- **Username:** `admin`
- **Password:** `admin123`
- **Email:** `admin@pbads.com`

## Auto-Login Feature

The frontend Login component has been configured to automatically log in with these credentials when you visit the login page. You don't need to manually enter them - the system will automatically authenticate you.

## Manual Login

If you prefer to log in manually, you can:
1. Navigate to the login page
2. The form will be pre-filled with the default credentials
3. Click "Sign In" or wait for auto-login

## Creating Additional Users

Currently, the system only supports the default admin user. To add more users, you would need to:
1. Implement a registration endpoint in the AuthController
2. Add a registration form in the frontend
3. Or manually insert users into the database

## Security Note

⚠️ **Important:** These are default credentials for development purposes only. In production:
- Change the default password
- Implement proper user registration
- Add password complexity requirements
- Enable account lockout after failed attempts
- Use stronger JWT secrets

## Troubleshooting

If auto-login fails:
1. Check that the Auth Service is running (port 8081)
2. Check that the API Gateway is running (port 8080)
3. Check that Eureka Server is running (port 8761)
4. Check browser console for error messages
5. Verify the database connection (PostgreSQL on port 5432)

## Testing Login

You can test the login endpoint directly:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Expected response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "message": "Login successful"
}
```

