// Lobby.jsx
import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";

// Axios instance cho Room API
const ROOM_API = axios.create({
  baseURL: "http://localhost:8080/api/rooms",
});

// Thêm JWT tự động
ROOM_API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default function Lobby({ currentUser }) {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);

  // Lấy thông tin phòng và danh sách người chơi
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
    // Refresh danh sách player mỗi 3s
    const interval = setInterval(fetchRoom, 3000);
    return () => clearInterval(interval);
  }, [roomId]);

  // Rời phòng
  const handleLeave = async () => {
    try {
      await ROOM_API.post(`/${roomId}/leave`);
      navigate("/rooms");
    } catch (err) {
      console.error("Error leaving room:", err);
    }
  };

  if (loading) return <p>Loading lobby...</p>;
  if (!room) return <p>Room not found.</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h1>Lobby: {room.roomName}</h1>

      {/* Hiển thị host */}
      {room.hostUsername}

      {/* Danh sách người chơi */}
      <h3>Players ({room.players?.length || 0}):</h3>
      <ul>
        {room.players?.map((p) => (
          <li key={p.id}>
            {p.username} {room.host?.id === p.id ? "(Host)" : ""}
          </li>
        ))}
      </ul>

      <button onClick={handleLeave} style={{ marginTop: "20px" }}>
        Leave Room
      </button>
    </div>
  );
}
