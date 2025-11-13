package com.kienminh.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kienminh.model.GameStateDTO;
import com.kienminh.util.HttpClientUtil;
import com.kienminh.util.SessionManager;

public class MultiGameApi {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Bắt đầu game multiplayer cho roomId
     */
    public static GameStateDTO start(Long roomId) {
        try {
            String response = HttpClientUtil.post(
                    "/api/multigame/start/" + roomId,
                    "{}", // backend không nhận null
                    SessionManager.getToken()
            );
            return mapper.readValue(response, GameStateDTO.class);
        } catch (Exception e) {
            System.err.println("[MultiGameApi] Error starting game: " + e.getMessage());
            return null;
        }
    }

    /**
     * Thực hiện action (left, right, rotate, drop) cho player
     */
    public static GameStateDTO move(Long playerId, String action) {
        String endpoint = switch (action.toLowerCase()) {
            case "left" -> "/moveLeft";
            case "right" -> "/moveRight";
            case "rotate" -> "/rotate";
            case "drop" -> "/drop";
            default -> throw new IllegalArgumentException("Invalid move: " + action);
        };
        try {
            String response = HttpClientUtil.post(
                    "/api/multigame/player/" + playerId + endpoint,
                    "{}", // backend không nhận null
                    SessionManager.getToken()
            );
            return mapper.readValue(response, GameStateDTO.class);
        } catch (Exception e) {
            System.err.println("[MultiGameApi] Error moving player: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy trạng thái game hiện tại của player
     */
    public static GameStateDTO getPlayerState(Long playerId) {
        try {
            String response = HttpClientUtil.get(
                    "/api/multigame/player/" + playerId + "/state",
                    SessionManager.getToken()
            );
            return mapper.readValue(response, GameStateDTO.class);
        } catch (Exception e) {
            System.err.println("[MultiGameApi] Error getting player state: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra xem game của player đã kết thúc chưa
     */
    public static boolean isGameOver(Long playerId) {
        try {
            String response = HttpClientUtil.get(
                    "/api/multigame/player/" + playerId + "/isGameOver",
                    SessionManager.getToken()
            );
            return response.contains("true");
        } catch (Exception e) {
            System.err.println("[MultiGameApi] Error checking game over: " + e.getMessage());
            return false;
        }
    }
}
