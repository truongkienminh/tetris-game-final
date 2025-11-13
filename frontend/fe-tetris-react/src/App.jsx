import { useState, useEffect } from "react";
import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MainMenu from "./pages/MainMenu";
import Profile from "./pages/Profile";
import SoloGame from "./pages/SoloGame";
import { getCurrentUser } from "./api";
import Room from "./pages/Room";
import Lobby from "./pages/Lobby";


function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const navigate = useNavigate();

  // Kiểm tra nếu đã login khi load app
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
      {/* Login */}
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

      {/* Register */}
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

      {/* Main Menu */}
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

      {/* Profile */}
      <Route
        path="/profile"
        element={currentUser ? <Profile /> : <Navigate to="/login" />}
      />

      {/* Solo Game */}
      <Route
        path="/solo-game/:playerId"
        element={
          currentUser ? <SoloGame playerId={currentUser.id} /> : <Navigate to="/login" />
        }
      />

      {/* Fallback route */}
      <Route
        path="*"
        element={<Navigate to={currentUser ? "/mainmenu" : "/login"} />}
      />

      <Route
        path="/rooms"
        element={currentUser ? <Room currentUser={currentUser} /> : <Navigate to="/login" />}
      />

      <Route
        path="/lobby/:roomId"
        element={currentUser ? <Lobby currentUser={currentUser} /> : <Navigate to="/login" />}
      />
    </Routes>


  );
}

export default App;
