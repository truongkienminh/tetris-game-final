import { useState, useEffect } from "react";
import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MainMenu from "./pages/MainMenu";
import Profile from "./pages/Profile";
import SoloGame from "./pages/SoloGame";
import Room from "./pages/Room";
import Lobby from "./pages/Lobby";
import MultiGame from "./pages/MultiGame"; // ← import MultiGame
import { getCurrentUser } from "./api";

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await getCurrentUser();
        setCurrentUser(res.data);
      } catch {
        setCurrentUser(null);
      }
    };
    fetchUser();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("token");
    setCurrentUser(null);
    navigate("/login");
  };

  return (
    <Routes>
      <Route
        path="/login"
        element={
          <Login
            onLogin={(user) => {
              setCurrentUser(user);
              navigate("/mainmenu");
            }}
          />
        }
      />
      <Route
        path="/register"
        element={
          <Register
            onRegister={(user) => {
              setCurrentUser(user);
              navigate("/mainmenu");
            }}
          />
        }
      />
      <Route
        path="/mainmenu"
        element={
          currentUser ? (
            <MainMenu onLogout={handleLogout} currentUser={currentUser} />
          ) : (
            <Navigate to="/login" />
          )
        }
      />
      <Route
        path="/profile"
        element={currentUser ? <Profile /> : <Navigate to="/login" />}
      />
      <Route
        path="/solo-game/:playerId"
        element={
          currentUser ? <SoloGame playerId={currentUser.id} /> : <Navigate to="/login" />
        }
      />
      <Route
        path="/rooms"
        element={currentUser ? <Room currentUser={currentUser} /> : <Navigate to="/login" />}
      />
      <Route
        path="/lobby/:roomId"
        element={currentUser ? <Lobby currentUser={currentUser} /> : <Navigate to="/login" />}
      />
      {/* ← Thêm route MultiGame */}
      <Route
        path="/multigame/:roomId"
        element={currentUser ? <MultiGame currentUser={currentUser} /> : <Navigate to="/login" />}
      />
      <Route
        path="*"
        element={<Navigate to={currentUser ? "/mainmenu" : "/login"} />}
      />
    </Routes>
  );
}

export default App;
