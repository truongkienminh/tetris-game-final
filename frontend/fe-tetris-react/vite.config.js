import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  define: {
    global: "window"
  },
  server: {
    port: 5173, // port dev
    proxy: {
      // Khi dev, /api sẽ được chuyển tiếp tới backend trên Render
      '/api': {
        target: 'https://tetris-game-final.onrender.com',
        changeOrigin: true,
        secure: true,
      }
    }
  }
});
