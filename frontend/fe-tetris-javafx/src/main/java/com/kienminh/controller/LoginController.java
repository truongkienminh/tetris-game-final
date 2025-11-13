package com.kienminh.controller;

import com.kienminh.api.AuthApi;
import com.kienminh.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!").showAndWait();
            return;
        }

        boolean success = AuthApi.login(username, password);

        if (success) {
            SceneUtil.switchScene("main_menu.fxml");
        } else {
            new Alert(Alert.AlertType.ERROR, "Sai tài khoản hoặc mật khẩu!").showAndWait();
        }
    }

    @FXML
    private void onRegisterRedirect() {
        SceneUtil.switchScene("register.fxml");
    }
}
