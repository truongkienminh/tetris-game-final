import axios from "axios";

// Base URL từ biến môi trường
const API = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true, // dùng nếu backend trả cookie
});

// Thêm JWT token vào header
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

export const registerUser = (username, password) =>
  API.post("/register", { username, password });

export const loginUser = (username, password) =>
  API.post("/login", { username, password });

export const getCurrentUser = () =>
  API.get("/me");
