import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor: otomatis tambahkan JWT token ke setiap request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor: handle 401 (token expired/invalid)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ==================== AUTH API ====================
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
};

// ==================== CAR API ====================
export const carAPI = {
  getAll: (params) => api.get('/cars', { params }),
  getById: (id) => api.get(`/cars/${id}`),
  create: (data) => api.post('/admin/cars', data),
  update: (id, data) => api.put(`/admin/cars/${id}`, data),
  delete: (id) => api.delete(`/admin/cars/${id}`),
};

// ==================== USER API ====================
export const userAPI = {
  getAll: () => api.get('/admin/users'),
  getById: (id) => api.get(`/admin/users/${id}`),
  create: (data) => api.post('/admin/users', data),
  update: (id, data) => api.put(`/admin/users/${id}`, data),
  delete: (id) => api.delete(`/admin/users/${id}`),
  getProfile: () => api.get('/users/profile'),
};

// ==================== RENTAL API ====================
export const rentalAPI = {
  create: (data) => api.post('/rentals', data),
  getMyRentals: () => api.get('/rentals/my'),
  cancel: (id) => api.put(`/rentals/${id}/cancel`),
  getAll: () => api.get('/admin/rentals'),
  approve: (id) => api.put(`/admin/rentals/${id}/approve`),
  reject: (id) => api.put(`/admin/rentals/${id}/reject`),
  complete: (id) => api.put(`/admin/rentals/${id}/complete`),
};

// ==================== ACTIVITY LOG API ====================
export const logAPI = {
  getAll: () => api.get('/admin/logs'),
};

export default api;
