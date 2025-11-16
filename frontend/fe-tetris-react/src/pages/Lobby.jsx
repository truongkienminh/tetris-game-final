// Lobby.jsx
import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

// Axios cho Room API
const ROOM_API = axios.create({
  baseURL: "http://localhost:8080/api/rooms",
});
ROOM_API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Axios cho MultiGame API
const MULTIGAME_API = axios.create({
  baseURL: "http://localhost:8080/api/multigame",
});
MULTIGAME_API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

const styles = `
  * { margin: 0; padding: 0; box-sizing: border-box; }
  .lobby-container { min-height: 100vh; background: linear-gradient(135deg, #0a0e27 0%, #1a1f4d 50%, #0f1838 100%); font-family: 'Arial', sans-serif; color: #00ff88; display: flex; align-items: center; justify-content: center; padding: 20px; overflow-x: hidden; }
  .lobby-wrapper { width: 100%; max-width: 900px; }
  .header { text-align: center; margin-bottom: 40px; animation: glow 2s ease-in-out infinite; }
  .header h1 { font-size: 48px; font-weight: bold; text-shadow: 0 0 20px #00ff88, 0 0 40px #00ff88; letter-spacing: 3px; text-transform: uppercase; margin-bottom: 10px; }
  .room-name { font-size: 24px; color: #ff00ff; text-shadow: 0 0 10px #ff00ff; letter-spacing: 2px; }
  @keyframes glow { 0%, 100% { text-shadow: 0 0 20px #00ff88, 0 0 40px #00ff88; } 50% { text-shadow: 0 0 30px #00ff88, 0 0 60px #00ff88, 0 0 80px #00ff88; } }
  .info-section { background: rgba(0, 255, 136, 0.05); border: 2px solid #00ff88; border-radius: 10px; padding: 20px; margin-bottom: 30px; box-shadow: 0 0 20px rgba(0, 255, 136, 0.2), inset 0 0 20px rgba(0, 255, 136, 0.05); backdrop-filter: blur(10px); }
  .host-info { display: flex; align-items: center; gap: 15px; padding: 15px; background: rgba(255,0,255,0.1); border-left: 4px solid #ff00ff; border-radius: 5px; }
  .host-label { font-size: 14px; font-weight: bold; color: #ff00ff; text-transform: uppercase; letter-spacing: 1px; }
  .host-name { font-size: 20px; color: #ff00ff; text-shadow: 0 0 10px #ff00ff; }
  .players-section { margin-top: 30px; }
  .players-header { font-size: 18px; font-weight: bold; color: #00ff88; text-transform: uppercase; letter-spacing: 2px; margin-bottom: 15px; display: flex; align-items: center; gap: 10px; }
  .player-count { display: inline-block; background: #00ff88; color: #0a0e27; padding: 5px 12px; border-radius: 20px; font-weight: bold; font-size: 14px; }
  .players-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 15px; }
  .player-card { background: rgba(0,255,136,0.08); border: 2px solid #00ff88; border-radius: 8px; padding: 15px; display: flex; align-items: center; gap: 10px; cursor: default; transition: all 0.3s ease; }
  .player-card:hover { background: rgba(0,255,136,0.15); box-shadow: 0 0 15px rgba(0,255,136,0.4); transform: translateY(-3px); }
  .player-avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, #00ff88, #00ccff); display: flex; align-items: center; justify-content: center; font-weight: bold; color: #0a0e27; font-size: 18px; }
  .player-info { flex: 1; }
  .player-name { font-size: 14px; font-weight: bold; color: #00ff88; }
  .player-badge { font-size: 11px; color: #ff00ff; font-weight: bold; text-transform: uppercase; letter-spacing: 1px; }
  .empty-state { text-align: center; padding: 40px; color: #888; font-style: italic; }
  .button-group { display: flex; gap: 15px; margin-top: 40px; justify-content: center; }
  .btn { padding: 12px 30px; font-size: 16px; font-weight: bold; border: none; border-radius: 8px; cursor: pointer; text-transform: uppercase; letter-spacing: 2px; transition: all 0.3s ease; box-shadow: 0 0 20px rgba(0,255,136,0.3); }
  .btn-leave { background: linear-gradient(135deg, #ff0055, #ff4488); color: white; border: 2px solid #ff0055; text-shadow: 0 0 10px #ff0055; }
  .btn-leave:hover { background: linear-gradient(135deg, #ff4488, #ff0055); box-shadow: 0 0 30px #ff0055; transform: scale(1.05); }
  .btn-leave:active { transform: scale(0.98); }
  .btn-start { background: linear-gradient(135deg, #00ff88, #00ccff); color: #0a0e27; border: 2px solid #00ff88; text-shadow: 0 0 10px #00ff88; }
  .btn-start:hover { background: linear-gradient(135deg, #00ccff, #00ff88); box-shadow: 0 0 30px #00ff88; transform: scale(1.05); }
  .btn-start:active { transform: scale(0.98); }
  .loading { display: flex; align-items: center; justify-content: center; min-height: 100vh; font-size: 24px; color: #00ff88; text-shadow: 0 0 20px #00ff88; animation: pulse 1.5s ease-in-out infinite; }
  @keyframes pulse { 0%,100% {opacity:1;} 50% {opacity:0.5;} }
  .error { text-align:center; padding:40px; font-size:18px; color:#ff0055; text-shadow:0 0 10px #ff0055; }
`;

