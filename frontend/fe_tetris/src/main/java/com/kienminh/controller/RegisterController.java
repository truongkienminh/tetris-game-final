package com.kienminh.controller;

import com.kienminh.api.AuthApi;
import com.kienminh.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void onRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin!").showAndWait();
            return;
        }

        boolean success = AuthApi.register(username, password);

        if (success) {
            new Alert(Alert.AlertType.INFORMATION, "Đăng ký thành công!").showAndWait();
            SceneUtil.switchScene("login.fxml");
        } else {
            new Alert(Alert.AlertType.ERROR, "Đăng ký thất bại! Vui lòng thử lại.").showAndWait();
        }
    }
    @FXML
    private void onLoginRedirect() {
        SceneUtil.switchScene("login.fxml");
    }

}
