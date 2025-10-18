package com.kienminh.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameStateDTO {
    private int[][] board;       // ma trận game
    private int score;
    private int level;
    private String status;       // PLAYING / GAME_OVER / PAUSED
    private String currentBlock;
    private String nextBlock;
}
