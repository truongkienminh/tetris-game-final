import { useNavigate } from "react-router-dom";
import { useState } from "react";
import axios from "axios";
import "../css/MainMenu.css";

export default function MainMenu({ currentUser, onLogout }) {
  const navigate = useNavigate();
  const [hoveredBtn, setHoveredBtn] = useState(null);

  const goToSoloGame = () => {
    if (!currentUser) return;
    navigate(`/solo-game/${currentUser.id}`);
  };

   const handleLogout = async () => {
    try {
      const token = localStorage.getItem("token");
      if (token) {
        await axios.post(
          `${import.meta.env.VITE_API_URL}/auth/logout`,
          {},
          { headers: { Authorization: `Bearer ${token}` } }
        );
      }
    } catch (err) {
      console.error("Error logging out:", err);
    } finally {
      localStorage.removeItem("token");
      if (onLogout) onLogout(); // reset state ở App
      navigate("/login");
    }
  };

  const menuItems = [
    { id: 1, label: "Solo", icon: "🎮", onClick: goToSoloGame },
    { id: 2, label: "Multiplayer", icon: "👥", onClick: () => navigate("/rooms") },
    { id: 3, label: "Profile", icon: "👤", onClick: () => navigate("/profile") },
  ];

  return (
    <div className="mainmenu-page">
      <div className="blob blob-1"></div>
      <div className="blob blob-2"></div>
      <div className="blob blob-3"></div>
      <div className="grid-overlay"></div>

      <div className="mainmenu-container">
        <div className="header-section">
          <div className="logo-box">
            <span className="logo-icon">🎮</span>
          </div>
          <h1 className="main-title">TETRIS</h1>
          <p className="subtitle">MASTER EDITION</p>
          {currentUser && (
            <p className="user-greeting">Welcome, {currentUser.username || "Player"}</p>
          )}
        </div>

        <div className="menu-buttons">
          {menuItems.map((item) => (
            <button
              key={item.id}
              className={`menu-btn enhanced-btn ${hoveredBtn === item.id ? "active" : ""}`}
              onClick={item.onClick}
              onMouseEnter={() => setHoveredBtn(item.id)}
              onMouseLeave={() => setHoveredBtn(null)}
            >
              <span className="btn-icon">{item.icon}</span>
              <span className="btn-label">{item.label}</span>
              <span className="btn-arrow">→</span>
            </button>
          ))}
        </div>

        {/* Logout Button */}
        <button
          className="menu-btn logout-btn enhanced-btn"
          onClick={handleLogout}
          onMouseEnter={() => setHoveredBtn("logout")}
          onMouseLeave={() => setHoveredBtn(null)}
        >
          <span className="btn-icon">🚪</span>
          <span className="btn-label">Logout</span>
        </button>

        <div className="stats-footer">
          <div className="stat-item">
            <span className="stat-value">∞</span>
            <span className="stat-label">Levels</span>
          </div>
          <div className="stat-divider"></div>
          <div className="stat-item">
            <span className="stat-value">HD</span>
            <span className="stat-label">Quality</span>
          </div>
          <div className="stat-divider"></div>
          <div className="stat-item">
            <span className="stat-value">4K</span>
            <span className="stat-label">Graphics</span>
          </div>
        </div>
      </div>
    </div>
  );
}