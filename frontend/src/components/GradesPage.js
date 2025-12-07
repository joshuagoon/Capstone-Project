import React, { useState, useEffect } from 'react';
import './GradesPage.css';
import { getStudentPerformance } from '../services/api';

function GradesPage({ studentId, onBack }) {
  const [allGrades, setAllGrades] = useState([]);
  const [filteredGrades, setFilteredGrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Filter states
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedGrade, setSelectedGrade] = useState('');
  const [selectedYear, setSelectedYear] = useState('');
  const [selectedMonth, setSelectedMonth] = useState('');
  const [sortBy, setSortBy] = useState('date-desc');

  // Extract unique values for filters
  const [availableGrades, setAvailableGrades] = useState([]);
  const [availableYears, setAvailableYears] = useState([]);
  const [availableMonths, setAvailableMonths] = useState([]);

  // Scroll to top when component mounts
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    fetchGrades();
  }, [studentId]);

  useEffect(() => {
    applyFilters();
  }, [searchTerm, selectedGrade, selectedYear, selectedMonth, sortBy, allGrades]);

  const fetchGrades = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getStudentPerformance(studentId);
      
      if (response.data && response.data.grades) {
        setAllGrades(response.data.grades);
        
        // Extract unique filter options
        const grades = [...new Set(response.data.grades.map(g => g.grade))].sort();
        const years = [...new Set(response.data.grades.map(g => {
          const parts = g.semester.split('/');
          return parts[1];
        }))].sort().reverse();
        const months = [...new Set(response.data.grades.map(g => {
          const parts = g.semester.split('/');
          return parts[0];
        }))].sort((a, b) => parseInt(a) - parseInt(b));
        
        setAvailableGrades(grades);
        setAvailableYears(years);
        setAvailableMonths(months);
      }
    } catch (err) {
      setError('Failed to load grades');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...allGrades];

    // Search filter
    if (searchTerm) {
      filtered = filtered.filter(grade =>
        grade.course.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    // Grade filter
    if (selectedGrade) {
      filtered = filtered.filter(grade => grade.grade === selectedGrade);
    }

    // Year filter
    if (selectedYear) {
      filtered = filtered.filter(grade => {
        const parts = grade.semester.split('/');
        return parts[1] === selectedYear;
      });
    }

    // Month filter
    if (selectedMonth) {
      filtered = filtered.filter(grade => {
        const parts = grade.semester.split('/');
        return parts[0] === selectedMonth;
      });
    }

    // Sorting
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'date-desc':
          const [aMonth, aYear] = a.semester.split('/');
          const [bMonth, bYear] = b.semester.split('/');
          if (bYear !== aYear) return parseInt(bYear) - parseInt(aYear);
          return parseInt(bMonth) - parseInt(aMonth);
        
        case 'date-asc':
          const [aMonthAsc, aYearAsc] = a.semester.split('/');
          const [bMonthAsc, bYearAsc] = b.semester.split('/');
          if (aYearAsc !== bYearAsc) return parseInt(aYearAsc) - parseInt(bYearAsc);
          return parseInt(aMonthAsc) - parseInt(bMonthAsc);
        
        case 'grade':
          const gradeOrder = { 'A+': 1, 'A': 2, 'A-': 3, 'B+': 4, 'B': 5, 'B-': 6, 'C+': 7, 'C': 8, 'C-': 9, 'D': 10, 'F': 11 };
          return (gradeOrder[a.grade] || 99) - (gradeOrder[b.grade] || 99);
        
        case 'percentage':
          return (b.percentage || 0) - (a.percentage || 0);
        
        case 'course':
          return a.course.localeCompare(b.course);
        
        default:
          return 0;
      }
    });

    setFilteredGrades(filtered);
  };

  const clearFilters = () => {
    setSearchTerm('');
    setSelectedGrade('');
    setSelectedYear('');
    setSelectedMonth('');
    setSortBy('date-desc');
  };

  if (loading) {
    return (
      <div className="grades-page">
        <div className="loading">Loading grades...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="grades-page">
        <div className="error">{error}</div>
        <button onClick={onBack} className="back-button">Back to Dashboard</button>
      </div>
    );
  }

  return (
    <div className="grades-page">
      <div className="grades-header">
        <button onClick={onBack} className="back-button">
          ← Back to Dashboard
        </button>
        <h1>All Grades</h1>
        <p className="subtitle">Complete academic history - {allGrades.length} courses</p>
      </div>

      <div className="grades-container">
        {/* Filters Section */}
        <div className="filters-section">
          <div className="filters-row">
            <div className="filter-group">
              <label>Search Course</label>
              <input
                type="text"
                placeholder="Search by course name..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            <div className="filter-group">
              <label>Grade</label>
              <select value={selectedGrade} onChange={(e) => setSelectedGrade(e.target.value)}>
                <option value="">All Grades</option>
                {availableGrades.map(grade => (
                  <option key={grade} value={grade}>{grade}</option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label>Year</label>
              <select value={selectedYear} onChange={(e) => setSelectedYear(e.target.value)}>
                <option value="">All Years</option>
                {availableYears.map(year => (
                  <option key={year} value={year}>20{year}</option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label>Month</label>
              <select value={selectedMonth} onChange={(e) => setSelectedMonth(e.target.value)}>
                <option value="">All Months</option>
                {availableMonths.map(month => (
                  <option key={month} value={month}>
                    {['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'][parseInt(month) - 1]}
                  </option>
                ))}
              </select>
            </div>

            <div className="filter-group">
              <label>Sort By</label>
              <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
                <option value="date-desc">Date (Newest First)</option>
                <option value="date-asc">Date (Oldest First)</option>
                <option value="grade">Grade (Best First)</option>
                <option value="percentage">Percentage (Highest First)</option>
                <option value="course">Course Name (A-Z)</option>
              </select>
            </div>

            <button onClick={clearFilters} className="clear-filters-btn">
              Clear Filters
            </button>
          </div>
        </div>

        {/* Grades Table */}
        <div className="grades-table-container">
          {filteredGrades.length === 0 ? (
            <div className="no-results">
              <p>No grades match your filters</p>
            </div>
          ) : (
            <table className="grades-table-full">
              <thead>
                <tr>
                  <th>Course</th>
                  <th>Grade</th>
                  <th>Percentage</th>
                  <th>Month / Year</th>
                </tr>
              </thead>
              <tbody>
                {filteredGrades.map((grade, index) => (
                  <tr key={index}>
                    <td className="course-cell">{grade.course}</td>
                    <td>
                      <span className={`grade-badge grade-${grade.grade.replace('+', 'plus').replace('-', 'minus')}`}>
                        {grade.grade}
                      </span>
                    </td>
                    <td className="percentage-cell">
                      {grade.percentage ? `${grade.percentage.toFixed(1)}%` : 'N/A'}
                    </td>
                    <td className="date-cell">{grade.semester}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default GradesPage;