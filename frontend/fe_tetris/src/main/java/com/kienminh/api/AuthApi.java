package com.kienminh.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kienminh.util.HttpClientUtil;
import com.kienminh.util.SessionManager;

public class AuthApi {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean login(String username, String password) {
        try {
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String response = HttpClientUtil.post("/api/auth/login", json, null);
            JsonNode node = mapper.readTree(response);
            if (node.has("token")) {
                SessionManager.setSession(node.get("token").asText(), username);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean register(String username, String password) {
        try {
            String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            String response = HttpClientUtil.post("/api/auth/register", json, null);
            return response.contains("success") || response.contains("created");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static JsonNode me() {
        try {
            String response = HttpClientUtil.get("/api/auth/me", SessionManager.getToken());
            return mapper.readTree(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
