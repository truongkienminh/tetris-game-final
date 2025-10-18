package com.kienminh.controller;

import com.kienminh.api.PlayerApi;
import com.kienminh.api.SoloGameApi;
import com.kienminh.model.GameStateDTO;
import com.kienminh.model.PlayerDTO;
import com.kienminh.util.AuthGuard;
import com.kienminh.util.SceneUtil;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class SoloGameController {

    @FXML private Canvas gameCanvas;
    @FXML private Canvas nextBlockCanvas;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label statusLabel;
    @FXML private Label currentBlockLabel;
    @FXML private Button playAgainButton;

    private GraphicsContext gc;      // Board canvas
    private GraphicsContext nextGc;  // Next block canvas
    private Long playerId;
    private GameStateDTO gameState;
    private AnimationTimer gameLoop;

    private final int blockSize = 25;
    private final int nextBlockSize = 15;

    @FXML
    public void initialize() {
        AuthGuard.requireLogin();

        gc = gameCanvas.getGraphicsContext2D();
        nextGc = nextBlockCanvas.getGraphicsContext2D();
        playAgainButton.setVisible(false); // ẩn mặc định

        PlayerDTO player = PlayerApi.getCurrentPlayer();
        if (player == null || player.getId() == null) {
            statusLabel.setText("Không lấy được thông tin người chơi!");
            return;
        }
        playerId = player.getId();

        startNewGame();

        Platform.runLater(() -> {
            gameCanvas.setFocusTraversable(true);
            gameCanvas.requestFocus();
            gameCanvas.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        });
    }

    private void startNewGame() {
        gameState = SoloGameApi.start(playerId);
        if (gameState == null) {
            statusLabel.setText("Không thể bắt đầu game!");
            return;
        }

        drawBoard();
        drawNextBlock();
        updateLabels();

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
        GameStateDTO updated = SoloGameApi.getState(playerId);
        if (updated != null) {
            gameState = updated;
            drawBoard();
            drawNextBlock();
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
            GameStateDTO updated = SoloGameApi.sendAction(playerId, action);
            if (updated != null) {
                gameState = updated;
                drawBoard();
                drawNextBlock();
                updateLabels();
                checkGameOver();
            }
            event.consume();
        }
    }

    private void checkGameOver() {
        if (gameState != null && "GAME_OVER".equalsIgnoreCase(gameState.getStatus())) {
            statusLabel.setText("Game Over!");
            if (gameLoop != null) gameLoop.stop();

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText(null);
                alert.setContentText("Bạn đã thua! Điểm của bạn: " + gameState.getScore());
                alert.showAndWait();

                playAgainButton.setVisible(true); // hiển thị nút chơi lại
            });
        }
    }

    @FXML
    private void onPlayAgain() {
        playAgainButton.setVisible(false);
        startNewGame();
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

    private void drawNextBlock() {
        if (gameState == null || gameState.getNextBlock() == null) return;

        nextGc.clearRect(0, 0, nextBlockCanvas.getWidth(), nextBlockCanvas.getHeight());

        String type = gameState.getNextBlock().toUpperCase();
        int[][] shape = getBlockShape(type);
        Color color = getColorForBlockType(type);

        for (int y = 0; y < shape.length; y++) {
            for (int x = 0; x < shape[y].length; x++) {
                if (shape[y][x] == 1) {
                    nextGc.setFill(color);
                    nextGc.fillRect(x * nextBlockSize, y * nextBlockSize, nextBlockSize, nextBlockSize);
                    nextGc.setStroke(Color.DARKGRAY);
                    nextGc.strokeRect(x * nextBlockSize, y * nextBlockSize, nextBlockSize, nextBlockSize);
                }
            }
        }
    }

    private int[][] getBlockShape(String type) {
        return switch (type) {
            case "I" -> new int[][] {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}};
            case "O" -> new int[][] {{1,1},{1,1}};
            case "T" -> new int[][] {{0,1,0},{1,1,1},{0,0,0}};
            case "S" -> new int[][] {{0,1,1},{1,1,0},{0,0,0}};
            case "Z" -> new int[][] {{1,1,0},{0,1,1},{0,0,0}};
            case "J" -> new int[][] {{1,0,0},{1,1,1},{0,0,0}};
            case "L" -> new int[][] {{0,0,1},{1,1,1},{0,0,0}};
            default -> new int[][] {{0}};
        };
    }

    private Color getColorForBlockType(String type) {
        return switch (type) {
            case "I" -> Color.CYAN;
            case "O" -> Color.YELLOW;
            case "T" -> Color.PURPLE;
            case "S" -> Color.LIMEGREEN;
            case "Z" -> Color.RED;
            case "J" -> Color.BLUE;
            case "L" -> Color.ORANGE;
            default -> Color.BLACK;
        };
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
        currentBlockLabel.setText("Current: " + gameState.getCurrentBlock());
    }

    @FXML
    private void onBack() {
        if (gameLoop != null) gameLoop.stop();
        SceneUtil.switchScene("main_menu.fxml");
    }
}