export default function Lobby({ currentUser }) {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);

  // Lấy thông tin room
  const fetchRoom = async () => {
    try {
      const res = await ROOM_API.get(`/${roomId}`);
      setRoom(res.data);
      setLoading(false);
    } catch (err) {
      console.error("Error fetching room:", err);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRoom();
    const interval = setInterval(fetchRoom, 3000); // cập nhật lobby mỗi 3s
    return () => clearInterval(interval);
  }, [roomId]);

  // Check if current user is host
  const isHost = room && currentUser &&
    currentUser.username?.toLowerCase() === room.hostUsername?.toLowerCase();

  const handleLeave = async () => {
    try {
      await ROOM_API.post(`/${roomId}/leave`);
      navigate("/rooms");
    } catch (err) {
      console.error("Error leaving room:", err);
    }
  };

  const handleStartGame = async () => {
    if (!isHost) {
      alert("Only host can start the game!");
      return;
    }
    try {
      const res = await MULTIGAME_API.post(`/start/${roomId}`);
      const updatedRoom = res.data; // backend trả về room với roomStatus mới
      setRoom(updatedRoom);

      // Redirect ngay nếu game đã bắt đầu
      if (updatedRoom.roomStatus?.toUpperCase() === "PLAYING") {
        navigate(`/multigame/${roomId}`);
      }
    } catch (err) {
      console.error("Error starting game:", err);
      alert("Failed to start game.");
    }
  };

  // Redirect nếu room đã đang PLAYING (dành cho player khác)
  useEffect(() => {
    if (room?.roomStatus?.toUpperCase() === "PLAYING") {
      navigate(`/multigame/${roomId}`);
    }
  }, [room, navigate, roomId]);

  if (loading) {
    return (
      <>
        <style>{styles}</style>
        <div className="lobby-container">
          <div className="loading">Loading lobby...</div>
        </div>
      </>
    );
  }

  if (!room) {
    return (
      <>
        <style>{styles}</style>
        <div className="lobby-container">
          <div className="error">Room not found.</div>
        </div>
      </>
    );
  }

  return (
    <>
      <style>{styles}</style>
      <div className="lobby-container">
        <div className="lobby-wrapper">
          <div className="header">
            <h1>🎮 Tetris Battle</h1>
            <div className="room-name">Room: {room.roomName}</div>
          </div>

          <div className="info-section">
            <div className="host-info">
              <span className="host-label">👑 Host</span>
              <span className="host-name">{room.hostUsername}</span>
            </div>
          </div>

          <div className="info-section">
            <div className="players-section">
              <div className="players-header">
                <span>Players</span>
                <span className="player-count">{room.players?.length || 0}</span>
              </div>

              {room.players && room.players.length > 0 ? (
                <div className="players-list">
                  {room.players.map((p) => (
                    <div key={p.id} className="player-card">
                      <div className="player-avatar">{p.username.charAt(0).toUpperCase()}</div>
                      <div className="player-info">
                        <div className="player-name">{p.username}</div>
                        {room.hostUsername === p.username && <div className="player-badge">👑 Host</div>}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="empty-state">No players yet...</div>
              )}
            </div>
          </div>

          <div className="button-group">
            <button className="btn btn-leave" onClick={handleLeave}>Leave Room</button>
            {isHost && <button className="btn btn-start" onClick={handleStartGame}>Start Game</button>}
          </div>
        </div>
      </div>
    </>
  );
}