# Frontend - Student Performance Tracker

React frontend for the Student Performance Tracker and Capstone Project Recommendation System.

## Overview

**Technology Stack:**
- React 18
- JavaScript ES6+
- CSS3
- Axios (HTTP client)

**Key Components:**
- Login - Student authentication
- Dashboard - Performance tracking with competencies
- Preferences - Manage favourites and recommendation preferences
- Recommendations - AI-generated project ideas
- Grades - Full grade history with filters

## Installation

### Prerequisites
- Node.js 18+
- npm or yarn

### Setup

```bash
# Install dependencies
npm install

# Start development server
npm start
```

Development server runs on `http://localhost:3000`

### Production Build

```bash
npm run build
```

Creates optimised build in `build/` directory.

## Configuration

Update API endpoint in `src/services/api.js`:

**For local development:**
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

**For production (AWS):**
```javascript
const API_BASE_URL = 'http://YOUR-AWS-IP:8080/api';
```

**Using environment variables (recommended):**

Create `.env.development`:
```
REACT_APP_API_URL=http://localhost:8080/api
```

Create `.env.production`:
```
REACT_APP_API_URL=http://YOUR-AWS-IP:8080/api
```

## Project Structure

```
frontend/
├── src/
│   ├── components/       # React components
│   ├── services/         # API calls
│   ├── App.js           # Main app
│   └── index.js         # Entry point
├── public/
└── package.json
```

## Common Commands

```bash
npm start          # Start development server
npm test           # Run tests
npm run build      # Create production build
```

## Notes

- Code uses British English spelling (favourites, analyse)
- Data stored in localStorage (favourites, preferences)
- Backend must be running on port 8080