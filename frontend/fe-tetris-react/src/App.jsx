  import { useState, useEffect } from "react";
  import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
  import Login from "./pages/Login";
  import Register from "./pages/Register";
  import MainMenu from "./pages/MainMenu";
  import Profile from "./pages/Profile";
  import SoloGame from "./pages/SoloGame";
  import Room from "./pages/Room";
  import Lobby from "./pages/Lobby";
  import MultiGame from "./pages/MultiGame";
  import { getCurrentUser } from "./api";

  function App() {
    const [currentUser, setCurrentUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    // ✅ Fetch user on mount and when token changes
    useEffect(() => {
      const fetchUser = async () => {
        try {
          const token = localStorage.getItem("token");
          // ✅ Only try to fetch if token exists
          if (token) {
            const res = await getCurrentUser();
            setCurrentUser(res.data);
          } else {
            setCurrentUser(null);
          }
        } catch (error) {
          console.error("Failed to fetch current user:", error);
          // ✅ If token is invalid, clear it
          localStorage.removeItem("token");
          setCurrentUser(null);
        } finally {
          setLoading(false);
        }
      };

      fetchUser();
    }, []);

    const handleLogout = () => {
      localStorage.removeItem("token");
      setCurrentUser(null);
      navigate("/login");
    };

    // ✅ Show loading while checking authentication
    if (loading) {
      return (
        <div style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
          background: "linear-gradient(135deg, #0a0e27 0%, #1a1f4d 50%, #0f1838 100%)",
          color: "#00ff88",
          fontSize: "24px",
        }}>
          Loading...
        </div>
      );
    }

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
              <MainMenu
                currentUser={currentUser}
                onLogout={() => {
                  handleLogout();
                }}
              />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="/profile"
          element={
            currentUser ? (
              <Profile />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="/solo-game/:userId"
          element={
            currentUser ? (
              <SoloGame playerId={currentUser.id} />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="/rooms"
          element={
            currentUser ? (
              <Room currentUser={currentUser} />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="/lobby/:roomId"
          element={
            currentUser ? (
              <Lobby currentUser={currentUser} />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="/multigame/:roomId"
          element={
            currentUser ? (
              <MultiGame currentUser={currentUser} />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route
          path="*"
          element={<Navigate to={currentUser ? "/mainmenu" : "/login"} />}
        />
      </Routes>
    );
  }

  export default App;