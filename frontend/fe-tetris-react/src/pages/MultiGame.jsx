import React, { useState, useEffect, useCallback, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import "../css/MultiGame.css";

const BLOCK_COLORS = {
  I: "cyan", O: "yellow", T: "purple", S: "lime",
  Z: "red", J: "blue", L: "orange", 0: "#111",
};

const COLOR_MAP = {
  0: "0", 1: "I", 2: "O", 3: "T", 4: "S", 5: "Z", 6: "J", 7: "L",
};

const TETRIMINO_SHAPES = {
  I: [[0, 0, 0, 0], [1, 1, 1, 1], [0, 0, 0, 0], [0, 0, 0, 0]],
  O: [[1, 1], [1, 1]],
  T: [[0, 1, 0], [1, 1, 1], [0, 0, 0]],
  S: [[0, 1, 1], [1, 1, 0], [0, 0, 0]],
  Z: [[1, 1, 0], [0, 1, 1], [0, 0, 0]],
  J: [[1, 0, 0], [1, 1, 1], [0, 0, 0]],
  L: [[0, 0, 1], [1, 1, 1], [0, 0, 0]],
};

export default function MultiGame() {
  const { roomId } = useParams();
  const navigate = useNavigate();

  const [currentPlayerId, setCurrentPlayerId] = useState(null);
  const [players, setPlayers] = useState([]);
  const [gameStates, setGameStates] = useState({});
  const [error, setError] = useState(null);
  const [gameOverPlayers, setGameOverPlayers] = useState(new Set());
  const [rankings, setRankings] = useState(null);
  const [roomGameOver, setRoomGameOver] = useState(false);
  const [isCurrentPlayerGameOver, setIsCurrentPlayerGameOver] = useState(false);
  // ✅ NEW: Track which player the user is watching
  const [watchingPlayerId, setWatchingPlayerId] = useState(null);

  const intervalRef = useRef(null);
  const stompClientRef = useRef(null);

  const API = axios.create({ baseURL: "http://localhost:8080/api" });
  API.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });

  // ===== STOMP + SockJS setup =====
  const setupWebSocket = useCallback(() => {
    const token = localStorage.getItem("token");
    const socket = new SockJS("http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        token,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("✅ STOMP connected for room:", roomId);

        client.subscribe(`/topic/room/${roomId}`, (frame) => {
          try {
            const message = JSON.parse(frame.body);

            if (message.type === "PLAYER_GAME_OVER") {
              console.log("✅ Player game over:", message.playerName, "Score:", message.score);
              setGameOverPlayers((prev) => new Set([...prev, message.playerId]));

              if (message.finalState) {
                setGameStates((prev) => ({
                  ...prev,
                  [message.playerId]: {
                    ...prev[message.playerId],
                    board: message.finalState.board,
                    score: message.finalState.score,
                    level: message.finalState.level,
                    status: "GAME_OVER",
                  },
                }));
              }
            } else if (message.type === "ROOM_GAME_OVER") {
              console.log("✅ Room game over - Rankings received");
              setRoomGameOver(true);
              if (message.rankings) setRankings(message.rankings);
            } else if (message.type === "TICK_UPDATE") {
              setGameStates((prev) => ({
                ...prev,
                [message.playerId]: {
                  board: message.board,
                  score: message.score,
                  level: message.level,
                  status: message.status,
                  nextBlock: message.nextBlock,
                },
              }));
            } else if (message.type === "GAME_START" && String(message.roomId) === String(roomId)) {
              console.log("🎮 GAME_START received, game is starting.");
            }
          } catch (e) {
            console.error("❌ STOMP message parse error:", e);
          }
        });
      },
      onStompError: (frame) => {
        console.error("❌ STOMP error:", frame);
      },
      onWebSocketClose: (evt) => {
        console.warn("⚠️ STOMP websocket closed", evt);
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      try {
        if (client && client.active) {
          client.deactivate();
        }
      } catch (e) {
        console.warn("Error deactivating STOMP client:", e);
      }
    };
  }, [roomId]);

  const fetchCurrentPlayer = useCallback(async () => {
    try {
      const res = await API.get("/player/me");
      setCurrentPlayerId(res.data.id);
      // ✅ Initialize watching self
      setWatchingPlayerId(res.data.id);
    } catch (e) {
      console.error("❌ fetchCurrentPlayer:", e);
      setError("Cannot load current player");
    }
  }, []);

  const fetchPlayers = useCallback(async () => {
    try {
      const res = await API.get(`/rooms/${roomId}`);
      setPlayers(res.data.players || []);
    } catch (e) {
      console.error("❌ fetchPlayers:", e);
      setError("Cannot load room players");
    }
  }, [roomId]);

  const fetchStates = useCallback(async () => {
    try {
      const res = await API.get(`/multigame/room/${roomId}/states`);
      setGameStates(res.data || {});
    } catch (e) {
      console.error("❌ fetchStates:", e);
    }
  }, [roomId]);

  const checkRoomCompletion = useCallback(async () => {
    try {
      const res = await API.get(`/multigame/room/${roomId}/isComplete`);
      if (res.data) {
        console.log("✅ Room is complete, fetching rankings...");
        const rankRes = await API.get(`/multigame/room/${roomId}/rankings`);
        setRankings(rankRes.data);
        setRoomGameOver(true);
      }
    } catch (e) {
      console.error("❌ checkRoomCompletion:", e);
    }
  }, [roomId]);

  const startSync = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }

    intervalRef.current = setInterval(async () => {
      await fetchStates();

      if (currentPlayerId && gameOverPlayers.has(currentPlayerId)) {
        await checkRoomCompletion();
      }
    }, 500);
  }, [fetchStates, currentPlayerId, gameOverPlayers, checkRoomCompletion]);

  const stopSync = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  }, []);

  const sendAction = useCallback(
    async (playerId, action) => {
      try {
        await API.post(`/multigame/player/${playerId}/${action}`);
        await fetchStates();
      } catch (e) {
        console.error("❌ sendAction:", e);
      }
    },
    [fetchStates]
  );

  // ✅ Handle back to lobby
  const handleBackToLobby = useCallback(async () => {
    stopSync();
    if (stompClientRef.current) {
      try {
        if (stompClientRef.current.active) {
          await stompClientRef.current.deactivate();
        }
      } catch (e) {
        console.warn("Error deactivating stomp client:", e);
      }
    }
    navigate(`/lobby/${roomId}`);
  }, [stopSync, navigate, roomId]);

  const onKeyDown = useCallback(
    (e) => {
      if (!currentPlayerId || isCurrentPlayerGameOver) return;

      let action = null;
      switch (e.key) {
        case "ArrowLeft": action = "moveLeft"; break;
        case "ArrowRight": action = "moveRight"; break;
        case "ArrowUp": action = "rotate"; break;
        case "ArrowDown": action = "tick"; break;
        case " ": action = "drop"; break;
        default: return;
      }

      e.preventDefault();
      sendAction(currentPlayerId, action);
    },
    [currentPlayerId, isCurrentPlayerGameOver, sendAction]
  );

  useEffect(() => {
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [onKeyDown]);

  useEffect(() => {
    if (currentPlayerId && gameOverPlayers.has(currentPlayerId)) {
      console.log("✅ Current player is game over");
      setIsCurrentPlayerGameOver(true);
    }
  }, [currentPlayerId, gameOverPlayers]);

  useEffect(() => {
    fetchCurrentPlayer();
    fetchPlayers();
    fetchStates();
    startSync();

    const cleanupWs = setupWebSocket();

    return () => {
      stopSync();
      try {
        if (stompClientRef.current && stompClientRef.current.active) {
          stompClientRef.current.deactivate();
        }
      } catch (e) {
        console.warn("Error deactivating stomp client on unmount:", e);
      }
      if (typeof cleanupWs === "function") cleanupWs();
    };
  }, [fetchCurrentPlayer, fetchPlayers, fetchStates, startSync, stopSync, setupWebSocket]);

  useEffect(() => {
    console.log("🎮 Game over players updated:", Array.from(gameOverPlayers));
    console.log("🏁 Current room game over status:", roomGameOver);
    console.log("🏆 Rankings:", rankings);
  }, [gameOverPlayers, roomGameOver, rankings]);

  const renderBoard = (state) => {
    if (!state?.board) return <div className="solo-game-board">Loading...</div>;
    return (
      <div className="solo-game-board">
        {state.board.map((row, y) => (
          <div key={y} style={{ display: "flex" }}>
            {row.map((cell, x) => {
              const blockKey = COLOR_MAP[cell] || "0";
              const color = BLOCK_COLORS[blockKey];
              return (
                <div
                  key={`${x}-${y}`}
                  className={`solo-game-cell ${cell ? "solo-game-cell-filled" : "solo-game-cell-empty"}`}
                  style={cell ? { "--block-color": color, "--block-color33": color + "33" } : {}}
                />
              );
            })}
          </div>
        ))}
      </div>
    );
  };

  const renderNextBlock = (blockKey) => {
    if (!blockKey || !TETRIMINO_SHAPES[blockKey]) return <p style={{ color: "#666" }}>...</p>;
    const shape = TETRIMINO_SHAPES[blockKey];
    const color = BLOCK_COLORS[blockKey] || "white";

    return (
      <div className="solo-next-block-container">
        {shape.map((row, y) => (
          <div key={y} style={{ display: "flex" }}>
            {row.map((cell, x) => (
              <div
                key={x}
                className={`solo-next-cell ${cell ? "solo-next-cell-filled" : "solo-next-cell-empty"}`}
                style={cell ? { "--block-color": color, "--block-color33": color + "33" } : {}}
              />
            ))}
          </div>
        ))}
      </div>
    );
  };

  // ✅ NEW: Game Over Screen with Watch/Back options
  const renderGameOverScreen = () => {
    const activePlayers = players.filter(p => !gameOverPlayers.has(p.id));
    const finishedPlayers = players.filter(p => gameOverPlayers.has(p.id));

    return (
      <div className="game-over-overlay">
        <div className="game-over-container">
          <h1 className="game-over-title">💀 GAME OVER</h1>

          {!roomGameOver ? (
            <div className="waiting-screen">
              <p className="waiting-text">⏳ Waiting for all players to finish...</p>

              {/* ✅ Active Players Section */}
              {activePlayers.length > 0 && (
                <div className="watch-section">
                  <h3 className="watch-title">👀 WATCH PLAYERS</h3>
                  <div className="watch-players-list">
                    {activePlayers.map((p) => (
                      <button
                        key={p.id}
                        onClick={() => setWatchingPlayerId(p.id)}
                        className={`watch-player-btn ${watchingPlayerId === p.id ? "active" : ""}`}
                      >
                        🎮 {p.username} - Score: {gameStates[p.id]?.score ?? 0}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* ✅ Finished Players Status */}
              {finishedPlayers.length > 0 && (
                <div className="finished-section">
                  <h3 className="finished-title">
                    ✅ FINISHED ({finishedPlayers.length}/{players.length})
                  </h3>
                  <div className="finished-players-list">
                    {finishedPlayers.map((p) => (
                      <div key={p.id} className="finished-player-badge">
                        {p.username} ({gameStates[p.id]?.score ?? 0})
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* ✅ Back to Lobby Button */}
              <button
                onClick={handleBackToLobby}
                className="back-to-lobby-btn"
              >
                ← Back to Lobby
              </button>
            </div>
          ) : (
            <div className="rankings-screen">
              <h2 className="rankings-title">🏆 FINAL RANKINGS</h2>
              <div className="rankings-list">
                {rankings && rankings.length > 0 ? (
                  rankings.map((rank, index) => (
                    <div key={rank.playerId} className={`ranking-item rank-${index + 1}`}>
                      <span className="rank-medal">
                        {index === 0 ? "🥇" : index === 1 ? "🥈" : index === 2 ? "🥉" : `#${index + 1}`}
                      </span>
                      <span className="rank-name">{rank.username}</span>
                      <span className="rank-score">{rank.score} pts</span>
                    </div>
                  ))
                ) : (
                  <p style={{ color: "#999" }}>Loading rankings...</p>
                )}
              </div>
              <button
                className="back-to-lobby-btn"
                onClick={handleBackToLobby}
                style={{
                  padding: "12px 30px",
                  marginTop: 20,
                  background: "linear-gradient(135deg, #00ff88, #00ccff)",
                  color: "#0a0e27",
                  border: "2px solid #00ff88",
                  borderRadius: "8px",
                  fontSize: 16,
                  fontWeight: "bold",
                  cursor: "pointer",
                  width: "100%",
                }}
              >
                Back to Lobby
              </button>
            </div>
          )}
        </div>
      </div>
    );
  };

  if (error)
    return (
      <div className="solo-game-container">
        <div style={{ color: "#ff4d4f", fontSize: 24 }}>{error}</div>
      </div>
    );

  if (!currentPlayerId || players.length === 0)
    return (
      <div className="solo-game-container">
        <div style={{ color: "white" }}>Loading game...</div>
      </div>
    );

  // ✅ Determine which player to display when game over
  const displayedPlayerId = isCurrentPlayerGameOver && watchingPlayerId ? watchingPlayerId : currentPlayerId;

  return (
    <div className="solo-game-container">
      <div className="game-header">
        <h1 className="game-logo">🎮 TETRIS</h1>
      </div>
      <div className="solo-game-flex">
        {players.map((p) => {
          const state = gameStates[p.id];
          const isCurrent = p.id === currentPlayerId;
          const isPlayerGameOver = gameOverPlayers.has(p.id);
          // ✅ Show this player's board if we're watching them or they're current
          const shouldDisplay = p.id === displayedPlayerId;

          if (isCurrentPlayerGameOver && !shouldDisplay) {
            return null;
          }

          return (
            <div
              key={p.id}
              className={`multi-game-card ${isCurrent ? "current-player" : ""} ${isPlayerGameOver ? "game-over-card" : ""}`}
            >
              <div className="board-header">
                <h3 className="board-player-name">
                  {isCurrent ? "🎮 YOU" : p.username}
                  {isPlayerGameOver && " ✅"}
                  {!isCurrent && watchingPlayerId === p.id && isCurrentPlayerGameOver && " 👀"}
                </h3>
              </div>

              <div className="solo-game-info-panel" style={{ gridColumn: 1, gridRow: 2 }}>
                <h2 className="solo-game-title">{p.username}</h2>
                <div className="stat-card">
                  <div className="stat-label">Score</div>
                  <div className="stat-value">{state?.score ?? 0}</div>
                </div>
                <div className="stat-card">
                  <div className="stat-label">Level</div>
                  <div className="stat-value">{state?.level ?? 1}</div>
                </div>
                {!isCurrent && (
                  <div className="stat-card" style={{ marginTop: 10 }}>
                    <div className="stat-label">Status</div>
                    <div style={{ fontSize: 12, color: "#ff6a00" }}>
                      {isPlayerGameOver ? "FINISHED ✅" : state?.status ?? "PLAYING"}
                    </div>
                  </div>
                )}
              </div>

              {renderBoard(state)}

              <div className="solo-game-right-panel" style={{ display: "flex", flexDirection: "column", gap: 20, gridColumn: 3, gridRow: 2 }}>
                <div>
                  <h3 style={{ marginBottom: 10, fontSize: 14, fontWeight: 700, textTransform: "uppercase", letterSpacing: 1.5, color: "#ff6a00" }}>
                    Next Block
                  </h3>
                  {renderNextBlock(state?.nextBlock)}
                </div>

                {isCurrent && !isPlayerGameOver && (
                  <>
                    <div style={{ fontSize: 11, color: "#999", textAlign: "center", lineHeight: 1.6 }}>
                      <div>↑ ROTATE • ← → MOVE</div>
                      <div>↓ TICK • SPACE DROP</div>
                    </div>
                  </>
                )}

                {isCurrent && isPlayerGameOver && !roomGameOver && (
                  <div style={{ fontSize: 12, color: "#ffaa00", textAlign: "center" }}>
                    ⏳ Game Over - Check your options!
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {isCurrentPlayerGameOver && renderGameOverScreen()}
    </div>
  );
}