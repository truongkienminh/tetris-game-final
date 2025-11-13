// Room.jsx
import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

// Axios instance cho Room API
const ROOM_API = axios.create({
  baseURL: "http://localhost:8080/api/rooms",
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

  // 🔹 Lấy danh sách phòng
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

  // 🔹 Tạo phòng mới và join vào Lobby
  const handleCreateRoom = async () => {
    if (!roomName.trim()) return;
    try {
      // 1️⃣ Tạo phòng
      const res = await ROOM_API.post(`/create?roomName=${roomName}`);
      const newRoom = res.data;
      const newRoomId = newRoom.id;

      // 2️⃣ Tự động join phòng vừa tạo
      await ROOM_API.post(`/${newRoomId}/join`);

      // 3️⃣ Chuyển hướng đến Lobby
      navigate(`/lobby/${newRoomId}`);
    } catch (err) {
      console.error("Error creating/joining room:", err.response || err);
    }
  };

  // 🔹 Join room từ danh sách
  const handleJoinRoom = async (roomId) => {
    try {
      await ROOM_API.post(`/${roomId}/join`);
      navigate(`/lobby/${roomId}`);
    } catch (err) {
      console.error("Error joining room:", err.response || err);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h1>Multiplayer Lobby</h1>

      {/* Tạo phòng mới */}
      <div style={{ marginBottom: "20px" }}>
        <input
          type="text"
          value={roomName}
          onChange={(e) => setRoomName(e.target.value)}
          placeholder="Enter room name"
        />
        <button onClick={handleCreateRoom}>Create Room</button>
      </div>

      {/* Danh sách phòng */}
      <div>
        <h2>Available Rooms</h2>
        {rooms.length === 0 && <p>No rooms available</p>}
        <ul>
          {rooms.map((room) => (
            <li key={room.id} style={{ marginBottom: "10px" }}>
              <strong>Room Name:</strong> {room.roomName} <br />
              <strong>Host:</strong> {room.hostUsername} <br />
              <strong>Players:</strong> {room.players?.length || 0} <br />
              <div style={{ marginTop: "5px" }}>
                <button onClick={() => handleJoinRoom(room.id)}>Join</button>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
