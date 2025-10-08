package kienminh.tetrisgame.model.game;

import kienminh.tetrisgame.model.game.enums.BlockType;
import lombok.*;

import java.util.Arrays;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {
    private BlockType type;
    private int[][] shape;
    private int x;
    private int y;

    /** Rotate clockwise */
    public void rotate() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                rotated[j][rows - 1 - i] = shape[i][j];
        shape = rotated;
    }

    /** Rotate counter-clockwise */
    public void rotateCounter() {
        for (int i = 0; i < 3; i++) rotate();
    }

    /** Deep copy block */
    public Block copy() {
        int[][] s = Arrays.stream(shape).map(int[]::clone).toArray(int[][]::new);
        return Block.builder().type(type).shape(s).x(x).y(y).build();
    }
}
