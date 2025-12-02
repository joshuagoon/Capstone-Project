import React, { useState, useEffect } from 'react';
import './App.css';
import Login from './components/Login';
import { getStudentPerformance, getAIRecommendations } from './services/api';

function App() {
  const [currentStudent, setCurrentStudent] = useState(null);
  const [performance, setPerformance] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Handle successful login
  const handleLoginSuccess = async (student) => {
    setCurrentStudent(student);
    await fetchStudentData(student.studentId);
  };

  // Fetch student performance and recommendations
  const fetchStudentData = async (studentId) => {
    try {
      setLoading(true);
      setError(null);

      // Fetch performance data
      const perfResponse = await getStudentPerformance(studentId);
      setPerformance(perfResponse.data);

      // Fetch AI recommendations
      const recResponse = await getAIRecommendations(studentId);
      setRecommendations(recResponse.data);
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
    setRecommendations([]);
    setError(null);
  };

  // If not logged in, show login page
  if (!currentStudent) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
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
                      <th>Semester</th>
                    </tr>
                  </thead>
                  <tbody>
                    {performance.grades.map((grade, index) => (
                      <tr key={index}>
                        <td>{grade.course}</td>
                        <td><span className="grade-badge">{grade.grade}</span></td>
                        <td>{grade.semester}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            )}
          </div>
        )}

        {/* AI Recommendations */}
        <div className="recommendations card">
          <h2>AI-Recommended Capstone Projects</h2>
          {recommendations && recommendations.length > 0 ? (
            <div className="recommendation-list">
              {recommendations.map((rec, index) => (
                <div key={index} className="recommendation-item">
                  <div className="rec-header">
                    <h3>{rec.projectTitle}</h3>
                    <span className="confidence-score">
                      {(rec.score * 100).toFixed(0)}% match
                    </span>
                  </div>
                  <p className="reason">{rec.reason}</p>
                </div>
              ))}
            </div>
          ) : (
            <p>No recommendations available for this student.</p>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;