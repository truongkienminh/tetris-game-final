package com.kienminh.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kienminh.model.PlayerDTO;
import com.kienminh.util.HttpClientUtil;
import com.kienminh.util.SessionManager;

public class PlayerApi {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static PlayerDTO createPlayer() {
        try {
            String response = HttpClientUtil.post("/api/player/create", "{}", SessionManager.getToken());
            return mapper.readValue(response, PlayerDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PlayerDTO getCurrentPlayer() {
        try {
            String response = HttpClientUtil.get("/api/player/me", SessionManager.getToken());
            return mapper.readValue(response, PlayerDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateStatus(Long playerId, boolean online) {
        try {
            String json = String.format("{\"online\":%b}", online);
            String response = HttpClientUtil.post("/api/player/" + playerId + "/status", json, SessionManager.getToken());
            return response.contains("updated");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
