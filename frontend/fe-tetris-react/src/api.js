import axios from "axios";

// URL base backend
const API = axios.create({
  baseURL: "http://localhost:8080/api/auth",
});

// ✅ Add JWT token to header if available
API.interceptors.request.use(config => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ✅ Handle 401 responses (token expired or invalid)
API.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token is invalid or expired
      localStorage.removeItem("token");
      console.warn("⚠️ Unauthorized - Token cleared. Redirecting to login...");
      // Optionally redirect to login
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export const registerUser = (username, password) =>
  API.post("/register", { username, password });

export const loginUser = (username, password) =>
  API.post("/login", { username, password });

export const getCurrentUser = () =>
  API.get("/me");