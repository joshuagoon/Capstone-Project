import React, { useState, useEffect } from 'react';
import './App.css';
import Login from './components/Login';
import Recommendations from './components/Recommendations';
import { getStudentPerformance } from './services/api';

function App() {
  const [currentStudent, setCurrentStudent] = useState(null);
  const [performance, setPerformance] = useState(null);
  const [showRecommendations, setShowRecommendations] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Load saved session on mount
  useEffect(() => {
    const savedStudent = localStorage.getItem('currentStudent');
    if (savedStudent) {
      try {
        const student = JSON.parse(savedStudent);
        setCurrentStudent(student);
        fetchStudentData(student.studentId);
      } catch (e) {
        console.error('Failed to load saved session:', e);
        localStorage.removeItem('currentStudent');
      }
    }
  }, []);

  // Handle successful login
  const handleLoginSuccess = async (student) => {
    setCurrentStudent(student);
    // Save to localStorage
    localStorage.setItem('currentStudent', JSON.stringify(student));
    await fetchStudentData(student.studentId);
  };

  // Fetch student performance
  const fetchStudentData = async (studentId) => {
    try {
      setLoading(true);
      setError(null);

      // Fetch performance data
      const perfResponse = await getStudentPerformance(studentId);
      setPerformance(perfResponse.data);
    } catch (err) {
      setError('Failed to fetch student data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Logout function
  const handleLogout = () => {
    setCurrentStudent(null);
    setPerformance(null);
    setShowRecommendations(false);
    setError(null);
    // Clear from localStorage
    localStorage.removeItem('currentStudent');
  };

  // If not logged in, show login page
  if (!currentStudent) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  // Show recommendations page
  if (showRecommendations) {
    return (
      <Recommendations 
        studentId={currentStudent.studentId}
        onBack={() => setShowRecommendations(false)}
      />
    );
  }

  // Show loading state
  if (loading) {
    return (
      <div className="App">
        <div className="loading">Loading student data...</div>
      </div>
    );
  }

  // Show error state
  if (error) {
    return (
      <div className="App">
        <div className="error">{error}</div>
        <button onClick={handleLogout}>Back to Login</button>
      </div>
    );
  }

  // Main dashboard
  return (
    <div className="App">
      <header className="App-header">
        <h1>Student Performance Tracker & Capstone Recommender</h1>
        <button onClick={handleLogout} className="logout-button">
          Logout
        </button>
      </header>

      <div className="container">
        {/* Student Info */}
        <div className="student-info card">
          <h2>Student Profile</h2>
          <p><strong>Student ID:</strong> {currentStudent.studentId}</p>
          <p><strong>Name:</strong> {currentStudent.name}</p>
          <p><strong>Email:</strong> {currentStudent.email}</p>
          <p><strong>Program:</strong> {currentStudent.program}</p>
          <p><strong>Cohort:</strong> {currentStudent.cohort}</p>
          <p><strong>Overall CGPA:</strong> {currentStudent.cgpa.toFixed(2)}</p>
        </div>

        {/* Performance Dashboard */}
        {performance && (
          <div className="performance card">
            <h2>Academic Performance</h2>
            <div className="stats">
              <div className="stat">
                <span className="stat-label">CGPA</span>
                <span className="stat-value">{performance.gpa.toFixed(2)}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Completed Courses</span>
                <span className="stat-value">{performance.completedCourses}</span>
              </div>
            </div>

            {performance.competencies && performance.competencies.length > 0 && (
              <>
                <h3>Competencies</h3>
                <div className="competencies">
                  {performance.competencies.map((comp, index) => (
                    <div key={index} className="competency">
                      <div className="competency-header">
                        <span>{comp.name}</span>
                        <span className="badge">{comp.level}</span>
                      </div>
                      <div className="progress-bar">
                        <div 
                          className="progress-fill" 
                          style={{width: `${comp.score}%`}}
                        ></div>
                      </div>
                      <span className="score">{comp.score}%</span>
                    </div>
                  ))}
                </div>
              </>
            )}

            {performance.grades && performance.grades.length > 0 && (
              <>
                <h3>Recent Grades</h3>
                <table className="grades-table">
                  <thead>
                    <tr>
                      <th>Course</th>
                      <th>Grade</th>
                      <th>Percentage</th>
                      <th>Month / Year</th>
                    </tr>
                  </thead>
                  <tbody>
                    {performance.grades.map((grade, index) => (
                      <tr key={index}>
                        <td>{grade.course}</td>
                        <td><span className="grade-badge">{grade.grade}</span></td>
                        <td>{grade.percentage ? `${grade.percentage.toFixed(1)}%` : 'N/A'}</td>
                        <td>{grade.semester}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            )}
          </div>
        )}

        {/* CTA to View Recommendations */}
        <div className="recommendations-cta card">
          <h2>Ready to find your perfect capstone project?</h2>
          <p>Get personalized AI-powered recommendations based on your academic profile</p>
          <button 
            onClick={() => setShowRecommendations(true)}
            className="view-recommendations-button"
          >
            View AI Recommendations →
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;