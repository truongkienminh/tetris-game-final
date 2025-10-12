package kienminh.tetrisgame.dto;

import kienminh.tetrisgame.model.entity.Player;
import lombok.Data;

@Data
public class PlayerDTO {
    private Long id;           // ✅ thêm id
    private String username;
    private boolean host;
    private boolean online;
    private int score;
    private int level;
    private String gameStatus; // PLAYING / GAME_OVER / WAITING

    public PlayerDTO(Player player) {
        this.id = player.getId(); // ✅ gán id của Player
        this.username = player.getUser().getUsername();
        this.host = player.isHost();
        this.online = player.isOnline();

        if (player.getGameState() != null) {
            this.score = player.getGameState().getScore();
            this.level = player.getGameState().getLevel();
            this.gameStatus = player.getGameState().getStatus() != null
                    ? player.getGameState().getStatus().name()
                    : "WAITING";
        } else {
            this.score = 0;
            this.level = 1;
            this.gameStatus = "WAITING";
        }
    }
}
