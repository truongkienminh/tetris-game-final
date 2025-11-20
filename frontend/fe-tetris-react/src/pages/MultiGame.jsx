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

  const intervalRef = useRef(null);
  const stompClientRef = useRef(null);

  const API = axios.create({ baseURL: `${import.meta.env.VITE_API_URL.replace('/auth', '')}/api` });
  API.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });

  const setupWebSocket = useCallback(() => {
    const token = localStorage.getItem("token");
    const socket = new SockJS(`${import.meta.env.VITE_API_URL.replace('/auth', '')}/ws`);
    const client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: { token },
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

  const renderBoard = (state, size = "large") => {
    if (!state?.board) return <div className={`solo-game-board solo-game-board-${size}`}>Loading...</div>;
    return (
      <div className={`solo-game-board solo-game-board-${size}`}>
        {state.board.map((row, y) => (
          <div key={y} style={{ display: "flex" }}>
            {row.map((cell, x) => {
              const blockKey = COLOR_MAP[cell] || "0";
              const color = BLOCK_COLORS[blockKey];
              return (
                <div
                  key={`${x}-${y}`}
                  className={`solo-game-cell solo-game-cell-${size} ${cell ? "solo-game-cell-filled" : "solo-game-cell-empty"}`}
                  style={cell ? { "--block-color": color, "--block-color33": color + "33" } : {}}
                />
              );
            })}
          </div>
        ))}
      </div>
    );
  };

  const renderNextBlock = (blockKey, size = "large") => {
    if (!blockKey || !TETRIMINO_SHAPES[blockKey]) return <p style={{ color: "#666" }}>...</p>;
    const shape = TETRIMINO_SHAPES[blockKey];
    const color = BLOCK_COLORS[blockKey] || "white";

    return (
      <div className={`solo-next-block-container solo-next-block-container-${size}`}>
        {shape.map((row, y) => (
          <div key={y} style={{ display: "flex" }}>
            {row.map((cell, x) => (
              <div
                key={x}
                className={`solo-next-cell solo-next-cell-${size} ${cell ? "solo-next-cell-filled" : "solo-next-cell-empty"}`}
                style={cell ? { "--block-color": color, "--block-color33": color + "33" } : {}}
              />
            ))}
          </div>
        ))}
      </div>
    );
  };

  const renderGameCard = (player, isCurrent, size = "small") => {
    const state = gameStates[player.id];
    const isPlayerGameOver = gameOverPlayers.has(player.id);

    return (
      <div
        key={player.id}
        className={`game-card game-card-${size} ${isCurrent ? "game-card-current" : ""} ${isPlayerGameOver ? "game-card-over" : ""}`}
      >
        <div className={`card-header card-header-${size}`}>
          <h3 className={`card-player-name card-player-name-${size}`}>
            {isCurrent ? "🎮 YOU" : player.username}
            {isPlayerGameOver && " ✅"}
          </h3>
        </div>

        {renderBoard(state, size)}

        <div className={`card-stats card-stats-${size}`}>
          <div className={`stat-item stat-item-${size}`}>
            <span className="stat-label">Score</span>
            <span className="stat-value">{state?.score ?? 0}</span>
          </div>
          <div className={`stat-item stat-item-${size}`}>
            <span className="stat-label">Level</span>
            <span className="stat-value">{state?.level ?? 1}</span>
          </div>
        </div>

        {isCurrent && (
          <div className={`next-block-section next-block-section-${size}`}>
            <p className="next-block-label">Next Block</p>
            {renderNextBlock(state?.nextBlock, size)}
          </div>
        )}

        {isCurrent && !isPlayerGameOver && (
          <div className="controls-hint">
            <div>⬅️ ROTATE • ← → MOVE</div>
            <div>↓ TICK • SPACE DROP</div>
          </div>
        )}
      </div>
    );
  };

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

              {activePlayers.length > 0 && (
                <div className="active-players-section">
                  <h3 className="section-title">🎮 STILL PLAYING ({activePlayers.length})</h3>
                  <div className="players-grid">
                    {activePlayers.map((p) => (
                      <div key={p.id} className="player-card-small">
                        <div className="player-card-name">{p.username}</div>
                        <div className="player-card-score">Score: {gameStates[p.id]?.score ?? 0}</div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {finishedPlayers.length > 0 && (
                <div className="finished-section">
                  <h3 className="section-title">✅ FINISHED ({finishedPlayers.length}/{players.length})</h3>
                  <div className="finished-players-list">
                    {finishedPlayers.map((p) => (
                      <div key={p.id} className="finished-player-badge">
                        {p.username} ({gameStates[p.id]?.score ?? 0})
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <button onClick={handleBackToLobby} className="back-to-lobby-btn">
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
              <button onClick={handleBackToLobby} className="back-to-lobby-btn">
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

  const currentPlayer = players.find(p => p.id === currentPlayerId);
  const otherPlayers = players.filter(p => p.id !== currentPlayerId);
  const leftPlayer = otherPlayers[0];
  const rightPlayer = otherPlayers[1];

  return (
    <div className="solo-game-container">
      <div className="game-header">
        <h1 className="game-logo">🎮 TETRIS</h1>
      </div>

      <div className="main-game-layout">
        {/* Left player */}
        {leftPlayer && (
          <div className="side-player left-player">
            {renderGameCard(leftPlayer, false, "small")}
          </div>
        )}

        {/* Center - Current player */}
        {currentPlayer && (
          <div className="center-player">
            {renderGameCard(currentPlayer, true, "large")}
            {!isCurrentPlayerGameOver && (
              <div className="playing-indicator">🔴 PLAYING</div>
            )}
          </div>
        )}

        {/* Right player */}
        {rightPlayer && (
          <div className="side-player right-player">
            {renderGameCard(rightPlayer, false, "small")}
          </div>
        )}
      </div>

      {/* Back to lobby button - visible when game over */}
      {isCurrentPlayerGameOver && (
        <button className="floating-back-btn" onClick={handleBackToLobby}>
          ← Back to Lobby
        </button>
      )}

      {isCurrentPlayerGameOver && renderGameOverScreen()}
    </div>
  );
}