package kienminh.tetrisgame.dto;

import kienminh.tetrisgame.model.game.GameState;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameUpdateDTO {
    private String username;
    private Long roomId;
    private GameState gameState; // trạng thái hiện tại của người chơi
}
