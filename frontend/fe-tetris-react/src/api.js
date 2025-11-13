import axios from "axios";

// URL base backend
const API = axios.create({
  baseURL: "http://localhost:8080/api/auth",
});

// Thêm token JWT vào header nếu có
API.interceptors.request.use(config => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const registerUser = (username, password) =>
  API.post("/register", { username, password });

export const loginUser = (username, password) =>
  API.post("/login", { username, password });

export const getCurrentUser = () =>
  API.get("/me");
