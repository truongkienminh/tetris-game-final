package com.kienminh.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kienminh.model.ActionDTO;
import com.kienminh.model.GameStateDTO;
import com.kienminh.util.HttpClientUtil;
import com.kienminh.util.SessionManager;

public class SoloGameApi {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Bắt đầu một ván solo game cho playerId
     */
    public static GameStateDTO start(Long playerId) {
        try {
            // Gửi body mặc định "{}" để backend không nhận null
            String response = HttpClientUtil.post(
                    "/api/solo/" + playerId + "/start",
                    "{}", // backend không nhận null
                    SessionManager.getToken()
            );

            return mapper.readValue(response, GameStateDTO.class);
        } catch (Exception e) {
            System.err.println("[SoloGameApi] Error starting solo game: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gửi hành động (LEFT, RIGHT, ROTATE, DROP, TICK)
     */
    public static GameStateDTO sendAction(Long playerId, String action) {
        try {
            if (action == null || action.isBlank()) return null;

            // Gửi action qua query param, body null
            String response = HttpClientUtil.post(
                    "/api/solo/" + playerId + "/action?action=" + action,
                    "",  // body rỗng
                    SessionManager.getToken()
            );

            return mapper.readValue(response, GameStateDTO.class);

        } catch (Exception e) {
            System.err.println("[SoloGameApi] Error sending action: " + e.getMessage());
            return null;
        }
    }


    /**
     * Lấy trạng thái game hiện tại của playerId
     */
    public static GameStateDTO getState(Long playerId) {
        try {
            String response = HttpClientUtil.get(
                    "/api/solo/" + playerId + "/state",
                    SessionManager.getToken()
            );
            return mapper.readValue(response, GameStateDTO.class);
        } catch (Exception e) {
            System.err.println("[SoloGameApi] Error getting state: " + e.getMessage());
            return null;
        }
    }
}
