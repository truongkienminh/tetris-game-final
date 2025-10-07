package kienminh.tetrisgame.model.game;

import kienminh.tetrisgame.model.game.enums.GameStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameState {
    private Board board;
    private int score;
    private int level;
    private GameStatus status;

    public void start() {
        board = new Board();
        board.init();
        score = 0;
        level = 1;
        status = GameStatus.PLAYING;
    }

    public void reset() { start(); }

    public void moveLeft() {
        ensurePlaying();
        board.moveLeft();
    }

    public void moveRight() {
        ensurePlaying();
        board.moveRight();
    }

    public void rotate() {
        ensurePlaying();
        board.rotateBlock();
    }

    public void drop() {
        ensurePlaying();
        board.dropDown();
        score += 10;
        levelUpCheck();
    }

    public void tick() {
        ensurePlaying();
        boolean moved = board.moveDown();
        if (!moved) {
            int lines = board.clearLines();
            if (lines > 0) {
                score += computeScoreForLines(lines);
                levelUpCheck();
            }
            if (board.getCurrentBlock() == null) { // nếu block cuối đã lock
                boolean ok = board.spawnBlock();
                if (!ok) status = GameStatus.GAME_OVER;
            }
        }
    }

    private int computeScoreForLines(int lines) {
        return switch (lines) {
            case 1 -> 100;
            case 2 -> 300;
            case 3 -> 500;
            case 4 -> 800;
            default -> lines * 100;
        };
    }

    private void levelUpCheck() {
        int newLevel = score / 1000 + 1;
        if (newLevel > level) level = newLevel;
    }

    private void ensurePlaying() {
        if (status != GameStatus.PLAYING) throw new IllegalStateException("Game not playing");
    }
}

