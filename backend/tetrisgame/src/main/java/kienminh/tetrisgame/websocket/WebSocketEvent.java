package kienminh.tetrisgame.websocket;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketEvent {
    private String type;       // "JOIN", "LEAVE", "START", "GAME_OVER", "UPDATE"
    private Long roomId;
    private Long playerId;
    private String username;
    private Object payload;    // chứa GameState, thông tin phòng, vv.
}
