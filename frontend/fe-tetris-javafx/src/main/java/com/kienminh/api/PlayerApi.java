package com.kienminh.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kienminh.model.PlayerDTO;
import com.kienminh.util.HttpClientUtil;
import com.kienminh.util.SessionManager;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;

import java.nio.charset.StandardCharsets;

public class PlayerApi {
    private static final ObjectMapper mapper = new ObjectMapper();

    /** ✅ Tạo player mới */
    public static PlayerDTO createPlayer() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = HttpClientUtil.BASE_URL + "/api/player/create";
            HttpPost request = new HttpPost(url);

            String token = SessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                request.addHeader("Authorization", "Bearer " + token);
            }
            request.addHeader("Content-Type", "application/json");

            return client.execute(request, response -> {
                int status = response.getCode();
                HttpEntity entity = response.getEntity();
                String body = entity != null
                        ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                        : "";

                if (status >= 200 && status < 300) {
                    PlayerDTO player = mapper.readValue(body, PlayerDTO.class);
                    SessionManager.setPlayer(player);
                    System.out.println("✅ Player created: " + player.getUsername());
                    return player;
                } else {
                    System.err.println("❌ POST /api/player/create failed: " + status + " - " + body);
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi tạo player: " + e.getMessage());
            return null;
        }
    }

    /** ✅ Lấy player hiện tại */
    public static PlayerDTO getCurrentPlayer() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = HttpClientUtil.BASE_URL + "/api/player/me";
            HttpGet request = new HttpGet(url);

            String token = SessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                request.addHeader("Authorization", "Bearer " + token);
            }

            return client.execute(request, response -> {
                int status = response.getCode();
                HttpEntity entity = response.getEntity();
                String body = entity != null
                        ? new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8)
                        : "";

                if (status >= 200 && status < 300) {
                    PlayerDTO player = mapper.readValue(body, PlayerDTO.class);
                    SessionManager.setPlayer(player);
                    System.out.println("✅ Current player: " + player.getUsername());
                    return player;
                } else {
                    System.err.println("❌ GET /api/player/me failed: " + status + " - " + body);
                    return null;
                }
            });

        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi lấy player hiện tại: " + e.getMessage());
            return null;
        }
    }

    /** ✅ Cập nhật trạng thái online/offline */
    public static boolean updateStatus(Long playerId, boolean online) {
        try {
            String json = String.format("{\"online\":%b}", online);
            String response = HttpClientUtil.post("/api/player/" + playerId + "/status", json, SessionManager.getToken());
            if (response == null || response.isEmpty()) {
                System.err.println("⚠️ Không có phản hồi từ server khi cập nhật trạng thái.");
                return false;
            }
            boolean success = response.contains("updated") || response.contains("success");
            if (success) {
                System.out.println("✅ Player " + playerId + " updated online=" + online);
            } else {
                System.err.println("❌ Cập nhật trạng thái thất bại: " + response);
            }
            return success;
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi khi cập nhật trạng thái player: " + e.getMessage());
            return false;
        }
    }
}
