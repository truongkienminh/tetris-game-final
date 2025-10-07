package kienminh.tetrisgame.model.game;

import kienminh.tetrisgame.model.game.enums.BlockType;
import lombok.*;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {
    private int width = 10;
    private int height = 20;
    private int[][] grid;      // grid[y][x], 0=empty, >0 filled (type id)
    private Block currentBlock;
    private Block nextBlock;

    public void init() {
        grid = new int[height][width];
        for (int i = 0; i < height; i++) Arrays.fill(grid[i], 0);
        nextBlock = randomBlock();
        spawnBlock(); // sẽ set currentBlock
    }

    private Block randomBlock() {
        BlockType t = BlockType.random();
        int[][] s = Arrays.stream(t.getShape()).map(int[]::clone).toArray(int[][]::new);
        return Block.builder().type(t).shape(s).x(0).y(0).build();
    }

    /**
     * spawn nextBlock thành currentBlock
     * @return true nếu spawn ok, false nếu spawn collides => game over
     */
    public boolean spawnBlock() {
        currentBlock = nextBlock.copy();
        currentBlock.setX(width / 2 - currentBlock.getShape()[0].length / 2);
        currentBlock.setY(-currentBlock.getShape().length + 1); // allow negative y
        nextBlock = randomBlock();
        return !collision(currentBlock.getX(), currentBlock.getY(), currentBlock.getShape(), true);
    }

    public boolean moveLeft() {
        if (currentBlock == null) return false;
        return move(currentBlock.getX() - 1, currentBlock.getY());
    }
    public boolean moveRight() {
        if (currentBlock == null) return false;
        return move(currentBlock.getX() + 1, currentBlock.getY());
    }

    public boolean moveDown() {
        if (currentBlock == null) return false;
        if (!move(currentBlock.getX(), currentBlock.getY() + 1)) {
            lockBlock();
            return false;
        }
        return true;
    }

    public void dropDown() {
        while (currentBlock != null && moveDown()) {}
    }

    public void rotateBlock() {
        if (currentBlock == null) return;
        currentBlock.rotate();
        if (collision(currentBlock.getX(), currentBlock.getY(), currentBlock.getShape(), true)) {
            // wall kicks
            if (!move(currentBlock.getX() - 1, currentBlock.getY()) &&
                    !move(currentBlock.getX() + 1, currentBlock.getY())) {
                currentBlock.rotateCounter(); // rollback
            }
        }
    }

    private boolean move(int newX, int newY) {
        if (currentBlock == null) return false;
        if (!collision(newX, newY, currentBlock.getShape(), true)) {
            currentBlock.setX(newX);
            currentBlock.setY(newY);
            return true;
        }
        return false;
    }

    private boolean collision(int x, int y, int[][] shape, boolean allowAbove) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 0) continue;
                int nx = x + j;
                int ny = y + i;
                if (nx < 0 || nx >= width) return true;
                if (ny >= height) return true;
                if (ny < 0 && !allowAbove) return true;
                if (ny >= 0 && grid[ny][nx] != 0) return true;
            }
        }
        return false;
    }

    private void lockBlock() {
        if (currentBlock == null) return;
        int[][] shape = currentBlock.getShape();
        int id = currentBlock.getType().ordinal() + 1;
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = currentBlock.getX() + j;
                    int y = currentBlock.getY() + i;
                    if (y >= 0 && y < height && x >= 0 && x < width) grid[y][x] = id;
                }
            }
        }
        clearLines();
        currentBlock = null;
        spawnBlock(); // luôn spawn block mới
    }

    public int clearLines() {
        int linesCleared = 0;
        for (int row = height - 1; row >= 0; row--) {
            boolean full = true;
            for (int col = 0; col < width; col++) {
                if (grid[row][col] == 0) { full = false; break; }
            }
            if (full) {
                for (int r = row; r > 0; r--) {
                    System.arraycopy(grid[r - 1], 0, grid[r], 0, width);
                }
                Arrays.fill(grid[0], 0);
                linesCleared++;
                row++; // re-check row after cascade
            }
        }
        return linesCleared;
    }

    public int[][] getBoardSnapshot() {
        int[][] copy = new int[height][width];
        for (int i = 0; i < height; i++) copy[i] = Arrays.copyOf(grid[i], width);

        if (currentBlock != null) {
            int[][] shape = currentBlock.getShape();
            int id = currentBlock.getType().ordinal() + 1;
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        int x = currentBlock.getX() + j;
                        int y = currentBlock.getY() + i;
                        if (y >= 0 && y < height && x >= 0 && x < width) copy[y][x] = id;
                    }
                }
            }
        }
        return copy;
    }
}

