# Capstone-Project
GitHub Repository for Joshua Goon's Capstone Project

Backend (Scala/Spring Boot)
    Controllers (app/src/main/scala/org/example/controllers/)
        AIRecommendationController.scala
            Handles all HTTP API endpoints for the application:
            - Login: Authenticates students by ID
            - Get Student: Retrieves student profile information
            - Get Performance: Fetches academic performance data (CGPA, grades, competencies)
            - Get AI Recommendations: Generates personalized capstone project recommendations
            - Health Check: Verifies system and database connectivity

    Models (app/src/main/scala/org/example/models/)
        RealStudent.scala
            Database entity representing a student record:
            - Maps to students table in Cassandra
            - Fields: ID, name, email, CGPA, programme, cohort
            - Used by repository to fetch student data

        RealSubject.scala
            Database entity representing a course/subject record:
            - Maps to subjects table in Cassandra
            - Fields: student ID, subject code/name, grade, percentage, exam year/month
            - Used to track all courses a student has taken

    Repositories (app/src/main/scala/org/example/repositories/)
        RealStudentRepository.scala
            Data access layer for student records:
            - Extends CassandraRepository
            - Provides methods to query students table
            - Main method: findById() - gets student by ID

        RealSubjectRepository.scala
            Data access layer for subject/course records:
            - Extends CassandraRepository
            - Provides methods to query subjects table
            - Main method: findByStudentId() - gets all courses for a student

    Services (app/src/main/scala/org/example/services/)
        StudentDataService.scala
            Business logic for student data processing:
            - getStudentById: Fetches student from database
            - buildStudentProfile: Creates profile for AI (top subjects, interests, CGPA)
            - getStudentPerformance: Calculates competencies, formats grades for dashboard
            - calculateCompetencies: Analyzes courses and assigns proficiency levels (10 competency areas)
            - extractInterests: Identifies student interests from course names

        AIRecommendationService.scala
            AI-powered recommendation engine:
            - generateRecommendations: Main method - generates 3 personalized project ideas
            - buildCreativePrompt: Creates detailed prompt for Claude API with student profile + preferences
            - callClaudeAPI: Makes HTTP request to Anthropic's Claude API
            - parseRecommendations: Extracts project recommendations from AI response (JSON)
            - getFallbackRecommendations: Rule-based algorithm if API fails (skill matching, difficulty matching)
            - Uses Claude Sonnet 4 with temperature 0.9 for creative, varied responses

    Configuration (app/src/main/resources/)
        application.properties
            Spring Boot configuration file:
            - Database connection settings (Cassandra host, port, keyspace)
            - Server port configuration
            - API key configuration
            - Logging levels


    Main Application (app/src/main/scala/org/example/)
        Application.scala
            Spring Boot application entry point:
            - Main method that starts the server
            - Configures Spring Boot application
            - Enables auto-configuration and component scanning

Frontend (React)
    Main App Files (frontend/src/)
        index.js
            React application entry point:
            - Mounts React app to DOM
            - Renders App component into <div id="root">
            - Wraps in <React.StrictMode> for development warnings

        App.js
            Root component and application orchestrator:
            - State Management: Manages currentStudent, currentPage, performance, userPreferences
            - Navigation: Controls which page to display (dashboard, preferences, grades, recommendations)
            - Session Persistence: Loads/saves login state to localStorage
            - Data Fetching: Fetches student performance on login
            - Routing Logic: Determines which component to render based on currentPage

        index.css
            Global CSS styles:
            - Base styles for body, fonts
            - CSS resets
            - Global color variables

        App.css
            Styles for main dashboard:
            - Student profile card styles
            - Performance card (CGPA, courses, competencies)
            - Recent grades table
            - Recommendations CTA button
            - Responsive design for mobile

    Components (frontend/src/components/)
        Login.js
            Login page component:
            - Input field for student ID
            - Validates and authenticates via /api/login endpoint
            - On success, calls onLoginSuccess callback to update App state
            - Error handling for invalid IDs

        Login.css
            Styles for login page:
            - Centered login form
            - Input field styling
            - Button hover effects
            - Error message styling

        PreferencesPage.jsx
            Preferences and favorites management page:
            - Favorites Display: Shows starred projects with match scores
            - Preferences Form: Interests, difficulty level, avoid topics, additional notes
            - Save Preferences: Persists to localStorage
            - Remove Favorites: Delete individual or all favorites
            - Generate Button: Navigates to recommendations page with preferences

        PreferencesPage.css
            Styles for preferences page:
            - Two-column layout for favorites and form
            - Favorite card styling (gold border for starred items)
            - Form input/textarea styles
            - Generate recommendations button (gradient)

        Recommendations.jsx
            AI recommendations display page:
            - Fetch Recommendations: Calls /api/recommendations with student ID + preferences
            - Display 3 Projects: Shows AI-generated projects with match scores and reasons
            - Favorite System: Star/unstar projects, persist to localStorage
            - Regenerate: Individual or bulk regeneration (excludes already shown projects)
            - Sync with Preferences: Saves favorites for display on preferences page

        Recommendations.css
            Styles for recommendations page:
            - 3-column grid for project cards
            - Favorited card highlighting (gold border, gradient background)
            - Match badge styling
            - Regenerate button styles
            - Favorites counter section

        GradesPage.jsx
            All grades display with filtering page:
            - Fetch All Grades: Gets complete academic history from /api/performance
            - Filtering: By course name, grade, year, month
            - Sorting: By date (newest/oldest), grade, percentage, course name
            - Dynamic Filters: Dropdown options generated from student's actual data
            - Clear Filters: Reset all filters at once
            - Results Display: Shows all courses matching filters

        GradesPage.css
            Styles for grades page:
            - Filter controls grid layout
            - Full-width grades table
            - Color-coded grade badges (A=green, B=blue, C=orange, D/F=red)
            - Responsive table design

    Services (frontend/src/services/)
        api.js
            API communication layer:
            - login: POST to /api/login - authenticate student
            - getStudentPerformance: GET /api/performance/{studentId} - fetch academic data
            - getAIRecommendations: GET /api/recommendations/{studentId} - fetch AI projects
            - Sends preferences as query parameters (interests, difficulty, avoid, notes)
            - Sends exclude IDs to avoid duplicate recommendations
            - Uses Axios for HTTP requests
            - Base URL: http://localhost:8080/api

Configuration Files
    Build Files
        build.gradle (Backend)
            Gradle build configuration:
            - Dependencies: Spring Boot, Cassandra, Jackson (JSON), Scala
            - Build tasks and plugins
            - Compile settings

        package.json (Frontend)
            NPM package configuration:
            - Dependencies: React, Axios
            - Scripts: npm start, npm build
            - Project metadata