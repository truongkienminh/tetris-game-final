package com.kienminh.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kienminh.model.RoomDTO;
import com.kienminh.util.HttpClientUtil;
import com.kienminh.util.SessionManager;

import java.util.List;

public class RoomApi {
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Tạo phòng mới */
    public static RoomDTO createRoom(String name) {
        try {
            String json = String.format("{\"name\":\"%s\"}", name);
            String response = HttpClientUtil.post("/api/rooms/create", json, SessionManager.getToken());
            RoomDTO room = mapper.readValue(response, RoomDTO.class);

            // Lưu thông tin phòng vào Session
            SessionManager.setRoom(room.getId(), room.getName(), true);
            return room;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Tham gia phòng */
    public static boolean joinRoom(Long roomId) {
        try {
            String response = HttpClientUtil.post("/api/rooms/" + roomId + "/join", "{}", SessionManager.getToken());
            if (response.contains("joined")) {
                RoomDTO room = getRoom(roomId);
                SessionManager.setRoom(room.getId(), room.getName(), false);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Rời khỏi phòng */
    public static boolean leaveRoom(Long roomId) {
        try {
            String response = HttpClientUtil.post("/api/rooms/" + roomId + "/leave", "{}", SessionManager.getToken());
            if (response.contains("left")) {
                SessionManager.clearRoom();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Lấy thông tin phòng */
    public static RoomDTO getRoom(Long roomId) {
        try {
            String response = HttpClientUtil.get("/api/rooms/" + roomId, SessionManager.getToken());
            return mapper.readValue(response, RoomDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Lấy danh sách người chơi trong phòng */
    public static List<String> getRoomPlayers(Long roomId) {
        try {
            String response = HttpClientUtil.get("/api/rooms/" + roomId + "/players", SessionManager.getToken());
            return mapper.readValue(response, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** Kiểm tra xem phòng đã bắt đầu game chưa */
    public static boolean isGameStarted(Long roomId) {
        try {
            String response = HttpClientUtil.get("/api/rooms/" + roomId + "/started", SessionManager.getToken());
            return response.contains("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Bắt đầu game (chỉ host gọi được) */
    public static boolean startGame(Long roomId) {
        try {
            String response = HttpClientUtil.post("/api/rooms/" + roomId + "/start", "{}", SessionManager.getToken());
            return response.contains("started");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Xóa phòng (nếu cần) */
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
