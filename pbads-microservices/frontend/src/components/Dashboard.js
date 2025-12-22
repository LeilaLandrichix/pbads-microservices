import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import axios from 'axios';
import Container from '@mui/material/Container';
import Grid from '@mui/material/Grid';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Alert from '@mui/material/Alert';
import CircularProgress from '@mui/material/CircularProgress';

function Dashboard() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const response = await axios.get('/api/data/daily/user/1', {
        timeout: 10000, // 10 second timeout
      });
      setData(response.data || []);
      setError(null);
    } catch (err) {
      let errorMessage = 'Failed to load dashboard data. ';
      
      if (err.code === 'ECONNABORTED' || err.message?.includes('timeout')) {
        errorMessage += 'Request timed out. Please check if the API Gateway (port 8080) is running.';
      } else if (err.response) {
        // Server responded with error status
        const status = err.response.status;
        if (status === 404) {
          errorMessage += 'No data found for this user.';
        } else if (status === 503 || status === 502) {
          errorMessage += 'Service unavailable. Please check if Data Service (port 8082) and Eureka Server (port 8761) are running.';
        } else {
          errorMessage += `Server error (${status}). Please check service logs.`;
        }
      } else if (err.request) {
        // Request made but no response received
        errorMessage += 'No response from server. Please check if API Gateway (port 8080) and Data Service (port 8082) are running.';
      } else {
        errorMessage += 'Please check if services are running.';
      }
      
      setError(errorMessage);
      console.error('Dashboard error:', err);
    } finally {
      setLoading(false);
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
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Dashboard
      </Typography>

      {error && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Behavior Trends
            </Typography>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={data.length > 0 ? data : []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="sleepHours" stroke="#8884d8" name="Sleep Hours" />
                <Line type="monotone" dataKey="steps" stroke="#82ca9d" name="Steps" />
                <Line type="monotone" dataKey="moodScore" stroke="#ffc658" name="Mood Score" />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Recent Activity
            </Typography>
            <Typography variant="body2" color="text.secondary">
              View your recent behavior data entries and anomaly detections.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Anomaly Alerts
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Check for detected anomalies in your behavior patterns.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6" gutterBottom>
              Recommendations
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Get personalized recommendations based on your data.
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
}

export default Dashboard;

