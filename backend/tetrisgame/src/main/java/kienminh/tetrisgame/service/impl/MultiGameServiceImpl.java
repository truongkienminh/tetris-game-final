package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MultiGameServiceImpl {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** Bắt đầu game cho toàn bộ người chơi trong phòng */
    public void startGameForRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        room.getPlayers().forEach(p -> {
            GameState s = new GameState();
            s.start();
            p.setGameState(s);
            playerRepository.save(p);

            // gửi event START cho mọi người
            messagingTemplate.convertAndSend("/topic/room/" + roomId,
                    buildEvent("START", p, s));
        });
    }

    /** Cập nhật trạng thái của một người chơi */
    public void updatePlayerState(Long playerId, GameState state) {
        Player p = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        p.setGameState(state);
        playerRepository.save(p);

        Room room = roomRepository.findByPlayersContaining(p)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        messagingTemplate.convertAndSend("/topic/room/" + room.getId(),
                buildEvent("UPDATE", p, state));
    }

    private Object buildEvent(String type, Player p, GameState state) {
        return new Object() {
            public final String eventType = type;
            public final Long playerId = p.getId();
            public final String username = p.getUser().getUsername();
            public final GameState gameState = state;
        };
    }
}

