package com.kienminh.controller;

import com.kienminh.api.MultiGameApi;
import com.kienminh.api.RoomApi;
import com.kienminh.model.GameStateDTO;
import com.kienminh.util.SceneUtil;
import com.kienminh.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class WaitingRoomController {

    @FXML private Label roomNameLabel;
    @FXML private TextArea playerListArea;
    @FXML private Button startButton;
    @FXML private Button leaveButton;

    private volatile boolean running = true;
    private Long roomId;

    @FXML
    public void initialize() {
        roomId = SessionManager.getRoomId();
        roomNameLabel.setText("Room: " + SessionManager.getRoomName());
        startButton.setDisable(!SessionManager.isHost());
        new Thread(this::updatePlayersLoop).start();
    }

    private void updatePlayersLoop() {
        while (running) {
            try {
                var players = RoomApi.getRoomPlayers(roomId);
                Platform.runLater(() -> playerListArea.setText(String.join("\n", players)));
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    @FXML
    private void onStartGame() {
        // Chỉ host mới được start game
        if (SessionManager.isHost()) {
            Long roomId = SessionManager.getRoomId(); // Lấy roomId từ session
            if (roomId == null) {
                System.err.println("[MultiGame] Room ID is null!");
                return;
            }

            // Gọi API start game
            GameStateDTO gameState = MultiGameApi.start(roomId);

            if (gameState != null) {
                System.out.println("[MultiGame] Game started successfully!");
                // Lưu trạng thái game nếu cần (ví dụ pass sang controller của scene mới)
                MultiGameController.setInitialGameState(gameState);

                // Dừng bất cứ timer hoặc loop cũ nếu có
                running = false;

                // Chuyển sang scene game
                SceneUtil.switchScene("multi_game.fxml");
            } else {
                System.err.println("[MultiGame] Failed to start game. Check backend or network.");
            }
        } else {
            System.err.println("[MultiGame] You are not the host!");
        }
    }


    @FXML
    private void onLeaveRoom() {
        running = false;
        RoomApi.leaveRoom(roomId);
        SceneUtil.switchScene("room.fxml");
    }
}
