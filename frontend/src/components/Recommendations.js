import React, { useState, useEffect, useRef } from 'react';
import './Recommendations.css';
import { getAIRecommendations } from '../services/api';

function Recommendations({ studentId, userPreferences, onBack }) {
  const [allRecommendations, setAllRecommendations] = useState([]);
  const [displayedRecommendations, setDisplayedRecommendations] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(false);
  const [regeneratingIndex, setRegeneratingIndex] = useState(null);
  const [error, setError] = useState(null);
  
  // Use ref to prevent double fetching
  const hasFetchedRef = useRef(false);

  // Scroll to top when component mounts
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  // Load favorites from localStorage on mount
  useEffect(() => {
    loadFavorites();
  }, [studentId]);

  // Fetch recommendations on component mount (only once!)
  useEffect(() => {
    if (!hasFetchedRef.current) {
      hasFetchedRef.current = true;
      fetchRecommendations();
    }
  }, []);

  const loadFavorites = () => {
    const savedFavorites = localStorage.getItem(`favorites_${studentId}`);
    if (savedFavorites) {
      try {
        const favProjects = JSON.parse(savedFavorites);
        setFavorites(favProjects);
        console.log('Loaded favorites:', favProjects);
      } catch (e) {
        console.error('Failed to load favorites:', e);
      }
    }
  };

  const saveFavorites = (newFavorites) => {
    // Save favorites list (array of full project objects)
    localStorage.setItem(`favorites_${studentId}`, JSON.stringify(newFavorites));
    
    // Also save just the IDs for backward compatibility
    const favoriteIds = newFavorites.map(fav => fav.projectId);
    
    // Save complete project details for PreferencesPage to access
    localStorage.setItem(`all_recommendations_${studentId}`, JSON.stringify(newFavorites));
    
    console.log('Saved favorites:', newFavorites);
    console.log('Saved favorite IDs:', favoriteIds);
  };

  const fetchRecommendations = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAIRecommendations(studentId, [], userPreferences);
      
      // Store all recommendations
      setAllRecommendations(response.data);
      setDisplayedRecommendations(response.data);
      
      // Save to localStorage for favorites page access
      const currentRecs = response.data.map(rec => ({
        projectId: rec.projectId,
        projectTitle: rec.projectTitle,
        score: rec.score,
        reason: rec.reason
      }));
      
      // Merge with existing favorites
      const existingFavorites = favorites;
      const allProjects = [...existingFavorites, ...currentRecs];
      
      // Remove duplicates by projectId
      const uniqueProjects = allProjects.filter((proj, index, self) =>
        index === self.findIndex(p => p.projectId === proj.projectId)
      );
      
      localStorage.setItem(`all_recommendations_${studentId}`, JSON.stringify(uniqueProjects));
    } catch (err) {
      setError('Failed to load recommendations');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleFavorite = (rec) => {
    const isFavorited = favorites.some(fav => fav.projectId === rec.projectId);
    
    if (isFavorited) {
      // Remove from favorites
      const newFavorites = favorites.filter(fav => fav.projectId !== rec.projectId);
      setFavorites(newFavorites);
      saveFavorites(newFavorites);
    } else {
      // Add to favorites - save full project details
      const favoriteProject = {
        projectId: rec.projectId,
        projectTitle: rec.projectTitle,
        score: rec.score,
        reason: rec.reason
      };
      const newFavorites = [...favorites, favoriteProject];
      setFavorites(newFavorites);
      saveFavorites(newFavorites);
    }
  };

  const isFavorited = (projectId) => {
    return favorites.some(fav => fav.projectId === projectId);
  };

  const handleRegenerate = async (index) => {
    try {
      setRegeneratingIndex(index);
      setError(null);
      
      // Get all currently displayed project IDs to exclude
      const excludeIds = displayedRecommendations.map(rec => rec.projectId);
      
      // Fetch new recommendations excluding current ones
      const response = await getAIRecommendations(studentId, excludeIds, userPreferences);
      
      if (response.data.length > 0) {
        // Replace at index with first new recommendation
        const newDisplayed = [...displayedRecommendations];
        newDisplayed[index] = response.data[0];
        setDisplayedRecommendations(newDisplayed);
        
        // Add to pool
        setAllRecommendations([...allRecommendations, ...response.data]);
        
        // Update localStorage
        const allProjects = [...favorites, ...newDisplayed];
        const uniqueProjects = allProjects.filter((proj, idx, self) =>
          idx === self.findIndex(p => p.projectId === proj.projectId)
        );
        localStorage.setItem(`all_recommendations_${studentId}`, JSON.stringify(uniqueProjects));
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
      
      // Keep favorited projects
      const favoritedProjects = displayedRecommendations.filter(rec => 
        isFavorited(rec.projectId)
      );
      const excludeIds = favoritedProjects.map(rec => rec.projectId);
      
      // Get new recommendations
      const response = await getAIRecommendations(studentId, excludeIds, userPreferences);
      
      // Build new displayed list: keep favorites, add new recommendations
      const newDisplayed = [];
      let newRecIndex = 0;
      
      for (let i = 0; i < 3; i++) {
        if (i < displayedRecommendations.length && isFavorited(displayedRecommendations[i].projectId)) {
          newDisplayed.push(displayedRecommendations[i]);
        } else if (newRecIndex < response.data.length) {
          newDisplayed.push(response.data[newRecIndex]);
          newRecIndex++;
        }
      }
      
      setDisplayedRecommendations(newDisplayed);
      setAllRecommendations([...allRecommendations, ...response.data]);
      
      // Update localStorage
      const allProjects = [...favorites, ...newDisplayed];
      const uniqueProjects = allProjects.filter((proj, idx, self) =>
        idx === self.findIndex(p => p.projectId === proj.projectId)
      );
      localStorage.setItem(`all_recommendations_${studentId}`, JSON.stringify(uniqueProjects));
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
      localStorage.setItem(`favorites_${studentId}`, JSON.stringify([]));
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
          ← Back to Preferences
        </button>
        <h1>AI-Recommended Capstone Projects</h1>
        <p className="subtitle">Personalized recommendations based on your academic profile and preferences</p>
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
            className={`recommendation-card ${isFavorited(rec.projectId) ? 'favorited' : ''}`}
          >
            <div className="card-header">
              <div className="project-info">
                <h3>{rec.projectTitle}</h3>
                <span className="match-badge">
                  {(rec.score * 100).toFixed(0)}% match
                </span>
              </div>
              <button
                onClick={() => handleFavorite(rec)}
                className={`favorite-button ${isFavorited(rec.projectId) ? 'active' : ''}`}
                title={isFavorited(rec.projectId) ? 'Remove from favorites' : 'Add to favorites'}
              >
                {isFavorited(rec.projectId) ? '★' : '☆'}
              </button>
            </div>

            <div className="card-body">
              <p className="reason">{rec.reason}</p>
            </div>

            <div className="card-footer">
              <button
                onClick={() => handleRegenerate(index)}
                className="regenerate-button"
                disabled={regeneratingIndex === index || isFavorited(rec.projectId)}
                title={isFavorited(rec.projectId) ? 'Cannot regenerate favorited projects' : 'Get a different recommendation'}
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