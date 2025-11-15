// MultiGame.jsx
import { useState, useEffect, useCallback, useRef } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";
import "../css/SoloGame.css"; // dùng chung CSS với SoloGame

const BLOCK_COLORS = {
  I: "cyan",
  O: "yellow",
  T: "purple",
  S: "lime",
  Z: "red",
  J: "blue",
  L: "orange",
  0: "#111"
};

const COLOR_MAP = {
  0: "0",
  1: "I",
  2: "O",
  3: "T",
  4: "S",
  5: "Z",
  6: "J",
  7: "L"
};

const BLOCK_SHAPES = {
  I: [
    [0,1,0,0],
    [0,1,0,0],
    [0,1,0,0],
    [0,1,0,0]
  ],
  O: [
    [1,1],
    [1,1]
  ],
  T: [
    [0,1,0],
    [1,1,1],
    [0,0,0]
  ],
  S: [
    [0,1,1],
    [1,1,0],
    [0,0,0]
  ],
  Z: [
    [1,1,0],
    [0,1,1],
    [0,0,0]
  ],
  J: [
    [1,0,0],
    [1,1,1],
    [0,0,0]
  ],
  L: [
    [0,0,1],
    [1,1,1],
    [0,0,0]
  ]
};

export default function MultiGame({ currentUser }) {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const [states, setStates] = useState({});
  const [error, setError] = useState("");
  const intervalRef = useRef(null);

  const API = axios.create({
    baseURL: "http://localhost:8080/api/multigame",
    headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
  });

  // Tìm playerId của currentUser
  const getPlayerId = () => {
    const entry = Object.entries(states).find(([id, s]) => s.username === currentUser.username);
    return entry ? Number(entry[0]) : null;
  };

  const playerId = getPlayerId();

  // Polling trạng thái game cho toàn bộ phòng
  const startPolling = () => {
    stopPolling();
    intervalRef.current = setInterval(async () => {
      try {
        const res = await API.get(`/room/${roomId}/states`);
        setStates(res.data);
      } catch (err) {
        console.error("Fetch states error:", err);
      }
    }, 500);
  };

  const stopPolling = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  };

  useEffect(() => {
    startPolling();
    return stopPolling;
  }, [roomId]);

  // Gửi hành động
  const sendAction = async (action) => {
    if (!playerId || !states[playerId] || states[playerId].status === "GAME_OVER") return;
    try {
      const res = await API.post(`/player/${playerId}/${action}`);
      setStates((prev) => ({ ...prev, [playerId]: res.data }));
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || "Action failed");
    }
  };

  // Xử lý phím
  const handleKeyDown = useCallback(
    (e) => {
      if (!playerId || states[playerId]?.status === "GAME_OVER") return;
      if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", " "].includes(e.key)) e.preventDefault();
      switch (e.key) {
        case "ArrowLeft": sendAction("moveLeft"); break;
        case "ArrowRight": sendAction("moveRight"); break;
        case "ArrowUp": sendAction("rotate"); break;
        case "ArrowDown": sendAction("tick"); break;
        case " ": sendAction("drop"); break;
        default: break;
      }
    },
    [playerId, states]
  );

  useEffect(() => {
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [handleKeyDown]);

  if (error) return <p style={{ color: "red", textAlign: "center" }}>{error}</p>;
  if (!states || Object.keys(states).length === 0) return <p style={{ textAlign: "center", color: "white" }}>Loading game...</p>;

  return (
    <div className="multi-game-container" style={{ padding: "20px", color: "white" }}>
      <h2>🎮 Multi Tetris Room: {roomId}</h2>

      <div className="multi-game-players" style={{ display: "flex", gap: "20px", flexWrap: "wrap" }}>
        {Object.entries(states).map(([id, state]) => {
          const isCurrent = Number(id) === playerId;
          const isGameOver = state.status === "GAME_OVER";

          return (
            <div key={id} className={`multi-player-board ${isGameOver ? "game-over" : ""}`} style={{ border: "1px solid #00ff88", padding: "10px", borderRadius: "8px", minWidth: "220px" }}>
              <h3 style={{ textAlign: "center", margin: "0 0 10px 0", color: isCurrent ? "#00ff88" : "#fff" }}>
                {state.username} {isCurrent ? "(You)" : ""} {isGameOver ? "💀" : ""}
              </h3>
              <div className="stat-card">
                <div className="stat-label">Score</div>
                <div className="stat-value">{state.score}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Level</div>
                <div className="stat-value">{state.level}</div>
              </div>

              <div className="board" style={{ marginTop: "10px" }}>
                {state.board.map((row, y) => (
                  <div key={y} style={{ display: "flex" }}>
                    {row.map((cell, x) => {
                      const blockKey = COLOR_MAP[cell] || "0";
                      const color = BLOCK_COLORS[blockKey];
                      return (
                        <div key={x} className={`cell ${cell ? "filled" : "empty"}`} style={cell ? { "--block-color": color, "--block-color33": color + "33" } : {}} />
                      );
                    })}
                  </div>
                ))}
              </div>

              <div className="next-block" style={{ marginTop: "10px" }}>
                <h4 style={{ margin: "5px 0" }}>Next</h4>
                {(BLOCK_SHAPES[state.nextBlock] || [[]]).map((row, y) => (
                  <div key={y} style={{ display: "flex" }}>
                    {row.map((cell, x) => (
                      <div
                        key={x}
                        className={`next-cell ${cell ? "filled" : "empty"}`}
                        style={cell ? { "--block-color": BLOCK_COLORS[state.nextBlock], "--block-color33": BLOCK_COLORS[state.nextBlock] + "33" } : {}}
                      />
                    ))}
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      <div style={{ marginTop: "20px", display: "flex", justifyContent: "center", gap: "10px" }}>
        <button onClick={() => navigate("/rooms")}>Back to Room List</button>
      </div>
    </div>
  );
}
