import React, { useState, useEffect } from 'react';
import './PreferencesPage.css';

function PreferencesPage({ studentId, onGenerateRecommendations, onBack }) {
  const [favorites, setFavorites] = useState([]);
  const [preferences, setPreferences] = useState({
    interests: '',
    preferredDifficulty: '',
    avoidTopics: '',
    additionalNotes: ''
  });
  const [isSaved, setIsSaved] = useState(false);

  // Scroll to top when component mounts
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  // Load favorites and preferences on mount
  useEffect(() => {
    loadFavorites();
    loadPreferences();
  }, [studentId]);

  const loadFavorites = () => {
    const savedFavorites = localStorage.getItem(`favorites_${studentId}`);
    console.log('Raw favorites from localStorage:', savedFavorites);
    
    if (savedFavorites) {
      try {
        const favoriteProjects = JSON.parse(savedFavorites);
        console.log('Parsed favorites:', favoriteProjects);
        
        // Ensure it's an array
        if (Array.isArray(favoriteProjects) && favoriteProjects.length > 0) {
          // Check if it's an array of objects (not just IDs)
          if (typeof favoriteProjects[0] === 'object' && favoriteProjects[0].projectTitle) {
            console.log('Setting favorites (objects):', favoriteProjects);
            setFavorites(favoriteProjects);
          } else if (typeof favoriteProjects[0] === 'number') {
            // If it's just IDs, try to get full details from all_recommendations
            console.log('Favorites are just IDs, fetching full details...');
            const allRecs = localStorage.getItem(`all_recommendations_${studentId}`);
            if (allRecs) {
              const allProjects = JSON.parse(allRecs);
              const favoriteDetails = allProjects.filter(proj => 
                favoriteProjects.includes(proj.projectId)
              );
              console.log('Loaded favorite details:', favoriteDetails);
              setFavorites(favoriteDetails);
            }
          }
        }
      } catch (e) {
        console.error('Failed to load favorites:', e);
      }
    } else {
      console.log('No favorites found');
    }
  };

  const loadPreferences = () => {
    const savedPrefs = localStorage.getItem(`preferences_${studentId}`);
    if (savedPrefs) {
      try {
        setPreferences(JSON.parse(savedPrefs));
      } catch (e) {
        console.error('Failed to load preferences:', e);
      }
    }
  };

  const handlePreferenceChange = (field, value) => {
    setPreferences(prev => ({
      ...prev,
      [field]: value
    }));
    setIsSaved(false);
  };

  const handleSavePreferences = () => {
    localStorage.setItem(`preferences_${studentId}`, JSON.stringify(preferences));
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 3000);
  };

  const handleRemoveFavorite = (projectId) => {
    const savedFavorites = localStorage.getItem(`favorites_${studentId}`);
    if (savedFavorites) {
      const favoriteProjects = JSON.parse(savedFavorites);
      const updated = favoriteProjects.filter(fav => fav.projectId !== projectId);
      localStorage.setItem(`favorites_${studentId}`, JSON.stringify(updated));
      setFavorites(updated);
    }
  };

  const handleClearAllFavorites = () => {
    if (window.confirm('Are you sure you want to clear all favorites?')) {
      localStorage.setItem(`favorites_${studentId}`, JSON.stringify([]));
      setFavorites([]);
    }
  };

  return (
    <div className="preferences-page">
      <div className="preferences-header">
        <button onClick={onBack} className="back-button">
          ← Back to Dashboard
        </button>
        <h1>Preferences & Favorites</h1>
        <p className="subtitle">Customize your capstone project recommendations</p>
      </div>

      <div className="preferences-container">
        {/* Favorites Section */}
        <div className="section favorites-display-section">
          <div className="section-header">
            <h2>⭐ Your Favorite Projects ({favorites.length})</h2>
            {favorites.length > 0 && (
              <button onClick={handleClearAllFavorites} className="clear-all-button">
                Clear All
              </button>
            )}
          </div>

          {favorites.length === 0 ? (
            <div className="empty-state">
              <p>🔖 You haven't favorited any projects yet.</p>
              <p className="hint">Generate recommendations and star your favorites to see them here!</p>
            </div>
          ) : (
            <div className="favorites-grid">
              {favorites.map((fav) => (
                <div key={fav.projectId} className="favorite-card">
                  <div className="favorite-header">
                    <h3>{fav.projectTitle}</h3>
                    <button
                      onClick={() => handleRemoveFavorite(fav.projectId)}
                      className="remove-favorite-button"
                      title="Remove from favorites"
                    >
                      ✕
                    </button>
                  </div>
                  <div className="favorite-body">
                    <span className="match-score">{(fav.score * 100).toFixed(0)}% match</span>
                    <p className="favorite-reason">{fav.reason}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Preferences Section */}
        <div className="section preferences-form-section">
          <h2>🎯 Set Your Preferences</h2>
          <p className="section-description">
            Help the AI understand what you're looking for in a capstone project
          </p>

          <div className="form-group">
            <label htmlFor="interests">
              <strong>Specific Interests or Technologies</strong>
              <span className="label-hint">e.g., Machine Learning, Web Development, IoT</span>
            </label>
            <textarea
              id="interests"
              value={preferences.interests}
              onChange={(e) => handlePreferenceChange('interests', e.target.value)}
              placeholder="Enter topics, technologies, or areas you're passionate about..."
              rows="3"
            />
          </div>

          <div className="form-group">
            <label htmlFor="difficulty">
              <strong>Preferred Difficulty Level</strong>
            </label>
            <select
              id="difficulty"
              value={preferences.preferredDifficulty}
              onChange={(e) => handlePreferenceChange('preferredDifficulty', e.target.value)}
            >
              <option value="">No preference</option>
              <option value="Beginner">Beginner - Foundational projects</option>
              <option value="Intermediate">Intermediate - Moderate complexity</option>
              <option value="Advanced">Advanced - Challenging projects</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="avoidTopics">
              <strong>Topics to Avoid</strong>
              <span className="label-hint">Optional - projects you're not interested in</span>
            </label>
            <textarea
              id="avoidTopics"
              value={preferences.avoidTopics}
              onChange={(e) => handlePreferenceChange('avoidTopics', e.target.value)}
              placeholder="Enter topics or technologies you'd prefer to avoid..."
              rows="2"
            />
          </div>

          <div className="form-group">
            <label htmlFor="additionalNotes">
              <strong>Additional Notes</strong>
              <span className="label-hint">Any other preferences or requirements</span>
            </label>
            <textarea
              id="additionalNotes"
              value={preferences.additionalNotes}
              onChange={(e) => handlePreferenceChange('additionalNotes', e.target.value)}
              placeholder="E.g., 'I prefer team projects', 'Looking for industry-relevant topics'..."
              rows="3"
            />
          </div>

          <div className="form-actions">
            <button onClick={handleSavePreferences} className="save-preferences-button">
              Save Preferences
            </button>
            {isSaved && <span className="saved-indicator">✓ Saved!</span>}
          </div>
        </div>

        {/* Generate Recommendations CTA */}
        <div className="section generate-section">
          <h2>🚀 Ready to Find Your Perfect Project?</h2>
          <p>
            Based on your academic performance, saved preferences, and AI analysis, 
            we'll recommend the most suitable capstone projects for you.
          </p>
          <button 
            onClick={() => onGenerateRecommendations(preferences)}
            className="generate-recommendations-button"
          >
            Generate AI Recommendations →
          </button>
        </div>
      </div>
    </div>
  );
}

export default PreferencesPage;