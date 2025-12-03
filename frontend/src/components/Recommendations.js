import React, { useState, useEffect } from 'react';
import './Recommendations.css';
import { getAIRecommendations } from '../services/api';

function Recommendations({ studentId, onBack }) {
  const [allRecommendations, setAllRecommendations] = useState([]);
  const [displayedRecommendations, setDisplayedRecommendations] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(false);
  const [regeneratingIndex, setRegeneratingIndex] = useState(null);
  const [error, setError] = useState(null);

  // Load favorites from localStorage on mount
  useEffect(() => {
    const savedFavorites = localStorage.getItem(`favorites_${studentId}`);
    if (savedFavorites) {
      try {
        setFavorites(JSON.parse(savedFavorites));
      } catch (e) {
        console.error('Failed to load favorites:', e);
      }
    }
  }, [studentId]);

  // Save favorites to localStorage whenever they change
  useEffect(() => {
    if (favorites.length >= 0) {
      localStorage.setItem(`favorites_${studentId}`, JSON.stringify(favorites));
    }
  }, [favorites, studentId]);

  // Fetch recommendations on component mount
  useEffect(() => {
    fetchRecommendations();
  }, [studentId]);

  const fetchRecommendations = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAIRecommendations(studentId);
      
      // Store all recommendations
      setAllRecommendations(response.data);
      setDisplayedRecommendations(response.data);
    } catch (err) {
      setError('Failed to load recommendations');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleFavorite = (projectId) => {
    if (favorites.includes(projectId)) {
      // Remove from favorites
      setFavorites(favorites.filter(id => id !== projectId));
    } else {
      // Add to favorites
      setFavorites([...favorites, projectId]);
    }
  };

  const handleRegenerate = async (index) => {
    try {
      setRegeneratingIndex(index);
      setError(null);
      
      // Get all currently displayed project IDs to exclude
      const excludeIds = displayedRecommendations.map(rec => rec.projectId);
      
      // Fetch new recommendations excluding current ones
      const response = await getAIRecommendations(studentId, excludeIds);
      
      if (response.data.length > 0) {
        // Replace at index with first new recommendation
        const newDisplayed = [...displayedRecommendations];
        newDisplayed[index] = response.data[0];
        setDisplayedRecommendations(newDisplayed);
        
        // Add to pool
        setAllRecommendations([...allRecommendations, ...response.data]);
      } else {
        setError('No more unique recommendations available');
      }
    } catch (err) {
      setError('Failed to regenerate recommendation');
      console.error(err);
    } finally {
      setRegeneratingIndex(null);
    }
  };

  const handleRegenerateAll = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Exclude favorited projects
      const favoritedProjects = displayedRecommendations.filter(rec => 
        favorites.includes(rec.projectId)
      );
      const excludeIds = favoritedProjects.map(rec => rec.projectId);
      
      // Get new recommendations
      const response = await getAIRecommendations(studentId, excludeIds);
      
      // Build new displayed list: keep favorites, add new recommendations
      const newDisplayed = [];
      let newRecIndex = 0;
      
      for (let i = 0; i < 3; i++) {
        if (i < displayedRecommendations.length && favorites.includes(displayedRecommendations[i].projectId)) {
          newDisplayed.push(displayedRecommendations[i]);
        } else if (newRecIndex < response.data.length) {
          newDisplayed.push(response.data[newRecIndex]);
          newRecIndex++;
        }
      }
      
      setDisplayedRecommendations(newDisplayed);
      setAllRecommendations([...allRecommendations, ...response.data]);
    } catch (err) {
      setError('Failed to regenerate recommendations');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleClearFavorites = () => {
    if (window.confirm('Are you sure you want to clear all favorites?')) {
      setFavorites([]);
      localStorage.removeItem(`favorites_${studentId}`);
    }
  };

  if (loading && displayedRecommendations.length === 0) {
    return (
      <div className="recommendations-page">
        <div className="loading-spinner">Loading recommendations...</div>
      </div>
    );
  }

  return (
    <div className="recommendations-page">
      <div className="recommendations-header">
        <button onClick={onBack} className="back-button">
          ← Back to Dashboard
        </button>
        <h1>AI-Recommended Capstone Projects</h1>
        <p className="subtitle">Personalized recommendations based on your academic profile</p>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {favorites.length > 0 && (
        <div className="favorites-section">
          <h2>⭐ Your Favorites ({favorites.length})</h2>
          <p className="favorites-hint">
            You've favorited {favorites.length} project{favorites.length !== 1 ? 's' : ''}
          </p>
          <button onClick={handleClearFavorites} className="clear-favorites-button">
            Clear All Favorites
          </button>
        </div>
      )}

      <div className="recommendations-grid">
        {displayedRecommendations.map((rec, index) => (
          <div 
            key={`${rec.projectId}-${index}`}
            className={`recommendation-card ${favorites.includes(rec.projectId) ? 'favorited' : ''}`}
          >
            <div className="card-header">
              <div className="project-info">
                <h3>{rec.projectTitle}</h3>
                <span className="match-badge">
                  {(rec.score * 100).toFixed(0)}% match
                </span>
              </div>
              <button
                onClick={() => handleFavorite(rec.projectId)}
                className={`favorite-button ${favorites.includes(rec.projectId) ? 'active' : ''}`}
                title={favorites.includes(rec.projectId) ? 'Remove from favorites' : 'Add to favorites'}
              >
                {favorites.includes(rec.projectId) ? '★' : '☆'}
              </button>
            </div>

            <div className="card-body">
              <p className="reason">{rec.reason}</p>
            </div>

            <div className="card-footer">
              <button
                onClick={() => handleRegenerate(index)}
                className="regenerate-button"
                disabled={regeneratingIndex === index || favorites.includes(rec.projectId)}
                title={favorites.includes(rec.projectId) ? 'Cannot regenerate favorited projects' : 'Get a different recommendation'}
              >
                {regeneratingIndex === index ? '🔄 Regenerating...' : '🔄 Regenerate'}
              </button>
            </div>
          </div>
        ))}
      </div>

      <div className="actions-bar">
        <button 
          onClick={handleRegenerateAll} 
          className="regenerate-all-button"
          disabled={loading}
        >
          {loading ? '🔄 Regenerating...' : '🔄 Regenerate All (Keep Favorites)'}
        </button>
      </div>
    </div>
  );
}

export default Recommendations;