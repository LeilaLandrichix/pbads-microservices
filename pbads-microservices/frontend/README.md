# PBADS Frontend

React-based frontend application for the Personal Behavior Anomaly Detection System.

## Prerequisites

- Node.js 18+ 
- npm or yarn

## Installation

1. Install dependencies:
```bash
npm install
```

## Running the Application

Start the development server:
```bash
npm start
```

The application will be available at `http://localhost:3000`

## Building for Production

```bash
npm run build
```

This creates an optimized production build in the `build` folder.

## Features

- **Dashboard**: View behavior trends and analytics
- **Data Entry**: Submit daily behavior data (sleep, steps, mood, etc.)
- **Alerts**: View anomaly alerts and notifications
- **Authentication**: Login and user management

## API Integration

The frontend communicates with the backend services through the API Gateway (port 8080). Make sure the following services are running:

- API Gateway (port 8080)
- Auth Service (port 8081)
- Data Service (port 8082)
- Alert Service (port 8085)

## Environment Variables

The application uses a proxy configuration in `package.json` to forward API requests to `http://localhost:8080`. For production, update the API base URL accordingly.

## Project Structure

```
src/
├── components/       # React components
│   ├── Dashboard.js
│   ├── Login.js
│   ├── DataEntry.js
│   ├── Alerts.js
│   └── Navigation.js
├── App.js            # Main application component
├── index.js          # Application entry point
└── index.css         # Global styles
```

