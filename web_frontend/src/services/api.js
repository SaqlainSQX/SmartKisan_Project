import axios from 'axios';

const API_BASE_URL = 'http://localhost:8000';

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Add a request interceptor to attach the JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Auth APIs
export const login = async (email, password) => {
  const formData = new URLSearchParams();
  formData.append('username', email); // backend expects 'username' field for email in OAuth2
  formData.append('password', password);
  
  const response = await api.post('/token', formData, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  });
  return response.data;
};

export const register = async (email, password) => {
  const formData = new FormData();
  formData.append('email', email);
  formData.append('password', password);
  
  const response = await api.post('/register/', formData);
  return response.data;
};

// Add other APIs as needed (disease, weather, chatbot)

export default api;
