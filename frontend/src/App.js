import React, { useState, useEffect } from 'react';
import './App.css';
import Login from './components/Login';
import PreferencesPage from './components/PreferencesPage';
import Recommendations from './components/Recommendations';
import GradesPage from './components/GradesPage';
import { getStudentPerformance } from './services/api';

function App() {
  const [currentStudent, setCurrentStudent] = useState(null);
  const [performance, setPerformance] = useState(null);
  const [currentPage, setCurrentPage] = useState('dashboard'); // 'dashboard', 'preferences', 'recommendations', 'grades'
  const [userPreferences, setUserPreferences] = useState(null);
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
    localStorage.setItem('currentStudent', JSON.stringify(student));
    await fetchStudentData(student.studentId);
  };

  // Fetch student performance
  const fetchStudentData = async (studentId) => {
    try {
      setLoading(true);
      setError(null);

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
    setCurrentPage('dashboard');
    setUserPreferences(null);
    setError(null);
    localStorage.removeItem('currentStudent');
  };

  // Navigate to recommendations with preferences
  const handleGenerateRecommendations = (preferences) => {
    setUserPreferences(preferences);
    setCurrentPage('recommendations');
  };

  // If not logged in, show login page
  if (!currentStudent) {
    return <Login onLoginSuccess={handleLoginSuccess} />;
  }

  // Show grades page
  if (currentPage === 'grades') {
    return (
      <GradesPage
        studentId={currentStudent.studentId}
        onBack={() => setCurrentPage('dashboard')}
      />
    );
  }

  // Show preferences page
  if (currentPage === 'preferences') {
    return (
      <PreferencesPage
        studentId={currentStudent.studentId}
        onGenerateRecommendations={handleGenerateRecommendations}
        onBack={() => setCurrentPage('dashboard')}
      />
    );
  }

  // Show recommendations page
  if (currentPage === 'recommendations') {
    return (
      <Recommendations 
        studentId={currentStudent.studentId}
        userPreferences={userPreferences}
        onBack={() => setCurrentPage('preferences')}
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
                  {performance.competencies
                    .filter(comp => comp.score > 0)
                    .map((comp, index) => (
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
                <div className="section-header-with-action">
                  <h3>Recent Grades</h3>
                  <button 
                    onClick={() => setCurrentPage('grades')}
                    className="view-more-button"
                  >
                    View All Grades →
                  </button>
                </div>
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
                    {performance.grades.slice(0, 5).map((grade, index) => (
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

        {/* CTA to View Preferences & Recommendations */}
        <div className="recommendations-cta card">
          <h2>Ready to find your perfect capstone project?</h2>
          <p>Set your preferences and get personalized AI-powered recommendations</p>
          <button 
            onClick={() => setCurrentPage('preferences')}
            className="view-recommendations-button"
          >
            Preferences & Recommendations →
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;