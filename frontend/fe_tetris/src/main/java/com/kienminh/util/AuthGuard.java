package com.kienminh.util;

public class AuthGuard {

    /**
     * Kiểm tra nếu chưa đăng nhập thì chuyển về trang login.fxml
     * Gọi trong mỗi Controller (initialize hoặc onLoad)
     */
    public static void requireLogin() {
        if (!SessionManager.isLoggedIn()) {
            System.out.println("⚠️ Chưa đăng nhập — chuyển hướng về trang đăng nhập...");
            SceneUtil.switchScene("login.fxml");
        }
    }
}
