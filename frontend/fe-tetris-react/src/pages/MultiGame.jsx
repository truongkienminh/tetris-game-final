import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight, RotateCw, ChevronDown } from 'lucide-react';
import axios from 'axios';

// Axios for MultiGame API
const MULTIGAME_API = axios.create({
  baseURL: 'http://localhost:8080/api/multigame',
});
MULTIGAME_API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Axios for Room API
const ROOM_API = axios.create({
  baseURL: 'http://localhost:8080/api/rooms',
});
ROOM_API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

const MultiGame = ({ currentUser }) => {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const [gameStates, setGameStates] = useState({});
  const [currentPlayerState, setCurrentPlayerState] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [gameActive, setGameActive] = useState(false);

  // Fetch room info
  const fetchRoom = useCallback(async () => {
    try {
      const res = await ROOM_API.get(`/${roomId}`);
      setRoom(res.data);
    } catch (err) {
      console.error('Error fetching room:', err);
      setError('Failed to fetch room info');
    }
  }, [roomId]);

  // Get all states for all players in the room
  const getAllStates = useCallback(async () => {
    try {
      const res = await MULTIGAME_API.get(`/room/${roomId}/states`);
      setGameStates(res.data);
    } catch (err) {
      console.error('Error fetching all states:', err);
    }
  }, [roomId]);

  // Get current player state
  const getCurrentPlayerState = useCallback(async () => {
    try {
      const res = await MULTIGAME_API.get(`/player/${currentUser.id}/state`);
      setCurrentPlayerState(res.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching player state:', err);
    }
  }, [currentUser.id]);

  // Check if game is over
  const checkGameOver = useCallback(async () => {
    try {
      const res = await MULTIGAME_API.get(`/player/${currentUser.id}/isGameOver`);
      return res.data;
    } catch (err) {
      console.error('Error checking game over:', err);
      return false;
    }
  }, [currentUser.id]);

  // Initial setup
  useEffect(() => {
    const initGame = async () => {
      setLoading(true);
      await fetchRoom();
      await getCurrentPlayerState();
      await getAllStates();
      setGameActive(true);
      setLoading(false);
    };
    initGame();
  }, [fetchRoom, getCurrentPlayerState, getAllStates, roomId]);

  // Fetch state on interval
  useEffect(() => {
    if (!gameActive) return;

    const interval = setInterval(async () => {
      await getCurrentPlayerState();
      await getAllStates();
      const isOver = await checkGameOver();
      if (isOver) {
        setGameActive(false);
      }
    }, 500);

    return () => clearInterval(interval);
  }, [gameActive, getCurrentPlayerState, getAllStates, checkGameOver]);

  // Player actions
  const handleAction = async (action) => {
    try {
      const res = await MULTIGAME_API.post(`/player/${currentUser.id}/${action}`);
      setCurrentPlayerState(res.data);
      setError(null);
    } catch (err) {
      setError(`Failed to execute ${action}`);
    }
  };

  const handleMoveLeft = () => handleAction('moveLeft');
  const handleMoveRight = () => handleAction('moveRight');
  const handleRotate = () => handleAction('rotate');
  const handleDrop = () => handleAction('drop');
  const handleTick = () => handleAction('tick');

  // Render game board
  const renderBoard = () => {
    if (!currentPlayerState || !currentPlayerState.board) return null;

    return (
      <div className="bg-gray-900 p-4 rounded-lg border-4 border-cyan-500 inline-block">
        <div className="grid gap-0" style={{ gridTemplateColumns: `repeat(10, 1fr)` }}>
          {currentPlayerState.board.map((row, rowIdx) =>
            row.map((cell, colIdx) => (
              <div
                key={`${rowIdx}-${colIdx}`}
                className={`w-6 h-6 border border-gray-700 ${
                  cell === 0 ? 'bg-gray-900' : 'bg-cyan-400'
                }`}
              />
            ))
          )}
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 p-8 flex items-center justify-center">
        <div className="text-cyan-400 text-2xl">Loading game...</div>
      </div>
    );
  }

  if (!currentPlayerState) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 p-8 flex items-center justify-center">
        <div className="text-red-400 text-2xl">Failed to load game state</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 to-slate-800 p-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-5xl font-bold text-cyan-400">🎮 Tetris Battle</h1>
          <button
            onClick={() => navigate('/rooms')}
            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded"
          >
            Back to Rooms
          </button>
        </div>

        <div className="text-cyan-300 mb-6 text-lg">
          <span className="font-bold">Room:</span> {room?.roomName}
        </div>

        {error && (
          <div className="bg-red-900 text-red-100 p-4 rounded mb-6">{error}</div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Main Game Board */}
          <div className="lg:col-span-2">
            <div className="bg-slate-800 p-6 rounded-lg border border-slate-700">
              <h2 className="text-2xl font-bold text-white mb-4">Your Board</h2>
              <div className="flex justify-center mb-8">{renderBoard()}</div>

              {/* Game Info */}
              <div className="grid grid-cols-3 gap-4 mb-8">
                <div className="bg-slate-700 p-4 rounded">
                  <p className="text-cyan-300 text-sm">Score</p>
                  <p className="text-white text-2xl font-bold">{currentPlayerState.score}</p>
                </div>
                <div className="bg-slate-700 p-4 rounded">
                  <p className="text-cyan-300 text-sm">Level</p>
                  <p className="text-white text-2xl font-bold">{currentPlayerState.level}</p>
                </div>
                <div className="bg-slate-700 p-4 rounded">
                  <p className="text-cyan-300 text-sm">Status</p>
                  <p className={`text-2xl font-bold ${
                    currentPlayerState.status === 'GAME_OVER' ? 'text-red-400' :
                    currentPlayerState.status === 'PAUSED' ? 'text-yellow-400' :
                    'text-green-400'
                  }`}>
                    {currentPlayerState.status}
                  </p>
                </div>
              </div>

              {/* Controls */}
              <div className="space-y-4">
                <div className="flex gap-4 justify-center">
                  <button
                    onClick={handleMoveLeft}
                    disabled={!gameActive}
                    className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 text-white p-3 rounded flex items-center gap-2"
                  >
                    <ChevronLeft size={24} />
                  </button>
                  <button
                    onClick={handleRotate}
                    disabled={!gameActive}
                    className="bg-purple-600 hover:bg-purple-700 disabled:bg-gray-600 text-white p-3 rounded flex items-center gap-2"
                  >
                    <RotateCw size={24} />
                  </button>
                  <button
                    onClick={handleMoveRight}
                    disabled={!gameActive}
                    className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 text-white p-3 rounded flex items-center gap-2"
                  >
                    <ChevronRight size={24} />
                  </button>
                </div>
                <div className="flex gap-4 justify-center">
                  <button
                    onClick={handleDrop}
                    disabled={!gameActive}
                    className="bg-red-600 hover:bg-red-700 disabled:bg-gray-600 text-white px-6 py-3 rounded flex items-center gap-2 flex-1 justify-center"
                  >
                    <ChevronDown size={24} /> Drop
                  </button>
                  <button
                    onClick={handleTick}
                    disabled={!gameActive}
                    className="bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white px-6 py-3 rounded flex-1"
                  >
                    Tick
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Sidebar - Next Block & Other Players */}
          <div className="lg:col-span-2 space-y-6">
            {/* Current Block */}
            <div className="bg-slate-800 p-6 rounded-lg border border-slate-700">
              <h3 className="text-xl font-bold text-white mb-3">Current Block</h3>
              <p className="text-cyan-400 text-lg font-bold">{currentPlayerState.currentBlock || 'N/A'}</p>
            </div>

            {/* Next Block */}
            <div className="bg-slate-800 p-6 rounded-lg border border-slate-700">
              <h3 className="text-xl font-bold text-white mb-3">Next Block</h3>
              <p className="text-cyan-400 text-lg font-bold">{currentPlayerState.nextBlock || 'N/A'}</p>
            </div>

            {/* All Players States */}
            <div className="bg-slate-800 p-6 rounded-lg border border-slate-700">
              <h3 className="text-xl font-bold text-white mb-4">⚔️ Leaderboard</h3>
              <div className="space-y-3">
                {Object.entries(gameStates)
                  .sort(([, a], [, b]) => b.score - a.score)
                  .map(([id, state], idx) => (
                    <div
                      key={id}
                      className={`p-4 rounded flex items-center justify-between ${
                        Number(id) === currentUser.id
                          ? 'bg-cyan-500 text-black font-bold'
                          : 'bg-slate-700 text-white'
                      }`}
                    >
                      <div>
                        <p className="font-bold">#{idx + 1} Player {id}</p>
                        <p className="text-sm">Lvl {state.level}</p>
                      </div>
                      <div className="text-right">
                        <p className="text-2xl font-bold">{state.score}</p>
                      </div>
                    </div>
                  ))}
              </div>
            </div>

            {/* Room Info */}
            <div className="bg-slate-800 p-6 rounded-lg border border-slate-700">
              <h3 className="text-xl font-bold text-white mb-3">📋 Room Info</h3>
              <p className="text-cyan-300 text-sm mb-2">
                <span className="font-bold">Host:</span> {room?.hostUsername}
              </p>
              <p className="text-cyan-300 text-sm">
                <span className="font-bold">Players:</span> {room?.players?.length || 0}
              </p>
            </div>
          </div>
        </div>

        {/* Game Over Screen */}
        {!gameActive && currentPlayerState.status === 'GAME_OVER' && (
          <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center">
            <div className="bg-slate-800 p-8 rounded-lg border-2 border-red-500 text-center">
              <h2 className="text-4xl font-bold text-red-400 mb-4">GAME OVER</h2>
              <p className="text-white text-2xl mb-6">Final Score: {currentPlayerState.score}</p>
              <button
                onClick={() => navigate('/rooms')}
                className="bg-cyan-500 hover:bg-cyan-600 text-black font-bold px-8 py-3 rounded"
              >
                Back to Rooms
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MultiGame; 