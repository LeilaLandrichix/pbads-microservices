import React, { useState, useEffect } from 'react';
import Container from '@mui/material/Container';
import Paper from '@mui/material/Paper';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Alert from '@mui/material/Alert';

// Default credentials - hardcoded for easy access
const DEFAULT_USERNAME = 'admin';
const DEFAULT_PASSWORD = 'admin123';

function Login() {
  const [username, setUsername] = useState(DEFAULT_USERNAME);
  const [password, setPassword] = useState(DEFAULT_PASSWORD);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // Auto-login on component mount
  useEffect(() => {
    // Check if already logged in
    const token = localStorage.getItem('token');
    if (token) {
      navigate('/');
      return;
    }
    
    // Auto-login with default credentials
    autoLogin();
  }, [navigate]);

  const autoLogin = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await axios.post('/api/auth/login', {
        username: DEFAULT_USERNAME,
        password: DEFAULT_PASSWORD,
      });

      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        navigate('/');
      }
    } catch (err) {
      // Don't show error on auto-login, let user try manually
      console.error('Auto-login error:', err);
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await axios.post('/api/auth/login', {
        username,
        password,
      });

      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
        navigate('/');
      }
    } catch (err) {
      let errorMessage = 'Login failed. ';
      
      if (err.response) {
        // Server responded with error status
        const status = err.response.status;
        if (status === 401) {
          errorMessage += 'Credentials not working';
        } else if (status === 503 || status === 502) {
          errorMessage += 'Service unavailable. Please check if Auth Service (port 8081) and API Gateway (port 8080) are running.';
        } else if (status === 429) {
          errorMessage += 'Too many requests. Please wait a moment and try again.';
        } else {
          errorMessage += `Server error (${status}). Please check service logs.`;
        }
      } else if (err.request) {
        // Request made but no response received
        errorMessage += 'No response from server. Please check if API Gateway (port 8080) and Auth Service (port 8081) are running.';
      } else {
        errorMessage += 'Please check your credentials and ensure services are running.';
      }
      
      setError(errorMessage);
      console.error('Login error:', err);
      console.error('Error details:', {
        message: err.message,
        response: err.response?.data,
        status: err.response?.status,
        config: err.config
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper sx={{ p: 4, width: '100%' }}>
          <Typography component="h1" variant="h5" align="center" gutterBottom>
            Sign In
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </Button>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
}

export default Login;

