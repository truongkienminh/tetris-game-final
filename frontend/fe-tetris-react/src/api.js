import axios from "axios";

const API = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  // withCredentials: true, // nếu dùng JWT header thì bỏ
});

// JWT interceptor
API.interceptors.request.use(config => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Xử lý lỗi 401
API.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

// API
export const registerUser = (username, password) =>
  API.post("/auth/register", { username, password });

export const loginUser = (username, password) =>
  API.post("/auth/login", { username, password });

export const getCurrentUser = () =>
  API.get("/auth/me");

export default API;
