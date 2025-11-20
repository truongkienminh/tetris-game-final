import { useState, useEffect, useCallback, useRef } from "react";
import axios from "axios";
import "../css/SoloGame.css"; 
import { useParams, useNavigate } from "react-router-dom";

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

export default function SoloGame() {
  const { userId } = useParams();
  const [gameState, setGameState] = useState(null);
  const [playerId, setPlayerId] = useState(null);
  const [error, setError] = useState("");
  const intervalRef = useRef(null);
  const navigate = useNavigate();
  const pollingActiveRef = useRef(false);

  const API = axios.create({
    baseURL: `${import.meta.env.VITE_API_URL}/solo`,
    headers: { Authorization: `Bearer ${localStorage.getItem("token")}` }
  });

  // Start game
  const startGame = async () => {
    stopPolling();
    if (!userId) {
      setError("UserId not found");
      return;
    }
    try {
      const res = await API.post(`/start/${userId}`);
      const data = res.data;
      setGameState(data);
      setPlayerId(data.playerId);
      pollingActiveRef.current = true;
      startPolling(data.playerId);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || "Cannot start game");
    }
  };

  // Polling trạng thái game
  const startPolling = (pId) => {
    stopPolling();
    pollingActiveRef.current = true;
    intervalRef.current = setInterval(async () => {
      // ✅ Double-check polling is still active
      if (!pollingActiveRef.current) return;

      try {
        const res = await API.get(`/${pId}/state`);
        const data = res.data;
        setGameState(data);

        // ✅ Stop polling when game over is detected
        if (data.status === "GAME_OVER") {
          pollingActiveRef.current = false;
          stopPolling();
          console.log("Game Over detected, polling stopped");
        }
      } catch (err) {
        console.error("Fetch state error:", err);
        // ✅ Stop polling if game not found
        if (err.response?.status === 400 || err.response?.data?.error?.includes("not found")) {
          pollingActiveRef.current = false;
          stopPolling();
          console.log("Game not found, polling stopped");
        }
      }
    }, 500);
  };

  const stopPolling = () => {
    pollingActiveRef.current = false;
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  };

  useEffect(() => {
    startGame();
    return stopPolling;
  }, [userId]);

  // Gửi action
  const sendAction = async (action) => {
    if (!gameState || gameState.status === "GAME_OVER") return;
    try {
      const res = await API.post(`/${playerId}/action`, null, { params: { action } });
      setGameState(res.data);

      // ✅ Stop polling immediately if game ends from this action
      if (res.data.status === "GAME_OVER") {
        pollingActiveRef.current = false;
        stopPolling();
        console.log("Game Over from action, polling stopped");
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || "Action failed");
    }
  };

  // Xử lý phím
  const handleKeyDown = useCallback(
    (e) => {
      if (!gameState || gameState.status === "GAME_OVER") return;
      if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", " "].includes(e.key)) {
        e.preventDefault();
      }
      switch (e.key) {
        case "ArrowLeft": sendAction("LEFT"); break;
        case "ArrowRight": sendAction("RIGHT"); break;
        case "ArrowUp": sendAction("ROTATE"); break;
        case "ArrowDown": sendAction("TICK"); break;
        case " ": sendAction("DROP"); break;
        default: break;
      }
    },
    [gameState, playerId]
  );

  useEffect(() => {
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [handleKeyDown]);

  // Loading hoặc lỗi
  if (error) return <p style={{ color: "red", textAlign: "center" }}>{error}</p>;
  if (!gameState) return <p style={{ textAlign: "center", color: "white" }}>Loading game...</p>;

  // Kiểm tra gameOver
  const isGameOver = gameState.status === "GAME_OVER";

  if (isGameOver) {
    return (
      <div className="solo-game-container" style={{ textAlign: "center", color: "white" }}>
        <h2 className="game-over-text">💀 Game Over 💀</h2>
        <p style={{ fontSize: "1.2rem", fontWeight: "bold", color: "#fff" }}>Your score: {gameState.score}</p>
        <div style={{ marginTop: "20px", display: "flex", justifyContent: "center", gap: "20px" }}>
          <button className="game-over-button" onClick={startGame}>Play Again</button>
          <button className="game-over-button" onClick={() => navigate("/mainmenu")}>Back to MainMenu</button>
        </div>

        <div className="solo-game-flex" style={{ display: "flex", alignItems: "flex-start", gap: "20px", marginTop: "30px" }}>
          <div className="solo-game-info-panel game-over">
            <h2 className="solo-game-title">🎮 Solo Tetris Game</h2>
            <div className="stat-card">
              <div className="stat-label">Score</div>
              <div className="stat-value">{gameState.score}</div>
            </div>
            <div className="stat-card">
              <div className="stat-label">Level</div>
              <div className="stat-value">{gameState.level}</div>
            </div>
          </div>

          <div className="solo-game-board game-over">
            {gameState.board.map((row, y) => (
              <div key={y} style={{ display: "flex" }}>
                {row.map((cell, x) => {
                  const blockKey = COLOR_MAP[cell] || "0";
                  const color = BLOCK_COLORS[blockKey];
                  return (
                    <div
                      key={x}
                      className={`solo-game-cell ${cell ? "solo-game-cell-filled" : "solo-game-cell-empty"}`}
                      style={cell ? { "--block-color": color, "--block-color33": color + "33" } : {}}
                    />
                  );
                })}
              </div>
            ))}
          </div>

          <div className="solo-game-right-panel game-over">
            <h3>Next Block</h3>
            <div className="solo-next-block-container">
              {(BLOCK_SHAPES[gameState.nextBlock] || [[]]).map((row, y) => (
                <div key={y} style={{ display: "flex" }}>
                  {row.map((cell, x) => (
                    <div
                      key={x}
                      className={`solo-next-cell ${cell ? "solo-next-cell-filled" : "solo-next-cell-empty"}`}
                      style={cell ? { "--block-color": BLOCK_COLORS[gameState.nextBlock], "--block-color33": BLOCK_COLORS[gameState.nextBlock] + "33" } : {}}
                    />
                  ))}
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Render game bình thường
  return (
    <div className="solo-game-container">
      <div className="solo-game-flex" style={{ display: "flex", alignItems: "flex-start", gap: "20px" }}>

        {/* Left panel: game title + score */}
        <div className="solo-game-info-panel" style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
          <h2 className="solo-game-title" style={{ margin: 0 }}>🎮 Solo Tetris Game</h2>
          <div className="stat-card">
            <div className="stat-label">Score</div>
            <div className="stat-value">{gameState.score}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Level</div>
            <div className="stat-value">{gameState.level}</div>
          </div>
        </div>

        {/* Board */}
        <div className="solo-game-board">
          {gameState.board.map((row, y) => (
            <div key={y} style={{ display: "flex" }}>
              {row.map((cell, x) => {
                const blockKey = COLOR_MAP[cell] || "0";
                const color = BLOCK_COLORS[blockKey];
                return (
                  <div
                    key={x}
                    className={`solo-game-cell ${cell ? "solo-game-cell-filled" : "solo-game-cell-empty"}`}
                    style={cell ? { "--block-color": color, "--block-color33": color + "33" } : {}}
                  />
                );
              })}
            </div>
          ))}
        </div>

        {/* Right panel: Next Block + Back button */}
        <div className="solo-game-right-panel" style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
          <div>
            <h3 style={{ marginBottom: "10px" }}>Next Block</h3>
            <div className="solo-next-block-container">
              {(BLOCK_SHAPES[gameState.nextBlock] || [[]]).map((row, y) => (
                <div key={y} style={{ display: "flex" }}>
                  {row.map((cell, x) => (
                    <div
                      key={x}
                      className={`solo-next-cell ${cell ? "solo-next-cell-filled" : "solo-next-cell-empty"}`}
                      style={cell ? { "--block-color": BLOCK_COLORS[gameState.nextBlock], "--block-color33": BLOCK_COLORS[gameState.nextBlock] + "33" } : {}}
                    />
                  ))}
                </div>
              ))}
            </div>
          </div>

          <button className="solo-back-button" onClick={() => navigate("/mainmenu")}>
            Back to MainMenu
          </button>
        </div>

      </div>
    </div>
  );
}