import React, { useState } from 'react';
import './Login.css';

function Login({ onLoginSuccess }) {
  const [studentId, setStudentId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ studentId: parseInt(studentId) }),
      });

      const data = await response.json();

      if (data.success) {
        onLoginSuccess(data.student);
      } else {
        setError(data.message || 'Login failed. Please check your Student ID.');
      }
    } catch (err) {
      setError('Cannot connect to server. Make sure backend is running.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>Student Performance Tracker</h1>
          <p>Enter your Student ID to view your dashboard</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="studentId">Student ID</label>
            <input
              type="number"
              id="studentId"
              value={studentId}
              onChange={(e) => setStudentId(e.target.value)}
              placeholder="Enter your student ID (e.g., 9897587)"
              required
              autoFocus
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading || !studentId} className="login-button">
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="login-footer">
          <p className="hint">💡 Try Student IDs like: 9897587, 860750, 2733926</p>
        </div>
      </div>
    </div>
  );
}

export default Login;