# Student Performance Tracker and Capstone Project Recommendation System

A full-stack web application that helps Computer Science students track their academic performance and receive AI-powered capstone project recommendations.

## Overview

This system integrates real-time performance tracking with AI-powered project recommendations, helping students make informed decisions about their capstone projects based on their academic strengths and interests.

**Key Features:**
- Performance dashboard with competency analysis across 10 technical domains
- AI-powered capstone project recommendations using Claude API
- Skill gap analysis for project preparation
- Favourites management and preference customisation
- Comprehensive grade history with filtering

**Technology Stack:**
- **Backend:** Scala, Spring Boot, Apache Cassandra
- **Frontend:** React, JavaScript, CSS
- **AI:** Anthropic Claude API

## Project Structure

```
Capstone-Project/
├── app/                  # Backend (Scala/Spring Boot)
│   ├── src/
│   └── build.gradle
├── frontend/            # Frontend (React)
│   ├── src/
│   └── package.json
└── README.md
```

## Installation & Setup

### Backend Prerequisites
- Java 17+
- Gradle 8.0+
- Access to Cassandra database

### Frontend Prerequisites
- Node.js 18+
- npm or yarn

### Backend Setup

1. Configure database in `app/src/main/resources/application.properties`:
   ```properties
   spring.cassandra.contact-points=your-cassandra-host
   spring.cassandra.keyspace=student_performance
   claude.api.key=your-api-key
   ```

2. Run backend:
   ```bash
   cd app
   ./gradlew bootRun
   ```
   Backend runs on `http://localhost:8080`

### Frontend Setup

See [frontend/README.md](frontend/README.md) for details.

```bash
cd frontend
npm install
npm start
```
Frontend runs on `http://localhost:3000`

## Evaluation Results

- **System Usability Scale (SUS):** 82.79/100 (Excellent)
- **Technology Acceptance Model:**
  - Perceived Usefulness: 5.62/7
  - Perceived Ease of Use: 6.31/7
- **Participants:** 17 final-year CS students

## Project Information

**Student:** Joshua Goon Yu Ming (19104538)  
**Supervisor:** Dr. Chin Teck Min  
**Institution:** Sunway University  
**Programme:** BSc (Hons) Computer Science  
**Semester:** September 2025

## Contact

Email: 19104538@imail.sunway.edu.my