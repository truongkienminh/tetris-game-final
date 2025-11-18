// MultiGameClient.jsx
import React, { useEffect, useState } from "react";
import { Stomp } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const MultiGameClient = () => {
  const [connected, setConnected] = useState(false);
  const [messages, setMessages] = useState([]);

  useEffect(() => {
    // 1️⃣ Tạo kết nối SockJS đến server
    const socket = new SockJS("http://localhost:8080/ws"); // endpoint của bạn
    const stompClient = Stomp.over(socket);

    // 2️⃣ Kết nối STOMP
    stompClient.connect(
      {}, // headers nếu cần (ví dụ Authorization)
      frame => {
        console.log("✅ Connected: ", frame);
        setConnected(true);

        // 3️⃣ Subscribe topic
        stompClient.subscribe("/topic/game", message => {
          console.log("📩 Received: ", message.body);
          setMessages(prev => [...prev, message.body]);
        });
      },
      error => {
        console.error("❌ Connection error: ", error);
        setConnected(false);
      }
    );

    // 4️⃣ Cleanup khi component unmount
    return () => {
      if (stompClient && stompClient.connected) {
        stompClient.disconnect(() => {
          console.log("🔌 Disconnected");
        });
      }
    };
  }, []);

  return (
    <div style={{ padding: "1rem" }}>
      <h2>WebSocket Client</h2>
      <p>Status: {connected ? "Connected ✅" : "Disconnected ❌"}</p>
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
