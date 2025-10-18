package com.kienminh.controller;

import com.kienminh.api.RoomApi;
import com.kienminh.util.SceneUtil;
import com.kienminh.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WaitingRoomController {

    @FXML private Label roomNameLabel;
    @FXML private Label playerListLabel;
    @FXML private Button startButton;
    @FXML private Button leaveButton;

    private volatile boolean running = true;
    private Long roomId;

    @FXML
    public void initialize() {
        roomId = SessionManager.getRoomId();
        roomNameLabel.setText("Room: " + SessionManager.getRoomName());

        // Nếu là host → cho phép start
        startButton.setDisable(!SessionManager.isHost());

        // Tạo luồng cập nhật danh sách người chơi
        new Thread(this::updatePlayersLoop).start();
    }

    private void updatePlayersLoop() {
        while (running) {
            try {
                var players = RoomApi.getRoomPlayers(roomId);
                Platform.runLater(() -> {
                    playerListLabel.setText(String.join("\n", players));
                });

                // Kiểm tra nếu phòng đã bắt đầu
                boolean started = RoomApi.isGameStarted(roomId);
                if (started) {
                    running = false;
                    Platform.runLater(() -> SceneUtil.switchScene("multi_game.fxml"));
                }

                Thread.sleep(2000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @FXML
    private void onStartGame() {
        if (SessionManager.isHost()) {
            boolean ok = RoomApi.startGame(roomId);
            if (ok) {
                running = false;
                SceneUtil.switchScene("multi_game.fxml");
            }
        }
    }

    @FXML
    private void onLeaveRoom() {
        running = false;
        RoomApi.leaveRoom(roomId);
        SceneUtil.switchScene("room.fxml");
    }
}
