package com.kienminh.controller;

import com.kienminh.api.MultiGameApi;
import com.kienminh.model.GameStateDTO;
import com.kienminh.util.SceneUtil;
import com.kienminh.util.SessionManager;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class MultiGameController {

    @FXML private Canvas gameCanvas;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label statusLabel;

    private GraphicsContext gc;
    private Long playerId;
    private GameStateDTO gameState;
    private AnimationTimer gameLoop;
    private final int blockSize = 25;

    // --- Nhận state khởi tạo từ backend ---
    private static GameStateDTO initialState;

    public static void setInitialGameState(GameStateDTO state) {
        initialState = state;
    }

    @FXML
    public void initialize() {
        gc = gameCanvas.getGraphicsContext2D();
        playerId = SessionManager.getPlayerId();

        // Lấy state ban đầu từ host hoặc backend
        gameState = initialState != null ? initialState : MultiGameApi.getPlayerState(playerId);
        if (gameState == null) {
            statusLabel.setText("Không thể tải trạng thái trò chơi!");
            return;
        }

        drawBoard();
        updateLabels();

        // Bắt đầu vòng lặp game
        startGameLoop();

        // Gắn KeyEvent
        Platform.runLater(() -> {
            gameCanvas.setFocusTraversable(true);
            gameCanvas.requestFocus();
            if (gameCanvas.getScene() != null) {
                gameCanvas.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
            }
        });
    }

    private void startGameLoop() {
        if (gameLoop != null) gameLoop.stop();

        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 500_000_000) { // 0.5s
                    refreshState();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    private void refreshState() {
        GameStateDTO updated = MultiGameApi.getPlayerState(playerId);
        if (updated != null) {
            gameState = updated;
            drawBoard();
            updateLabels();
            checkGameOver();
        }
    }

    private void handleKey(KeyEvent event) {
        if (gameState == null || "GAME_OVER".equalsIgnoreCase(gameState.getStatus())) return;

        String action = switch (event.getCode()) {
            case LEFT -> "LEFT";
            case RIGHT -> "RIGHT";
            case UP -> "ROTATE";
            case DOWN -> "TICK";
            case SPACE -> "DROP";
            default -> null;
        };

        if (action != null) {
            GameStateDTO updated = MultiGameApi.move(playerId, action);
            if (updated != null) {
                gameState = updated;
                drawBoard();
                updateLabels();
                checkGameOver();
            }
            event.consume();
        }
    }

    private void drawBoard() {
        if (gameState == null || gameState.getBoard() == null) return;

        int[][] grid = gameState.getBoard();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                int val = grid[y][x];
                gc.setFill(getColorForBlock(val));
                gc.fillRect(x * blockSize, y * blockSize, blockSize, blockSize);
                gc.setStroke(Color.DARKGRAY);
                gc.strokeRect(x * blockSize, y * blockSize, blockSize, blockSize);
            }
        }
    }

    private Color getColorForBlock(int val) {
        return switch (val) {
            case 1 -> Color.CYAN;
            case 2 -> Color.YELLOW;
            case 3 -> Color.PURPLE;
            case 4 -> Color.LIMEGREEN;
            case 5 -> Color.RED;
            case 6 -> Color.BLUE;
            case 7 -> Color.ORANGE;
            default -> Color.BLACK;
        };
    }

    private void updateLabels() {
        if (gameState == null) return;
        scoreLabel.setText("Score: " + gameState.getScore());
        levelLabel.setText("Level: " + gameState.getLevel());
        statusLabel.setText("Status: " + gameState.getStatus());
    }

    private void checkGameOver() {
        if (gameState != null && "GAME_OVER".equalsIgnoreCase(gameState.getStatus())) {
            if (gameLoop != null) gameLoop.stop();
            Platform.runLater(() -> {
                statusLabel.setText("Game Over!");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("Trò chơi đã kết thúc!\nĐiểm của bạn: " + gameState.getScore());
                alert.showAndWait();
            });
        }
    }

    @FXML
    private void onBack() {
        if (gameLoop != null) gameLoop.stop();
        SceneUtil.switchScene("main_menu.fxml");
    }

    public void stop() {
        if (gameLoop != null) gameLoop.stop();
    }
}
