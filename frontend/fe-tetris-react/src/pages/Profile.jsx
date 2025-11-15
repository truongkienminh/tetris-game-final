import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCurrentUser } from "../api";

const styles = `
  @keyframes slideIn {
    from {
      opacity: 0;
      transform: translateY(30px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }

  @keyframes borderGlow {
    0%, 100% {
      box-shadow: inset 0 0 20px rgba(59, 130, 246, 0.1), 0 0 20px rgba(59, 130, 246, 0.2);
      border-color: rgba(59, 130, 246, 0.3);
    }
    50% {
      box-shadow: inset 0 0 30px rgba(59, 130, 246, 0.2), 0 0 40px rgba(59, 130, 246, 0.4);
      border-color: rgba(59, 130, 246, 0.5);
    }
  }

  @keyframes slideFromLeft {
    from {
      opacity: 0;
      transform: translateX(-30px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  @keyframes slideFromRight {
    from {
      opacity: 0;
      transform: translateX(30px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
  }

  .profile-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #0f172a 0%, #1a1f35 50%, #0f172a 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    position: relative;
    overflow: hidden;
  }

  .profile-container::before {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(59, 130, 246, 0.1) 0%, transparent 70%);
    border-radius: 50%;
    top: -200px;
    right: -200px;
    pointer-events: none;
  }

  .profile-container::after {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(96, 165, 250, 0.08) 0%, transparent 70%);
    border-radius: 50%;
    bottom: -200px;
    left: -200px;
    pointer-events: none;
  }

  .profile-wrapper {
    width: 100%;
    max-width: 520px;
    position: relative;
    z-index: 1;
    animation: slideIn 0.7s cubic-bezier(0.16, 1, 0.3, 1);
  }

  .header {
    text-align: center;
    margin-bottom: 50px;
    animation: slideIn 0.7s ease-out;
  }

  .title {
    font-size: 40px;
    font-weight: 800;
    background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 3px;
    margin-bottom: 12px;
    text-shadow: 0 0 30px rgba(59, 130, 246, 0.1);
  }

  .subtitle {
    font-size: 13px;
    color: rgba(148, 163, 184, 0.7);
    letter-spacing: 1px;
    text-transform: uppercase;
  }

  .divider {
    height: 3px;
    width: 60px;
    background: linear-gradient(90deg, transparent, #3b82f6, transparent);
    margin: 0 auto;
    margin-top: 16px;
    border-radius: 2px;
  }

  .card {
    background: linear-gradient(135deg, rgba(15, 23, 42, 0.7) 0%, rgba(30, 41, 59, 0.7) 100%);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(59, 130, 246, 0.3);
    border-radius: 16px;
    padding: 40px;
    margin-bottom: 28px;
    animation: borderGlow 3s ease-in-out infinite;
    position: relative;
    overflow: hidden;
  }

  .card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.5), transparent);
  }

  .field {
    margin-bottom: 28px;
    animation: slideIn 0.7s ease-out;
  }

  .field:last-child {
    margin-bottom: 0;
  }

  .field:nth-child(1) {
    animation-delay: 0.1s;
  }

  .field:nth-child(2) {
    animation-delay: 0.2s;
  }

  .field:nth-child(3) {
    animation-delay: 0.3s;
  }

  .field-wrapper {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .field-icon {
    width: 32px;
    height: 32px;
    background: rgba(59, 130, 246, 0.15);
    border: 1px solid rgba(59, 130, 246, 0.3);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #60a5fa;
    flex-shrink: 0;
  }

  .field-content {
    flex: 1;
  }

  .label {
    font-size: 11px;
    font-weight: 700;
    color: rgba(148, 163, 184, 0.6);
    text-transform: uppercase;
    letter-spacing: 1.2px;
    margin-bottom: 6px;
    display: block;
  }

  .value {
    font-size: 15px;
    color: #e2e8f0;
    font-weight: 500;
    font-family: 'SF Mono', 'Monaco', 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .field:hover .value {
    color: #3b82f6;
  }

  .score-badge {
    display: inline-block;
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(96, 165, 250, 0.1));
    border: 1px solid rgba(59, 130, 246, 0.3);
    padding: 8px 16px;
    border-radius: 8px;
    font-size: 20px;
    font-weight: 700;
    color: #60a5fa;
    transition: all 0.3s ease;
  }

  .field:hover .score-badge {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.25), rgba(96, 165, 250, 0.2));
    border-color: rgba(59, 130, 246, 0.6);
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.2);
  }

  .button {
    width: 100%;
    padding: 14px 28px;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    border: 1px solid rgba(59, 130, 246, 0.4);
    border-radius: 12px;
    font-size: 13px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 1.5px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: slideIn 0.7s ease-out 0.4s both;
    position: relative;
    overflow: hidden;
  }

  .button::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left 0.5s ease;
  }

  .button:hover::before {
    left: 100%;
  }

  .button:hover {
    background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
    box-shadow: 0 15px 40px rgba(59, 130, 246, 0.3);
    transform: translateY(-2px);
  }

  .button:active {
    transform: translateY(0);
  }

  .loading-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #0f172a 0%, #1a1f35 50%, #0f172a 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    gap: 24px;
  }

  .spinner {
    width: 50px;
    height: 50px;
    border: 2px solid rgba(59, 130, 246, 0.2);
    border-top: 2px solid #3b82f6;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  @keyframes spin {
    to { transform: rotate(360deg); }
  }

  .loading-text {
    color: #60a5fa;
    font-size: 14px;
    font-weight: 500;
    letter-spacing: 0.5px;
  }

  .error-container {
    min-height: 100vh;
    background: linear-gradient(135deg, #0f172a 0%, #1a1f35 50%, #0f172a 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
  }

  .error-box {
    background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(220, 38, 38, 0.05));
    border: 1px solid rgba(239, 68, 68, 0.3);
    border-radius: 12px;
    padding: 28px;
    max-width: 400px;
    text-align: center;
    animation: slideIn 0.6s ease-out;
  }

  .error-text {
    color: #fca5a5;
    font-size: 15px;
    font-weight: 500;
    letter-spacing: 0.3px;
  }
`;

