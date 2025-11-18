package kienminh.tetrisgame.model.game;

import com.fasterxml.jackson.annotation.JsonInclude;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.game.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameState {

    private Board board;
    private int score;
    private int level;
    private GameStatus status;
    private Player player;
    private String nextBlock;

    public GameState() {
        start();
    }

    public void start() {
        this.board = new Board();
        board.init();
        this.score = 0;
        this.level = 1;
        this.status = GameStatus.PLAYING;
        updateNextBlock();
    }

    public void reset() {
        start();
    }

    private void updateNextBlock() {
        Block next = board.getNextBlockCopy();
        this.nextBlock = next != null ? next.getType().name() : null;
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

        int lines = board.clearLines();
        if (lines > 0) {
            score += computeScoreForLines(lines);
        }

        score += 10;

        levelUpCheck();

        // ✅ Check if spawn failed (block collision) → GAME OVER
        boolean ok = board.spawnBlock();
        updateNextBlock();
        if (!ok) {
            status = GameStatus.GAME_OVER;
            return;
        }

        // ✅ Check if spawned block is already out of bounds
        if (board.isBlockTopOut()) {
            status = GameStatus.GAME_OVER;
        }
    }

    public synchronized void tick() {
        if (!isPlaying()) return;

        boolean moved = board.moveDown();
        if (!moved) {
            // Block hit ground or obstacle
            int lines = board.clearLines();
            if (lines > 0) {
                score += computeScoreForLines(lines);
                levelUpCheck();
            }

            // ✅ Try to spawn next block
            boolean ok = board.spawnBlock();
            updateNextBlock();

            if (!ok) {
                // Spawn failed due to collision
                status = GameStatus.GAME_OVER;
                return;
            }

            // ✅ Check if spawned block is already out of bounds (top out)
            if (board.isBlockTopOut()) {
                status = GameStatus.GAME_OVER;
                return;
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

    public boolean isPlaying() {
        return status == GameStatus.PLAYING;
    }

    public boolean isGameOver() {
        return status == GameStatus.GAME_OVER;
    }

    public Block getNextBlock() {
        return board.getNextBlockCopy();
    }
}