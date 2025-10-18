package com.kienminh.controller;

import com.kienminh.api.RoomApi;
import com.kienminh.model.RoomDTO;
import com.kienminh.util.AuthGuard;
import com.kienminh.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class RoomController {
    @FXML private TextField roomNameField;
    @FXML private TextField roomIdField;

    @FXML
    public void initialize() {
        AuthGuard.requireLogin();
    }

    @FXML
    private void onBack() {
        SceneUtil.switchScene("main_menu.fxml");
    }

    @FXML
    private void onCreateRoom() {
        String name = roomNameField.getText();
        RoomDTO room = RoomApi.createRoom(name);
        if (room != null) {
            showAlert("Room created: " + room.getName());
            SceneUtil.switchScene("waiting_room.fxml");
        } else {
            showAlert("Failed to create room");
        }
    }

    @FXML
    private void onJoinRoom() {
        try {
            Long roomId = Long.parseLong(roomIdField.getText());
            boolean joined = RoomApi.joinRoom(roomId);
            if (joined) {
                showAlert("Joined room " + roomId);
                SceneUtil.switchScene("waiting_room.fxml");
            } else {
                showAlert("Join failed");
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid room ID");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}
