import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import './Navigation.css';

function Navigation() {
  const location = useLocation();

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
          PBADS
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            color="inherit"
            component={Link}
            to="/"
            className={location.pathname === '/' ? 'active' : ''}
          >
            Dashboard
          </Button>
          <Button
            color="inherit"
            component={Link}
            to="/data"
            className={location.pathname === '/data' ? 'active' : ''}
          >
            Data Entry
          </Button>
          <Button
            color="inherit"
            component={Link}
            to="/alerts"
            className={location.pathname === '/alerts' ? 'active' : ''}
          >
            Alerts
          </Button>
          <Button
            color="inherit"
            component={Link}
            to="/login"
          >
            Login
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
}

export default Navigation;

