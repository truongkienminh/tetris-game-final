package com.kienminh.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kienminh.model.PlayerDTO;
import com.kienminh.model.RoomDTO;
import com.kienminh.util.HttpClientUtil;
import com.kienminh.util.SessionManager;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RoomApi {
    private static final ObjectMapper mapper = new ObjectMapper();

    /** ✅ Tạo phòng mới */
    public static RoomDTO createRoom(String name) {
        try {
            String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
            String url = HttpClientUtil.BASE_URL + "/api/rooms/create?roomName=" + encodedName;

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(url);
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
                        RoomDTO room = mapper.readValue(body, RoomDTO.class);
                        SessionManager.setRoom(room.getId(), room.getName(), true);

                        // 🧩 Lấy player hiện tại và lưu vào session
                        PlayerDTO player = PlayerApi.getCurrentPlayer();
                        if (player != null) {
                            SessionManager.setPlayer(player);
                            System.out.println("[RoomApi] ✅ Host PlayerID = " + player.getId());
                        }

                        return room;
                    } else {
                        System.err.println("❌ POST /api/rooms/create failed: " + status + " - " + body);
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** ✅ Tham gia phòng */
    public static boolean joinRoom(Long roomId) {
        try {
            String response = HttpClientUtil.post("/api/rooms/" + roomId + "/join", "{}", SessionManager.getToken());
            RoomDTO room = mapper.readValue(response, RoomDTO.class);
            if (room != null && room.getId() != null) {
                SessionManager.setRoom(room.getId(), room.getName(), false);

                // 🧩 Lấy player hiện tại và lưu vào session
                PlayerDTO player = PlayerApi.getCurrentPlayer();
                if (player != null) {
                    SessionManager.setPlayer(player);
                    System.out.println("[RoomApi] ✅ Joined PlayerID = " + player.getId());
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** ✅ Rời phòng */
    public static boolean leaveRoom(Long roomId) {
        try {
            String response = HttpClientUtil.post("/api/rooms/" + roomId + "/leave", "{}", SessionManager.getToken());
            if (response.contains("left")) {
                SessionManager.clearRoom();
                System.out.println("[RoomApi] ✅ Left room " + roomId);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** ✅ Lấy thông tin phòng */
    public static RoomDTO getRoom(Long roomId) {
        try {
            String response = HttpClientUtil.get("/api/rooms/" + roomId, SessionManager.getToken());
            return mapper.readValue(response, RoomDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** ✅ Lấy danh sách người chơi trong phòng */
    public static List<String> getRoomPlayers(Long roomId) {
        try {
            String response = HttpClientUtil.get("/api/rooms/" + roomId, SessionManager.getToken());
            RoomDTO room = mapper.readValue(response, RoomDTO.class);

            if (room.getPlayers() != null) {
                return room.getPlayers().stream()
                        .map(PlayerDTO::getUsername)
                        .toList();
            } else {
                System.err.println("[RoomApi] Room has no players");
                return List.of();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** ✅ Xóa phòng */
    public static boolean deleteRoom(Long roomId) {
        try {
            String response = HttpClientUtil.delete("/api/rooms/" + roomId, SessionManager.getToken());
            return response.contains("deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
