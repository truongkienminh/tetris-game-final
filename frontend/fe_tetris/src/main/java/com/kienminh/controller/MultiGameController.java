package com.kienminh.controller;

import com.kienminh.api.MultiGameApi;
import com.kienminh.model.GameStateDTO;
import com.kienminh.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class MultiGameController {

    @FXML private Canvas gameCanvas;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label statusLabel;

    private Long playerId;
    private GraphicsContext gc;
    private volatile boolean running = true;
    private final int blockSize = 25;
    private GameStateDTO gameState;

    // --- Nhận state khởi tạo từ backend ---
    private static GameStateDTO initialState;

    public static void setInitialGameState(GameStateDTO state) {
        initialState = state;
    }

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        playerId = SessionManager.getPlayerId();

        // Đăng ký KeyEvent sau khi Scene attach
        Platform.runLater(() -> {
            if (gameCanvas.getScene() != null) {
                gameCanvas.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
            }
        });

        // Lấy state ban đầu (từ host start hoặc từ backend)
        GameStateDTO state = initialState != null ? initialState : MultiGameApi.getPlayerState(playerId);

        // Bắt đầu game loop
        GameStateDTO finalState = state;
        new Thread(() -> gameLoop(finalState)).start();
    }

    private void handleKeyPress(KeyEvent e) {
        if (gameState == null || "GAME_OVER".equalsIgnoreCase(gameState.getStatus())) return;

        String action = switch (e.getCode()) {
            case LEFT -> "LEFT";
            case RIGHT -> "RIGHT";
            case UP -> "ROTATE";
            case DOWN -> "TICK";    // auto tick
            case SPACE -> "DROP";
            default -> null;
        };

        if (action != null) {
            GameStateDTO updated = MultiGameApi.move(playerId, action);
            if (updated != null) {
                update(updated);
                checkGameOver(updated);
            }
        }
    }

    private void gameLoop(GameStateDTO state) {
        while (running && state != null && !"GAME_OVER".equalsIgnoreCase(state.getStatus())) {
            try {
                Thread.sleep(500);
                state = MultiGameApi.getPlayerState(playerId);
                update(state);
                checkGameOver(state);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void update(GameStateDTO state) {
        if (state == null) return;
        gameState = state;

        Platform.runLater(() -> {
            scoreLabel.setText("Score: " + state.getScore());
            levelLabel.setText("Level: " + state.getLevel());
            drawBoard(state);
        });
    }

    private void checkGameOver(GameStateDTO state) {
        if (state != null && "GAME_OVER".equalsIgnoreCase(state.getStatus())) {
            running = false;
            Platform.runLater(() -> {
                if (statusLabel != null) {
                    statusLabel.setText("Game Over!");
                }
            });
        }
    }

    private void drawBoard(GameStateDTO state) {
        int[][] grid = state.getBoard();
        if (grid == null) return;

        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (grid[y][x] > 0) {
                    gc.setFill(Color.LIMEGREEN);
                    gc.fillRect(x * blockSize, y * blockSize, blockSize - 1, blockSize - 1);
                }
            }
        }
    }

    public void stop() {
        running = false;
    }
}
