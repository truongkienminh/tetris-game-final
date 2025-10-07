package kienminh.tetrisgame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.service.impl.MultiGameServiceImpl;
import kienminh.tetrisgame.service.impl.RoomServiceImpl;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomWebSocketHandler {

    private final MultiGameServiceImpl multiGameService;
    private final RoomServiceImpl roomService;
    private final PlayerRepository playerRepository; // ✅ inject trực tiếp repo
    private final RoomRepository roomRepository;     // ✅ inject trực tiếp repo
    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    /** Khi người chơi tham gia phòng */
    @MessageMapping("/room/join/{roomId}/{playerId}")
    public void joinRoom(@DestinationVariable Long roomId, @DestinationVariable Long playerId) {
        Room room = roomService.joinRoom(roomId, playerId);

        WebSocketEvent event = WebSocketEvent.builder()
                .type("JOIN")
                .roomId(roomId)
                .playerId(playerId)
                .payload(room)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

    /** Khi người chơi rời phòng */
    @MessageMapping("/room/leave/{roomId}/{playerId}")
    public void leaveRoom(@DestinationVariable Long roomId, @DestinationVariable Long playerId) {
        roomService.leaveRoom(roomId, playerId);

        WebSocketEvent event = WebSocketEvent.builder()
                .type("LEAVE")
                .roomId(roomId)
                .playerId(playerId)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

    /** Khi host bắt đầu trận */
    @MessageMapping("/room/start/{roomId}")
    public void startGame(@DestinationVariable Long roomId) {
        WebSocketEvent event = WebSocketEvent.builder()
                .type("START")
                .roomId(roomId)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

    /** Khi người chơi cập nhật trạng thái (move, drop, tick...) */
    @MessageMapping("/game/update/{playerId}")
    public void updateGame(@DestinationVariable Long playerId, @Payload GameState gameState) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Room room = roomRepository.findByPlayersContaining(player)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        player.setGameState(gameState);
        playerRepository.save(player);

        WebSocketEvent event = WebSocketEvent.builder()
                .type("UPDATE")
                .roomId(room.getId())
                .playerId(playerId)
                .username(player.getUser().getUsername())
                .payload(gameState)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), event);
    }

    /** Khi người chơi thua */
    @MessageMapping("/game/over/{playerId}")
    public void gameOver(@DestinationVariable Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        Room room = roomRepository.findByPlayersContaining(player)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        WebSocketEvent event = WebSocketEvent.builder()
                .type("GAME_OVER")
                .roomId(room.getId())
                .playerId(playerId)
                .username(player.getUser().getUsername())
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), event);
    }
}