export default function Profile() {
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await getCurrentUser();
        setUser(res.data);
      } catch (err) {
        setError("Cannot fetch profile. Please login.");
        console.error(err);
      }
    };
    fetchUser();
  }, []);

  if (error) {
    return (
      <>
        <style>{styles}</style>
        <div className="error-container">
          <div className="error-box">
            <p className="error-text">{error}</p>
          </div>
        </div>
      </>
    );
  }

  if (!user) {
    return (
      <>
        <style>{styles}</style>
        <div className="loading-container">
          <div className="spinner"></div>
          <p className="loading-text">Loading profile...</p>
        </div>
      </>
    );
  }

  return (
    <>
      <style>{styles}</style>
      <div className="profile-container">
        <div className="profile-wrapper">
          <div className="header">
            <h1 className="title">PROFILE</h1>
            <p className="subtitle">User Information</p>
            <div className="divider"></div>
          </div>

          <div className="card">
            <div className="field">
              <div className="field-wrapper">
                <div className="field-icon">👤</div>
                <div className="field-content">
                  <label className="label">User ID</label>
                  <div className="value">{user.id}</div>
                </div>
              </div>
            </div>

            <div className="field">
              <div className="field-wrapper">
                <div className="field-icon">✓</div>
                <div className="field-content">
                  <label className="label">Username</label>
                  <div className="value">{user.username}</div>
                </div>
              </div>
            </div>

            <div className="field">
              <div className="field-wrapper">
                <div className="field-icon">⭐</div>
                <div className="field-content">
                  <label className="label">Last Score</label>
                  <div className="score-badge">{user.lastScore }</div>
                </div>
              </div>
            </div>
          </div>

          <button onClick={() => navigate("/mainmenu")} className="button">
            Back to Main Menu
          </button>
        </div>
      </div>
    </>
  );
}