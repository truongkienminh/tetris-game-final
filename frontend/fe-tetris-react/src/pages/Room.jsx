// Room.jsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import "../css/Room.css"; // Import CSS file

// Axios instance cho Room API
const ROOM_API = axios.create({
  baseURL: `${import.meta.env.VITE_API_URL}/rooms`,
});

// Thêm JWT tự động
ROOM_API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default function Room({ currentUser }) {
  const [rooms, setRooms] = useState([]);
  const [roomName, setRoomName] = useState("");
  const navigate = useNavigate();

  // 🎯 Lấy danh sách phòng
  const fetchRooms = async () => {
    try {
      const res = await ROOM_API.get("");
      setRooms(res.data);
    } catch (err) {
      console.error("Error fetching rooms:", err);
    }
  };

  useEffect(() => {
    fetchRooms();
    // Refresh danh sách phòng mỗi 5s
    const interval = setInterval(fetchRooms, 5000);
    return () => clearInterval(interval);
  }, []);

  // 🎯 Tạo phòng mới và join vào Lobby
  const handleCreateRoom = async () => {
  if (!roomName.trim()) return;

  try {
    // 1️⃣ Tạo phòng
    const res = await ROOM_API.post(`/create?roomName=${roomName}`);
    const newRoom = res.data;
    const newRoomId = newRoom.id;

    // 2️⃣ Chuyển hướng thẳng đến Lobby
    navigate(`/lobby/${newRoomId}`);
    setRoomName("");
  } catch (err) {
    console.error("Error creating room:", err.response || err);
  }
};

  // 🎯 Quay lại Main Menu
  const handleBackToMenu = () => {
    navigate("/");
  };

  // 🎯 Join room từ danh sách
  const handleJoinRoom = async (roomId) => {
    try {
      await ROOM_API.post(`/${roomId}/join`);
      navigate(`/lobby/${roomId}`);
    } catch (err) {
      console.error("Error joining room:", err.response || err);
    }
  };

  return (
    <div className="room-container">
      {/* Animated background */}
      <div className="bg-animation"></div>

      {/* Back Button */}
      <button onClick={handleBackToMenu} className="back-btn">
        <span className="back-icon">←</span> BACK
      </button>

      {/* Content */}
      <div className="room-content">
        {/* Header */}
        <div className="room-header">
          <div className="title-section">
            <span className="icon">⚡</span>
            <h1 className="room-title">MULTIPLAYER LOBBY</h1>
            <span className="icon">⚡</span>
          </div>
          <p className="room-subtitle">Join or Create Rooms & Battle Online</p>
        </div>

        {/* Create Room Section */}
        <div className="create-section">
          <div className="create-card">
            <h2 className="section-title">
              <span className="icon-inline">➕</span> CREATE NEW ROOM
            </h2>
            <div className="input-wrapper">
              <input
                type="text"
                value={roomName}
                onChange={(e) => setRoomName(e.target.value)}
                placeholder="Enter room name..."
                className="room-input"
                onKeyPress={(e) => e.key === "Enter" && handleCreateRoom()}
              />
              <button onClick={handleCreateRoom} className="create-btn">
                CREATE
              </button>
            </div>
          </div>
        </div>

        {/* Available Rooms Section */}
        <div className="rooms-section">
          <h2 className="section-title">
            <span className="icon-inline">👥</span> AVAILABLE ROOMS ({rooms.length})
          </h2>

          {rooms.length === 0 ? (
            <div className="empty-state">
              <p>No rooms available yet. Be the first to create one!</p>
            </div>
          ) : (
            <div className="rooms-grid">
              {rooms.map((room) => (
                <div key={room.id} className="room-card">
                  <div className="room-card-header">
                    <h3 className="room-name">{room.roomName}</h3>
                    <div className="badge">{room.players?.length || 0}</div>
                  </div>

                  <div className="room-info">
                    <div className="info-row">
                      <span className="info-label">HOST:</span>
                      <span className="info-value">{room.hostUsername}</span>
                    </div>
                    <div className="info-row">
                      <span className="info-label">PLAYERS:</span>
                      <span className="info-value">{room.players?.length || 0}</span>
                    </div>
                  </div>

                  <button 
                    onClick={() => handleJoinRoom(room.id)} 
                    className="join-btn"
                  >
                    <span className="icon-inline">🚪</span> JOIN ROOM
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}