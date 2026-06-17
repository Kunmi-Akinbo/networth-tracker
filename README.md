# Net Worth Tracker

A full-stack personal finance application for tracking assets, liabilities, and net worth over time.

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.x with Kotlin
- **Database**: PostgreSQL
- **Cache**: Redis
- **Authentication**: JWT with Spring Security
- **Build Tool**: Gradle

### Frontend
- **Framework**: React Native with Expo
- **State Management**: Zustand
- **Charts**: Victory Native

### DevOps
- **Containerization**: Docker + Docker Compose
- **CI/CD**: GitHub Actions
- **Deployment**: Fly.io (backend)

## Features

- User authentication (register, login, JWT refresh)
- Asset and liability account management
- Manual balance tracking
- CSV import for bank statements
- Scheduled daily net worth snapshots
- Net worth trend analytics
- Savings goals with progress tracking
- Redis caching for performance

## Project Structure

```
networth-tracker/
├── backend/          # Spring Boot + Kotlin API
├── frontend/         # React Native + Expo app
├── docker/           # Docker configurations
└── README.md
```

## Getting Started

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL
- Redis

### Local Development

1. Clone the repository
2. Start services with Docker Compose:
   ```bash
   docker-compose up -d
   ```
3. Run backend:
   ```bash
   cd backend
   ./gradlew bootRun
   ```
4. Run frontend:
   ```bash
   cd frontend
   npm start
   ```

## API Documentation

API endpoints will be available at `http://localhost:8080` once the backend is running.

## License

MIT
