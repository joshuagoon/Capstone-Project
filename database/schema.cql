-- Cassandra Schema for Student Performance Tracker

-- Create keyspace if not exists
CREATE KEYSPACE IF NOT EXISTS subjectplanning
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE subjectplanning;

-- Students table
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY,
    name TEXT,
    email TEXT,
    program TEXT,
    enrolment_year INT,
    interests TEXT
);

-- Courses table
CREATE TABLE IF NOT EXISTS courses (
    course_id INT PRIMARY KEY,
    course_code TEXT,
    course_name TEXT,
    credits INT,
    description TEXT
);

-- Competencies table
CREATE TABLE IF NOT EXISTS competencies (
    competency_id INT PRIMARY KEY,
    name TEXT,
    description TEXT,
    category TEXT
);

-- Capstone Projects table
CREATE TABLE IF NOT EXISTS capstone_projects (
    project_id INT PRIMARY KEY,
    title TEXT,
    description TEXT,
    difficulty_level TEXT,
    required_competencies TEXT,
    supervisor TEXT,
    status TEXT
);

-- Enrolments table
CREATE TABLE IF NOT EXISTS enrolments (
    enrolment_id INT PRIMARY KEY,
    student_id INT,
    course_id INT,
    semester TEXT,
    year INT,
    status TEXT,
    final_grade DECIMAL
);

-- Assessments table
CREATE TABLE IF NOT EXISTS assessments (
    assessment_id INT PRIMARY KEY,
    course_id INT,
    name TEXT,
    type TEXT,
    max_score DECIMAL,
    weight DECIMAL,
    due_date DATE
);

-- Student Assessments table
CREATE TABLE IF NOT EXISTS student_assessments (
    student_assessment_id INT PRIMARY KEY,
    enrolment_id INT,
    assessment_id INT,
    score DECIMAL,
    feedback TEXT,
    submitted_at TIMESTAMP
);

-- Recommendations table
CREATE TABLE IF NOT EXISTS student_project_recommendations (
    recommendation_id INT PRIMARY KEY,
    student_id INT,
    project_id INT,
    score DECIMAL,
    reason TEXT,
    generated_at TIMESTAMP,
    is_accepted BOOLEAN
);

-- Create indexes for common queries
CREATE INDEX IF NOT EXISTS ON students (email);
CREATE INDEX IF NOT EXISTS ON enrolments (student_id);
CREATE INDEX IF NOT EXISTS ON capstone_projects (status);