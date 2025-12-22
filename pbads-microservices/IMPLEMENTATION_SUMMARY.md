# PBADS Microservices Implementation Summary

## Overview
This document summarizes the implementation of the Personal Behavior Anomaly Detection System (PBADS) microservices architecture.

## Completed Components

### 1. Configuration Files ✅
All microservices are now configured to interact with each other:

- **Config Server** (`config-repo/`): Centralized configuration for all services
- **Eureka Server**: Service discovery configuration
- **API Gateway**: Routes and filters for all services
- **Auth Service**: Database and JWT configuration
- **Data Service**: Database, Kafka producer configuration
- **Model Service**: MLflow and model storage configuration
- **Inference Service**: Kafka consumer and model cache configuration
- **Alert Service**: Database, Kafka consumer, and notification configuration

### 2. Data Service Implementation ✅

#### Entity
- **DailyData**: Complete entity with all fields:
  - User_ID, Date, Wake_Up_Time, Sleep_Hours
  - Steps, Calories_Burned, Water_Intake_ml
  - Study_Hours, Mood_Score
  - Timestamps (created_at, updated_at)

#### DTO
- **DailyDataDTO**: Validation annotations and all required fields

#### Repository
- **DailyDataRepository**: JPA repository with custom queries:
  - Find by user ID
  - Find by user and date
  - Find by date range
  - Count and existence checks

#### Service
- **DailyDataService**: Business logic for:
  - Create/Update daily data
  - Retrieve data by various criteria
  - Publish Kafka events when data is logged

#### Controller
- **DailyDataController**: REST endpoints:
  - POST `/api/data/daily` - Create daily data
  - GET `/api/data/daily/{id}` - Get by ID
  - GET `/api/data/daily/user/{userId}` - Get all for user
  - GET `/api/data/daily/user/{userId}/date/{date}` - Get specific date
  - GET `/api/data/daily/user/{userId}/range` - Get date range
  - PUT `/api/data/daily/{id}` - Update data
  - DELETE `/api/data/daily/{id}` - Delete data

#### Kafka Integration
- **DataEventProducer**: Publishes `DataLoggedEvent` to Kafka topic `data.logged.events`
- **DataLoggedEvent**: Event model with all habit data fields

### 3. Inference Service Implementation ✅

#### Kafka Consumer
- **DataEventConsumer**: Consumes `DataLoggedEvent` from Kafka
- Processes events for anomaly detection

#### Services
- **InferenceService**: Main service orchestrating anomaly detection
- **AnomalyDetectorService**: Core anomaly detection logic:
  - Uses ML model if available
  - Falls back to rule-based detection
  - Determines anomaly levels (NORMAL, LOW, MEDIUM, HIGH)
  
- **ModelLoaderService**: Loads and caches ML models (placeholder for production)
- **RuleBasedService**: Rule-based anomaly detection with thresholds:
  - Sleep hours: 6-10 hours normal
  - Steps: Minimum 5000 steps
  - Water intake: Minimum 1500ml
  - Mood score: Minimum 3/10
  - Study hours: Maximum 12 hours

#### DTOs
- **AnomalyResult**: Contains:
  - Anomaly flag
  - Anomaly score (0-1)
  - Anomaly level
  - Contributing factors
  - Message

#### Kafka Producer
- **AnomalyEventProducer**: Publishes anomaly events to `anomaly.detected.events` topic

### 4. Python Training Scripts ✅

#### train_model.py
- **Location**: `model-service/src/main/resources/ml/scripts/train_model.py`
- **Features**:
  - Loads data from CSV file
  - Supports Isolation Forest and One-Class SVM algorithms
  - Feature preprocessing and scaling
  - Model evaluation metrics
  - Saves trained model and metadata
  - Configurable hyperparameters via environment variables

**Usage**:
```bash
# Train Isolation Forest
python train_model.py

# Train One-Class SVM
ALGORITHM=one-class-svm python train_model.py

# Custom hyperparameters
CONTAMINATION=0.15 N_ESTIMATORS=200 python train_model.py
```

#### evaluate_model.py
- **Location**: `model-service/src/main/resources/ml/scripts/evaluate_model.py`
- **Features**:
  - Loads trained model
  - Evaluates on test data
  - Provides detailed metrics
  - Shows top anomalies

**Usage**:
```bash
MODEL_PATH=./models/trained/isolation_forest_20250101_120000.joblib python evaluate_model.py
```

### 5. Application Configuration ✅

All services now have `application.yml` files that:
- Connect to Config Server
- Register with Eureka
- Configure service-specific settings
- Use appropriate ports (8081-8086)

## Data Flow

1. **Data Entry**: User submits daily habit data via Data Service API
2. **Data Storage**: Data is persisted in PostgreSQL
3. **Event Publishing**: Data Service publishes `DataLoggedEvent` to Kafka
4. **Event Consumption**: Inference Service consumes the event
5. **Anomaly Detection**: 
   - Attempts ML model prediction
   - Falls back to rule-based detection
6. **Alert Generation**: If anomaly detected, publishes to Alert Service
7. **Notification**: Alert Service sends notifications via WebSocket/Email

## Service Ports

- Eureka Server: 8761
- Config Server: 8888
- API Gateway: 8080
- Auth Service: 8081
- Data Service: 8082
- Model Service: 8083
- Inference Service: 8084
- Alert Service: 8085
- Monitoring Service: 8086

## Kafka Topics

- `data.logged.events`: Published by Data Service when new data is logged
- `anomaly.detected.events`: Published by Inference Service when anomaly is detected

## Next Steps (Optional Enhancements)

1. **Auth Service**: Implement User entity, JWT service, and authentication endpoints
2. **Alert Service**: Implement WebSocket handlers and notification services
3. **Model Service**: Implement model training endpoints and MLflow integration
4. **Gateway Filters**: Implement authentication filter and rate limiting
5. **Database Migrations**: Create Flyway migration scripts for all services
6. **Model Integration**: Connect Python models to Java inference service
7. **Frontend**: Connect React frontend to backend APIs

## Running the System

1. **Start Infrastructure**:
   ```bash
   docker-compose -f docker-compose-infra.yml up -d
   ```

2. **Start Services** (in order):
   - Eureka Server
   - Config Server
   - API Gateway
   - Auth Service
   - Data Service
   - Model Service
   - Inference Service
   - Alert Service

3. **Train Model**:
   ```bash
   cd model-service/src/main/resources/ml/scripts
   python train_model.py
   ```

4. **Test Data Service**:
   ```bash
   curl -X POST http://localhost:8080/api/data/daily \
     -H "Content-Type: application/json" \
     -d '{
       "userId": "U001",
       "date": "2025-01-17",
       "wakeUpTime": "07:02:00",
       "sleepHours": 5.8,
       "steps": 19772,
       "caloriesBurned": 2645,
       "waterIntakeMl": 571,
       "studyHours": 0.4,
       "moodScore": 2
     }'
   ```

## Notes

- The CSV data path is hardcoded in the Python scripts. Update it in production.
- Model loading in Inference Service is a placeholder - needs actual model integration.
- Auth Service components are stubs - needs full implementation.
- Alert Service WebSocket needs implementation.
- All services use Config Server for centralized configuration.

