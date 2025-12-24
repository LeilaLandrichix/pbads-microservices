# Recommendation Service

Microservice for generating personalized recommendations using Google Gemini AI.

## Setup

1. **Get Gemini API Key:**
   - Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Create a new API key
   - Copy the API key

2. **Configure Environment Variable:**
   - Create a `.env` file in the root directory (or set environment variable)
   - Add: `GEMINI_API_KEY=your_api_key_here`
   - Or set it as an environment variable: `export GEMINI_API_KEY=your_api_key_here`

3. **Run the Service:**
   ```bash
   mvn spring-boot:run
   ```

## Endpoints

- `GET /api/recommendations/user/{userId}?dataSummary=...` - Get recommendation for a user
- `POST /api/recommendations/generate` - Generate recommendation with full request body

## Port

Default port: **8087**

## Configuration

The service reads the Gemini API key from:
1. Environment variable `GEMINI_API_KEY` (preferred)
2. Application property `gemini.api.key`

## Dependencies

- Spring Boot 3.4.12
- Google Gemini Java SDK
- Spring Cloud Netflix Eureka (for service discovery)

