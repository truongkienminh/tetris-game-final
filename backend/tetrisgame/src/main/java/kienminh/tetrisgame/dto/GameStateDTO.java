package kienminh.tetrisgame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDTO {
    private int[][] board;     // ma trận game
    private int score;         // điểm hiện tại
    private int level;         // level
    private String status;     // PLAYING / GAME_OVER / PAUSED
    private String currentBlock; // loại block đang rơi
    private String nextBlock;    // loại block tiếp theo
}
