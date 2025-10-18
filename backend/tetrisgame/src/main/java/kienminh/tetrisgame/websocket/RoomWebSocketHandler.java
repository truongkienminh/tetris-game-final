package kienminh.tetrisgame.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.RoomRepository;
import kienminh.tetrisgame.service.interfaces.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class RoomWebSocketHandler {

    private final RoomService roomService;
    private final PlayerRepository playerRepository;
    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ReentrantLock gameLock = new ReentrantLock();

    /** 🔹 Player tham gia phòng */
    @MessageMapping("/room/join/{roomId}/{playerId}")
    public void joinRoom(@DestinationVariable Long roomId,
                         @DestinationVariable Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Service giờ trả về RoomDTO
        RoomDTO roomDTO = roomService.joinRoom(roomId, player);

        WebSocketEvent event = WebSocketEvent.builder()
                .type("JOIN")
                .roomId(roomId)
                .playerId(playerId)
                .payload(new PlayerDTO(player)) // gửi thông tin player mới
                .build();

        // gửi cho tất cả client trong room
        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

    /** 🔹 Player rời phòng */
    @MessageMapping("/room/leave/{roomId}/{playerId}")
    public void leaveRoom(@DestinationVariable Long roomId,
                          @DestinationVariable Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Gọi service xử lý
        roomService.leaveRoom(roomId, player);

        WebSocketEvent event = WebSocketEvent.builder()
                .type("LEAVE")
                .roomId(roomId)
                .playerId(playerId)
                .payload(new PlayerDTO(player))
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

    /** 🔹 Host bắt đầu game */
    @MessageMapping("/room/start/{roomId}")
    public void startGame(@DestinationVariable Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // reset trạng thái cho tất cả player trong phòng
        room.getPlayers().forEach(p -> {
            GameState state = new GameState();
            state.start();
            p.setGameState(state);
        });

        roomRepository.save(room); // lưu lại trạng thái mới

        // Chuyển danh sách player thành DTO
        List<PlayerDTO> players = room.getPlayers().stream()
                .map(PlayerDTO::new)
                .collect(Collectors.toList());

        WebSocketEvent event = WebSocketEvent.builder()
                .type("START")
                .roomId(roomId)
                .payload(players)
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, event);
    }

    /** 🔹 Player cập nhật gameState */
    @MessageMapping("/game/update/{playerId}")
    public void updateGame(@DestinationVariable Long playerId,
                           @Payload GameState gameState) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Player player = playerRepository.findById(playerId)
                .filter(p -> p.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Unauthorized player"));

        Room room = player.getRoom();
        if (room == null) {
            throw new IllegalArgumentException("Player not in any room");
        }

        gameLock.lock();
        try {
            if (player.getGameState() == null) {
                player.setGameState(new GameState());
            }
            player.getGameState().setBoard(gameState.getBoard());
            player.getGameState().setScore(gameState.getScore());
            player.getGameState().setLevel(gameState.getLevel());
            playerRepository.save(player);
        } finally {
            gameLock.unlock();
        }

        WebSocketEvent event = WebSocketEvent.builder()
                .type("UPDATE")
                .roomId(room.getId())
                .playerId(playerId)
                .username(username)
                .payload(new PlayerDTO(player))
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), event);
    }

    /** 🔹 Player thua game */
    @MessageMapping("/game/over/{playerId}")
    public void gameOver(@DestinationVariable Long playerId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Player player = playerRepository.findById(playerId)
                .filter(p -> p.getUser().getUsername().equals(username))
                .orElseThrow(() -> new IllegalArgumentException("Unauthorized player"));

        Room room = player.getRoom();
        if (room == null) {
            throw new IllegalArgumentException("Room not found for this player");
        }

        player.setOnline(false);
        playerRepository.save(player);

        WebSocketEvent event = WebSocketEvent.builder()
                .type("GAME_OVER")
                .roomId(room.getId())
                .playerId(playerId)
                .username(username)
                .payload(new PlayerDTO(player))
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), event);

        boolean allOver = room.getPlayers().stream().allMatch(p -> !p.isOnline());
        if (allOver) {
            WebSocketEvent endEvent = WebSocketEvent.builder()
                    .type("ROOM_OVER")
                    .roomId(room.getId())
                    .build();
            messagingTemplate.convertAndSend("/topic/room/" + room.getId(), endEvent);
        }
    }
}
