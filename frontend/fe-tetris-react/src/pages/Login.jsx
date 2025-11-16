import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser, getCurrentUser } from "../api"; // import getCurrentUser
import "../css/Register.css";
import Logo from "../assets/logo.png";

export default function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async () => {
    if (!username || !password) {
      setError("Please enter username and password.");
      return;
    }

    try {
      setIsLoading(true);
      setError("");

      // 1️⃣ Login và lấy token
      const res = await loginUser(username, password);
      localStorage.setItem("token", res.data.token);

      // 2️⃣ Fetch lại user hiện tại bằng token mới
      const userRes = await getCurrentUser();
      const user = userRes.data;

      // 3️⃣ Cập nhật state và chuyển hướng
      onLogin(user);
      navigate("/mainmenu");
    } catch (err) {
      console.error(err);
      setError("Login failed. Please check your username/password.");
    } finally {
      setIsLoading(false);
    }
  };

  const goToRegister = () => navigate("/register");
  const handleKeyPress = (e) => e.key === "Enter" && handleLogin();

  return (
    <div className="register-container">
      <div className="register-bg-effect"></div>
      <div className="register-card">
        {/* Header */}
        <div className="register-header">
          <img src={Logo} alt="Logo" style={{ width: 80, marginBottom: 10 }} />
          <h1 className="register-title title-glow">Welcome Back</h1>
          <p className="register-subtitle">Login to continue</p>
          <div className="header-line"></div>
        </div>

        {/* Error */}
        {error && (
          <div className="error-box">
            <span className="error-icon">⚠</span>
            <p>{error}</p>
          </div>
        )}

        {/* Form */}
        <div className="register-form">
          <div className="input-group">
            <span className="input-icon">👤</span>
            <input
              type="text"
              placeholder="Username"
              className="form-input"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onKeyPress={handleKeyPress}
              autoComplete="username"
            />
            <span className="input-underline"></span>
          </div>

          <div className="input-group">
            <span className="input-icon">🔑</span>
            <input
              type="password"
              placeholder="Password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyPress={handleKeyPress}
              autoComplete="current-password"
            />
            <span className="input-underline"></span>
          </div>

          <button
            onClick={handleLogin}
            disabled={isLoading}
            className={`btn-register ${isLoading ? "loading" : ""}`}
          >
            {isLoading ? "Logging in..." : "Login"}
            <div className="btn-glow"></div>
          </button>
        </div>

        {/* Divider */}
        <div className="divider"><span>OR</span></div>

        <button onClick={goToRegister} className="btn-login">
          <span>Don’t have an account?</span>
          <span className="login-link">Create Account</span>
        </button>

        {/* Footer */}
        <div className="register-footer">
          <p>Secure login protected by Tetris System</p>
          <div className="security-indicator">
            <div className="security-dot"></div>
            <span>Encrypted Connection</span>
          </div>
        </div>
      </div>
    </div>
  );
}
