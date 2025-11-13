package com.kienminh.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.kienminh.api.AuthApi;
import com.kienminh.model.UserDTO;
import com.kienminh.util.AuthGuard;
import com.kienminh.util.SceneUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController {
    @FXML private Label usernameLabel;
    @FXML private Label userIdLabel;
    @FXML private Label scoreLabel;

    @FXML
    public void initialize() {
        AuthGuard.requireLogin();

        JsonNode me = AuthApi.me();
        if (me != null) {
            // Map JsonNode sang UserDTO
            UserDTO user = new UserDTO();
            user.setId(me.has("id") ? me.get("id").asLong() : null);
            user.setUsername(me.has("username") ? me.get("username").asText() : "Unknown user");
            user.setLastScore(me.has("lastScore") ? me.get("lastScore").asInt() : 0);

            // Cập nhật UI
            usernameLabel.setText(user.getUsername());
            userIdLabel.setText("User ID: " + (user.getId() != null ? user.getId() : ""));
            scoreLabel.setText("Last Score: " + user.getLastScore());
        } else {
            usernameLabel.setText("Unknown user");
            userIdLabel.setText("");
            scoreLabel.setText("");
        }
    }
    @FXML
    private void onBack(ActionEvent event) {
        // Chuyển về trang MainMenu, đường dẫn theo dự án của bạn
        SceneUtil.switchScene("/main_menu.fxml");
    }
}
