import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export const login = (studentId) => {
  return axios.post(`${API_BASE_URL}/login`, { studentId });
};

export const getStudentPerformance = (studentId) => {
  return axios.get(`${API_BASE_URL}/performance/${studentId}`);
};

export const getAIRecommendations = async (studentId, excludeIds = [], preferences = null) => {
  let url = `${API_BASE_URL}/recommendations/${studentId}`;
  const params = new URLSearchParams();
  
  if (excludeIds.length > 0) {
    params.append('exclude', excludeIds.join(','));
  }
  
  if (preferences) {
    if (preferences.interests) params.append('interests', preferences.interests);
    if (preferences.preferredDifficulty) params.append('difficulty', preferences.preferredDifficulty);
    if (preferences.avoidTopics) params.append('avoid', preferences.avoidTopics);
    if (preferences.additionalNotes) params.append('notes', preferences.additionalNotes);
  }
  
  if (params.toString()) {
    url += `?${params.toString()}`;
  }
  
  return axios.get(url);
};