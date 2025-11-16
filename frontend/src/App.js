import React, { useState, useEffect } from 'react';
import './App.css';
import { getAllStudents, getStudentPerformance, getRecommendations } from './services/api';

function App() {
  const [students, setStudents] = useState([]);
  const [selectedStudent, setSelectedStudent] = useState(null);
  const [performance, setPerformance] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchStudents();
  }, []);

  const fetchStudents = async () => {
    try {
      setLoading(true);
      const response = await getAllStudents();
      setStudents(response.data);
      // Automatically select first student
      if (response.data.length > 0) {
        selectStudent(response.data[0].studentId);
      }
    } catch (err) {
      setError('Failed to fetch students. Make sure backend is running on port 8080.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const selectStudent = async (studentId) => {
    try {
      setLoading(true);
      const student = students.find(s => s.studentId === studentId);
      setSelectedStudent(student);

      // Fetch performance data
      const perfResponse = await getStudentPerformance(studentId);
      setPerformance(perfResponse.data);

      // Fetch recommendations
      const recResponse = await getRecommendations(studentId);
      setRecommendations(recResponse.data);
    } catch (err) {
      setError('Failed to fetch student data');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && students.length === 0) {
    return (
      <div className="App">
        <div className="loading">Loading...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="App">
        <div className="error">{error}</div>
      </div>
    );
  }

  return (
    <div className="App">
      <header className="App-header">
        <h1>Student Performance Tracker & Capstone Recommender</h1>
      </header>

      <div className="container">
        {/* Student Selector */}
        <div className="student-selector">
          <h2>Select Student</h2>
          <select 
            onChange={(e) => selectStudent(parseInt(e.target.value))}
            value={selectedStudent?.studentId || ''}
          >
            {students.map(student => (
              <option key={student.studentId} value={student.studentId}>
                {student.name}
              </option>
            ))}
          </select>
        </div>

        {selectedStudent && (
          <>
            {/* Student Info */}
            <div className="student-info card">
              <h2>Student Profile</h2>
              <p><strong>Name:</strong> {selectedStudent.name}</p>
              <p><strong>Email:</strong> {selectedStudent.email}</p>
              <p><strong>Program:</strong> {selectedStudent.program}</p>
              <p><strong>Year:</strong> {selectedStudent.enrolmentYear}</p>
              <p><strong>Interests:</strong> {selectedStudent.interests}</p>
            </div>

            {/* Performance Dashboard */}
            {performance && (
              <div className="performance card">
                <h2>Academic Performance</h2>
                <div className="stats">
                  <div className="stat">
                    <span className="stat-label">GPA</span>
                    <span className="stat-value">{performance.gpa}</span>
                  </div>
                  <div className="stat">
                    <span className="stat-label">Completed Courses</span>
                    <span className="stat-value">{performance.completedCourses}</span>
                  </div>
                </div>

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
              </div>
            )}

            {/* Recommendations */}
            <div className="recommendations card">
              <h2>Recommended Capstone Projects</h2>
              {recommendations.length > 0 ? (
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
                <p>No recommendations available</p>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default App;