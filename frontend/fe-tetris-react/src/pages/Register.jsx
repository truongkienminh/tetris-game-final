import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerUser } from "../api";
import "../css/Register.css";

export default function Register({ onRegister }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async () => {
    if (!username || !password) {
      setError("Username and password required");
      return;
    }
    
    setLoading(true);
    try {
      const res = await registerUser(username, password);
      localStorage.setItem("token", res.data.token);
      onRegister(res.data.user);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") handleRegister();
  };

  const goToLogin = () => {
    navigate("/login");
  };

  return (
    <div className="register-container">
      <div className="register-bg-effect"></div>
      
      <div className="register-card">
        <div className="register-header">
          <h1 className="register-title">
            <span className="title-glow">CREATE</span> ACCOUNT
          </h1>
          <p className="register-subtitle">Join the game now</p>
          <div className="header-line"></div>
        </div>

        <div className="register-form">
          <div className="input-group">
            <div className="input-icon">👤</div>
            <input
              type="text"
              placeholder="Enter your username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onKeyPress={handleKeyPress}
              className="form-input"
              disabled={loading}
            />
            <div className="input-underline"></div>
          </div>

          <div className="input-group">
            <div className="input-icon">🔐</div>
            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyPress={handleKeyPress}
              className="form-input"
              disabled={loading}
            />
            <div className="input-underline"></div>
          </div>

          {error && (
            <div className="error-box">
              <span className="error-icon">⚠️</span>
              <p>{error}</p>
            </div>
          )}

          <button 
            onClick={handleRegister} 
            className={`btn-register ${loading ? "loading" : ""}`}
            disabled={loading}
          >
            <span className="btn-text">
              {loading ? "INITIALIZING..." : "REGISTER"}
            </span>
            <span className="btn-glow"></span>
          </button>

          <div className="divider">
            <span>OR</span>
          </div>

          <button 
            onClick={goToLogin} 
            className="btn-login"
            disabled={loading}
          >
            <span>Already have an account?</span>
            <span className="login-link">LOGIN HERE</span>
          </button>
        </div>

        <div className="register-footer">
          <p>Secure encrypted connection</p>
          <div className="security-indicator">
            <div className="security-dot"></div>
            <span>PROTECTED</span>
          </div>
        </div>
      </div>
    </div>
  );
}