import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Student API calls
export const getAllStudents = () => api.get('/students');
export const getStudentById = (id) => api.get(`/students/${id}`);
export const getStudentPerformance = (id) => api.get(`/performance/${id}`);

// Project API calls
export const getAllProjects = () => api.get('/projects');
export const getProjectById = (id) => api.get(`/projects/${id}`);

// Recommendation API calls
export const getRecommendations = (studentId) => api.get(`/recommendations/${studentId}`);

export default api;