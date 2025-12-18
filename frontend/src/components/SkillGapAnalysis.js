import React, { useState, useEffect } from 'react';
import './SkillGapAnalysis.css';
import { analyseSkillGap } from '../services/api';

function SkillGapAnalysis({ studentId, projectTitle, projectSkills, projectDifficulty, onClose }) {
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    handleAnalyse();
  }, []);

  const handleAnalyse = async () => {
    try {
      setLoading(true);
      setError(null);
      
      console.log('Calling skill gap API with:', {
        studentId,
        projectTitle,
        projectSkills,
        projectDifficulty
      });

      const response = await analyseSkillGap(
        studentId, 
        projectSkills,
        projectTitle,
        projectDifficulty
      );
      
      console.log('API Response:', response.data);
      setAnalysis(response.data);
    } catch (err) {
      console.error('Failed to fetch prediction:', err);
      console.error('Error details:', err.response?.data);
      setError('Failed to analyse skill gap. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="skill-gap-modal" onClick={onClose}>
        <div className="skill-gap-content" onClick={(e) => e.stopPropagation()}>
          <div className="loading-spinner">
            <div className="spinner"></div>
            <p>Analysing your skills...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="skill-gap-modal" onClick={onClose}>
        <div className="skill-gap-content" onClick={(e) => e.stopPropagation()}>
          <button className="close-button" onClick={onClose}>×</button>
          <div className="error-message">
            <h3>❌ Error</h3>
            <p>{error}</p>
            <button onClick={handleAnalyse} className="retry-button">Try Again</button>
          </div>
        </div>
      </div>
    );
  }

  if (!analysis) {
    return null;
  }

  const getReadinessColor = (readiness) => {
    if (readiness >= 90) return '#10b981';
    if (readiness >= 75) return '#3b82f6';
    if (readiness >= 60) return '#f59e0b';
    if (readiness >= 40) return '#ef4444';
    return '#dc2626';
  };

  const getStatusIcon = (status) => {
    switch(status) {
      case 'Ready': return '✅';
      case 'Nearly Ready': return '🟢';
      case 'Needs Improvement': return '⚠️';
      case 'Significant Gap': return '🔴';
      default: return '❓';
    }
  };

  return (
    <div className="skill-gap-modal" onClick={onClose}>
      <div className="skill-gap-content" onClick={(e) => e.stopPropagation()}>
        <button className="close-button" onClick={onClose}>×</button>
        
        <div className="skill-gap-header">
          <h2>📊 Skill Gap Analysis</h2>
          <h3>{analysis.projectTitle}</h3>
          <span className="difficulty-badge">{analysis.projectDifficulty}</span>
        </div>

        {/* Overall Readiness */}
        <div className="readiness-section">
          <div className="readiness-circle">
            <svg width="160" height="160">
              <circle
                cx="80"
                cy="80"
                r="70"
                fill="none"
                stroke="#e5e7eb"
                strokeWidth="12"
              />
              <circle
                cx="80"
                cy="80"
                r="70"
                fill="none"
                stroke={getReadinessColor(analysis.overallReadiness)}
                strokeWidth="12"
                strokeDasharray={`${2 * Math.PI * 70 * (analysis.overallReadiness / 100)} ${2 * Math.PI * 70}`}
                strokeDashoffset={2 * Math.PI * 70 * 0.25}
                strokeLinecap="round"
                transform="rotate(-90 80 80)"
              />
              <text
                x="80"
                y="75"
                textAnchor="middle"
                fontSize="32"
                fontWeight="bold"
                fill={getReadinessColor(analysis.overallReadiness)}
              >
                {Math.round(analysis.overallReadiness)}%
              </text>
              <text
                x="80"
                y="95"
                textAnchor="middle"
                fontSize="12"
                fill="#6b7280"
              >
                Ready
              </text>
            </svg>
          </div>
          <div className="readiness-details">
            <div className="readiness-level">
              <strong>Readiness Level:</strong>
              <span style={{ color: getReadinessColor(analysis.overallReadiness) }}>
                {analysis.readinessLevel}
              </span>
            </div>
            <div className="prep-time">
              <strong>Estimated Prep Time:</strong>
              <span>{analysis.estimatedPrepTime}</span>
            </div>
          </div>
        </div>

        {/* Strong Areas */}
        {analysis.strongAreas && analysis.strongAreas.length > 0 && (
          <div className="skills-section">
            <h4 className="section-title">💪 Strong Areas</h4>
            <div className="skills-list">
              {analysis.strongAreas.map((skill, index) => (
                <div key={index} className="skill-item strong">
                  <div className="skill-header">
                    <span className="skill-icon">{getStatusIcon(skill.status)}</span>
                    <span className="skill-name">{skill.skillName}</span>
                    <span className="skill-score">{skill.currentScore.toFixed(0)}%</span>
                  </div>
                  <div className="skill-bar">
                    <div 
                      className="skill-progress strong" 
                      style={{ width: `${(skill.currentScore / skill.requiredScore) * 100}%` }}
                    ></div>
                  </div>
                  
                  {skill.relatedCourses && skill.relatedCourses.length > 0 && (
                    <div className="related-courses">
                      <strong>📚 Related Courses You've Taken:</strong>
                      <ul>
                        {skill.relatedCourses.map((course, idx) => (
                          <li key={idx}>{course}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                  
                  <p className="skill-recommendation">{skill.recommendation}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Areas to Improve */}
        {analysis.areasToImprove && analysis.areasToImprove.length > 0 && (
          <div className="skills-section">
            <h4 className="section-title">📚 Areas to Improve</h4>
            <div className="skills-list">
              {analysis.areasToImprove.map((skill, index) => (
                <div key={index} className="skill-item needs-improvement">
                  <div className="skill-header">
                    <span className="skill-icon">{getStatusIcon(skill.status)}</span>
                    <span className="skill-name">{skill.skillName}</span>
                    <span className="skill-score gap">
                      {skill.currentScore.toFixed(0)}% → {skill.requiredScore.toFixed(0)}%
                      <span className="gap-value"> (Gap: {skill.gap.toFixed(0)})</span>
                    </span>
                  </div>
                  <div className="skill-bar">
                    <div 
                      className="skill-progress needs-improvement" 
                      style={{ width: `${(skill.currentScore / skill.requiredScore) * 100}%` }}
                    ></div>
                    <div 
                      className="skill-target" 
                      style={{ left: '100%' }}
                    ></div>
                  </div>
                  
                  {skill.relatedCourses && skill.relatedCourses.length > 0 && (
                    <div className="related-courses">
                      <strong>📚 Related Courses You've Taken:</strong>
                      <ul>
                        {skill.relatedCourses.map((course, idx) => (
                          <li key={idx}>{course}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                  
                  <p className="skill-recommendation">{skill.recommendation}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="action-buttons">
          <button onClick={onClose} className="close-action-button">Close</button>
        </div>
      </div>
    </div>
  );
}

export default SkillGapAnalysis;