package com.kienminh.controller;

import com.kienminh.util.AuthGuard;
import com.kienminh.util.SceneUtil;
import com.kienminh.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class MainMenuController {
    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        AuthGuard.requireLogin();
        welcomeLabel.setText("Xin chào, " + SessionManager.getUsername() + "!");
    }

    @FXML
    private void onSoloGame() {
        AuthGuard.requireLogin();
        SceneUtil.switchScene("solo_game.fxml");
    }

    @FXML
    private void onMultiplayer() {
        AuthGuard.requireLogin();
        SceneUtil.switchScene("room.fxml");
    }

    @FXML
    private void onProfile() {
        AuthGuard.requireLogin();
        SceneUtil.switchScene("profile.fxml");
    }

    @FXML
    private void onLogout() {
        SessionManager.clear();
        SceneUtil.switchScene("login.fxml");
    }
}
