import React, { useEffect, useState } from "react";
import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const MultiGameClient = () => {
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);
  const [stompClient, setStompClient] = useState(null);

  useEffect(() => {
    // 1️⃣ Lấy token JWT từ localStorage
    const token = localStorage.getItem("token");

    // 2️⃣ Tạo kết nối SockJS đến server (dùng HTTPS production)
    const socket = new SockJS("https://tetris-game-final.onrender.com/ws");
    const client = Stomp.over(socket);

    // Tắt debug log STOMP nếu muốn
    client.debug = () => {};

    // 3️⃣ Kết nối STOMP với JWT header
    client.connect(
      token ? { Authorization: `Bearer ${token}` } : {},
      frame => {
        console.log("✅ Connected: ", frame);
        setConnected(true);

        // 4️⃣ Subscribe topic game
        client.subscribe("/topic/game", message => {
          console.log("📩 Received: ", message.body);
          setMessages(prev => [...prev, message.body]);
        });
      },
      error => {
        console.error("❌ Connection error: ", error);
        setConnected(false);
      }
    );

    setStompClient(client);

    // 5️⃣ Cleanup khi component unmount
    return () => {
      if (client && client.connected) {
        client.disconnect(() => {
          console.log("🔌 Disconnected");
        });
      }
    };
  }, []);

  // 6️⃣ Gửi message test (nếu muốn)
  const sendMessage = msg => {
    if (stompClient && stompClient.connected) {
      stompClient.send("/app/game", {}, msg);
    }
  };

  return (
    <div style={{ padding: "1rem" }}>
      <h2>WebSocket Client</h2>
      <p>Status: {connected ? "Connected ✅" : "Disconnected ❌"}</p>

      <button
        onClick={() => sendMessage("Hello from client")}
        disabled={!connected}
        style={{ marginBottom: "1rem" }}
      >
        Send Test Message
      </button>

      <h3>Messages:</h3>
      <ul>
        {messages.map((msg, idx) => (
          <li key={idx}>{msg}</li>
        ))}
      </ul>
    </div>
  );
};

export default MultiGameClient;
