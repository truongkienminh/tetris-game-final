package com.kienminh.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
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
