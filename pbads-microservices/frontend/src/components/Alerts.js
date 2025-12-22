import React, { useState, useEffect } from 'react';
import Container from '@mui/material/Container';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Alert from '@mui/material/Alert';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
import Chip from '@mui/material/Chip';
import CircularProgress from '@mui/material/CircularProgress';
import axios from 'axios';

function Alerts() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchAlerts();
  }, []);

  const fetchAlerts = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/alerts', {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('token')}`,
        },
        timeout: 10000, // 10 second timeout
      });
      setAlerts(response.data || []);
      setError(null);
    } catch (err) {
      let errorMessage = 'Failed to load alerts. ';
      
      if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
        errorMessage += 'Request timed out. Please check if the API Gateway (port 8080) is running.';
      } else if (err.response) {
        // Server responded with error status
        const status = err.response.status;
        if (status === 404) {
          errorMessage += 'Alert service endpoint not found.';
        } else if (status === 503 || status === 502) {
          errorMessage += 'Service unavailable. Please check if Alert Service (port 8085) and Eureka Server (port 8761) are running.';
        } else {
          errorMessage += `Server error (${status}). Please check service logs.`;
        }
      } else if (err.request) {
        // Request made but no response received
        errorMessage += 'No response from server. Please check if API Gateway (port 8080) and Alert Service (port 8085) are running.';
      } else {
        errorMessage += 'Please check if services are running.';
      }
      
      setError(errorMessage);
      console.error('Alerts error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getSeverityColor = (severity) => {
    switch (severity?.toLowerCase()) {
      case 'high':
        return 'error';
      case 'medium':
        return 'warning';
      case 'low':
        return 'info';
      default:
        return 'info';
    }
  };

  if (loading) {
    return (
      <Container>
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Alerts & Notifications
      </Typography>

      {error && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Paper sx={{ p: 3 }}>
        {alerts.length === 0 ? (
          <Alert severity="info">
            No alerts at this time. Your behavior patterns are normal.
          </Alert>
        ) : (
          <List>
            {alerts.map((alert, index) => (
              <ListItem key={index} divider>
                <ListItemText
                  primary={alert.message || 'Anomaly detected'}
                  secondary={alert.timestamp || new Date().toLocaleString()}
                />
                {alert.severity && (
                  <Chip
                    label={alert.severity}
                    color={getSeverityColor(alert.severity)}
                    size="small"
                  />
                )}
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Container>
  );
}

export default Alerts;

