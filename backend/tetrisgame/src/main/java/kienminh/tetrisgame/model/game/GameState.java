package kienminh.tetrisgame.model.game;

import kienminh.tetrisgame.model.game.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameState {

    private Board board;
    private int score;
    private int level;
    private GameStatus status;

    public GameState() {
        start();
    }

    public void start() {
        this.board = new Board();
        board.init();
        this.score = 0;
        this.level = 1;
        this.status = GameStatus.PLAYING;
    }

    public void reset() {
        start();
    }

    public synchronized void moveLeft() {
        if (!isPlaying()) return;
        board.moveLeft();
    }

    public synchronized void moveRight() {
        if (!isPlaying()) return;
        board.moveRight();
    }

    public synchronized void rotate() {
        if (!isPlaying()) return;
        board.rotateBlock();
    }

    public synchronized void drop() {
        if (!isPlaying()) return;
        board.dropDown();
        score += 10;
        levelUpCheck();
        checkSpawnOrGameOver();
    }

    /** Tick tự động từ game loop */
    public synchronized void tick() {
        if (!isPlaying()) return;

        boolean moved = board.moveDown();
        if (!moved) {
            int lines = board.clearLines();
            if (lines > 0) {
                score += computeScoreForLines(lines);
                levelUpCheck();
            }
            checkSpawnOrGameOver();
        }
    }

    private void checkSpawnOrGameOver() {
        if (board.getCurrentBlock() == null) {
            boolean ok = board.spawnBlock();
            if (!ok) status = GameStatus.GAME_OVER;
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

    public boolean isPlaying() {
        return status == GameStatus.PLAYING;
    }

    public boolean isGameOver() {
        return status == GameStatus.GAME_OVER;
    }
}
