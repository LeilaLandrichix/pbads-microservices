import axios from 'axios';

// Configure default timeout
axios.defaults.timeout = 10000; // 10 seconds

// Request interceptor - add auth token if available
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - handle errors globally
axios.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Enhanced error logging
    if (error.response) {
      // Server responded with error status
      console.error('API Error Response:', {
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data,
        url: error.config?.url,
      });
    } else if (error.request) {
      // Request made but no response received
      console.error('API Error - No Response:', {
        message: error.message,
        url: error.config?.url,
      });
    } else {
      // Error setting up request
      console.error('API Error - Request Setup:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default axios;

