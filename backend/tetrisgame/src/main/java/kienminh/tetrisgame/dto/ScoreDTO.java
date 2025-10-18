package kienminh.tetrisgame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
class ScoreDTO {
private String username;
private int score;
private boolean gameOver;
}