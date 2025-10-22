package com.kienminh.util;

import com.kienminh.model.PlayerDTO;

/**
 * Quản lý thông tin người dùng hiện tại trong phiên.
 * Lưu trữ token, username, userId, playerId, và thông tin phòng (nếu có).
 */
public class SessionManager {

    private static String token;
    private static String username;
    private static Long userId;
    private static Long playerId;

    // --- Thông tin phòng ---
    private static Long roomId;
    private static String roomName;
    private static boolean isHost;

    // --- Thiết lập session sau khi đăng nhập ---
    public static void setSession(String jwt, String user, Long uId, Long pId) {
        token = jwt;
        username = user;
        userId = uId;
        playerId = pId;
    }

    public static void setSession(String jwt, String user) {
        token = jwt;
        username = user;
    }

    // --- Lưu thông tin player ---
    public static void setPlayer(PlayerDTO player) {
        if (player != null) {
            playerId = player.getId();
            username = player.getUsername();
            // Nếu backend không có userId riêng, ta dùng playerId như userId
            userId = player.getId();
        }
    }

    // --- Quản lý phòng ---
    public static void setRoom(Long id, String name, boolean host) {
        roomId = id;
        roomName = name;
        isHost = host;
    }

    public static void clearRoom() {
        roomId = null;
        roomName = null;
        isHost = false;
    }

    // --- Getter ---
    public static String getToken() { return token; }
    public static String getUsername() { return username; }
    public static Long getUserId() { return userId; }
    public static Long getPlayerId() { return playerId; }

    public static Long getRoomId() { return roomId; }
    public static String getRoomName() { return roomName; }
    public static boolean isHost() { return isHost; }

    // --- Kiểm tra trạng thái ---
    public static boolean isLoggedIn() {
        return token != null && !token.isEmpty();
    }

    public static void clear() {
        token = null;
        username = null;
        userId = null;
        playerId = null;
        clearRoom();
    }
}
