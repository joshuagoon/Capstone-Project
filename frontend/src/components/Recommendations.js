import React, { useState, useEffect, useRef } from 'react';
import './Recommendations.css';
import { getAIRecommendations } from '../services/api';
import SkillGapAnalysis from './SkillGapAnalysis';

// Helper function to extract skills from project
const extractSkillsFromProject = (projectTitle, projectReason) => {
  const skillKeywords = {
    'artificial intelligence': 'Artificial Intelligence',
    'machine learning': 'Artificial Intelligence',
    'ai': 'Artificial Intelligence',
    'ml': 'Artificial Intelligence',
    'deep learning': 'Artificial Intelligence',
    'neural': 'Artificial Intelligence',
    
    'web development': 'Web Development',
    'web': 'Web Development',
    'frontend': 'Web Development',
    'backend': 'Web Development',
    'react': 'Web Development',
    'html': 'Web Development',
    'javascript': 'Web Development',
    
    'mobile': 'Mobile Development',
    'android': 'Mobile Development',
    'ios': 'Mobile Development',
    'app': 'Mobile Development',
    
    'database': 'Database',
    'sql': 'Database',
    'data management': 'Database',
    
    'network': 'Networks',
    'networking': 'Networks',
    'distributed': 'Networks',
    'communication': 'Networks',
    
    'security': 'Cybersecurity',
    'cyber': 'Cybersecurity',
    'cryptography': 'Cybersecurity',
    
    'cloud': 'Cloud Computing',
    'aws': 'Cloud Computing',
    'azure': 'Cloud Computing',
    
    'iot': 'IoT',
    'sensor': 'IoT',
    'embedded': 'IoT',
    
    'data science': 'Data Analysis',
    'analytics': 'Data Analysis',
    'data analysis': 'Data Analysis',
    
    'programming': 'Programming',
    'software': 'Software Engineering',
    'blockchain': 'Blockchain'
  };

  const foundSkills = new Set();
  const textToSearch = (projectTitle + ' ' + projectReason).toLowerCase();

  Object.entries(skillKeywords).forEach(([keyword, skillName]) => {
    if (textToSearch.includes(keyword)) {
      foundSkills.add(skillName);
    }
  });

  const skillsArray = Array.from(foundSkills).slice(0, 4);
  
  if (skillsArray.length === 0) {
    return 'Programming,Software Engineering';
  }

  return skillsArray.join(',');
};

const extractDifficultyFromProject = (projectReason) => {
  const text = projectReason.toLowerCase();
  
  if (text.includes('advanced') || text.includes('complex') || text.includes('challenging') || text.includes('sophisticated')) {
    return 'Advanced';
  } else if (text.includes('beginner') || text.includes('simple') || text.includes('basic') || text.includes('foundational')) {
    return 'Beginner';
  } else {
    return 'Intermediate';
  }
};

function Recommendations({ studentId, userPreferences, onBack }) {
  const [allRecommendations, setAllRecommendations] = useState([]);
  const [displayedRecommendations, setDisplayedRecommendations] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(false);
  const [regeneratingIndex, setRegeneratingIndex] = useState(null);
  const [error, setError] = useState(null);
  
  // Skill Gap Modal state
  const [showSkillGap, setShowSkillGap] = useState(false);
  const [selectedProject, setSelectedProject] = useState(null);
  
  const hasFetchedRef = useRef(false);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    loadFavorites();
  }, [studentId]);

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
    localStorage.setItem(`favorites_${studentId}`, JSON.stringify(newFavorites));
    localStorage.setItem(`all_recommendations_${studentId}`, JSON.stringify(newFavorites));
  };

  const fetchRecommendations = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await getAIRecommendations(studentId, [], userPreferences);
      
      setAllRecommendations(response.data);
      setDisplayedRecommendations(response.data);
      
      const currentRecs = response.data.map(rec => ({
        projectId: rec.projectId,
        projectTitle: rec.projectTitle,
        score: rec.score,
        reason: rec.reason
      }));
      
      const existingFavorites = favorites;
      const allProjects = [...existingFavorites, ...currentRecs];
      
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
      const newFavorites = favorites.filter(fav => fav.projectId !== rec.projectId);
      setFavorites(newFavorites);
      saveFavorites(newFavorites);
    } else {
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
      
      const excludeIds = displayedRecommendations.map(rec => rec.projectId);
      const response = await getAIRecommendations(studentId, excludeIds, userPreferences);
      
      if (response.data.length > 0) {
        const newDisplayed = [...displayedRecommendations];
        newDisplayed[index] = response.data[0];
        setDisplayedRecommendations(newDisplayed);
        
        setAllRecommendations([...allRecommendations, ...response.data]);
        
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
      
      const favoritedProjects = displayedRecommendations.filter(rec => 
        isFavorited(rec.projectId)
      );
      const excludeIds = favoritedProjects.map(rec => rec.projectId);
      
      const response = await getAIRecommendations(studentId, excludeIds, userPreferences);
      
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
                onClick={() => {
                  setSelectedProject(rec);
                  setShowSkillGap(true);
                }}
                className="skill-gap-button"
              >
                📊 Analyze Skill Gap
              </button>
              
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

      {/* Skill Gap Modal */}
      {showSkillGap && selectedProject && (
        <SkillGapAnalysis
          studentId={studentId}
          projectTitle={selectedProject.projectTitle}
          projectSkills={extractSkillsFromProject(
            selectedProject.projectTitle,
            selectedProject.reason
          )}
          projectDifficulty={extractDifficultyFromProject(selectedProject.reason)}
          onClose={() => {
            setShowSkillGap(false);
            setSelectedProject(null);
          }}
        />
      )}
    </div>
  );
}

export default Recommendations;