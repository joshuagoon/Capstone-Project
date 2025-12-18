import axios from 'axios';

const API_URL = '/api';

export const login = async (studentId) => {
  try {
    const response = await axios.post(`${API_URL}/login`, { studentId });
    return response;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

export const getStudentPerformance = async (studentId) => {
  try {
    const response = await axios.get(`${API_URL}/performance/${studentId}`);
    return response;
  } catch (error) {
    console.error('Performance fetch error:', error);
    throw error;
  }
};

export const getAIRecommendations = async (studentId, excludeIds = [], preferences = {}) => {
  try {
    const params = {
      exclude: excludeIds.join(',')
    };

    // Add preferences if provided
    if (preferences.interests) params.interests = preferences.interests;
    if (preferences.preferredDifficulty) params.difficulty = preferences.preferredDifficulty;
    if (preferences.avoidTopics) params.avoid = preferences.avoidTopics;
    if (preferences.additionalNotes) params.notes = preferences.additionalNotes;

    const response = await axios.get(`${API_URL}/recommendations/${studentId}`, { params });
    return response;
  } catch (error) {
    console.error('Recommendations fetch error:', error);
    throw error;
  }
};

export const analyseSkillGap = async (studentId, projectSkills, projectTitle, projectDifficulty) => {
  try {
    const response = await axios.get(`${API_URL}/skill-gap/${studentId}`, {
      params: {
        projectTitle: projectTitle,
        projectSkills: projectSkills,
        projectDifficulty: projectDifficulty
      }
    });
    return response;
  } catch (error) {
    console.error('Skill gap analysis error:', error);
    console.error('Error response:', error.response?.data);
    throw error;
  }
};
