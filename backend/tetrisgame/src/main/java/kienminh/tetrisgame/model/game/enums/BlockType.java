package kienminh.tetrisgame.model.game.enums;

import java.util.concurrent.ThreadLocalRandom;

public enum BlockType {
    I(new int[][] {{1,1,1,1}}),
    O(new int[][] {{1,1},{1,1}}),
    T(new int[][] {{0,1,0},{1,1,1}}),
    S(new int[][] {{0,1,1},{1,1,0}}),
    Z(new int[][] {{1,1,0},{0,1,1}}),
    J(new int[][] {{1,0,0},{1,1,1}}),
    L(new int[][] {{0,0,1},{1,1,1}});

    private final int[][] shape;
    BlockType(int[][] shape) { this.shape = shape; }
    public int[][] getShape() { return shape; }

    public static BlockType random() {
        var vals = values();
        return vals[ThreadLocalRandom.current().nextInt(vals.length)];
    }
}
