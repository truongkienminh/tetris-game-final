package kienminh.tetrisgame.service.impl;

import jakarta.annotation.PreDestroy;
import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.model.game.enums.RoomStatus;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.RoomRepository;
import kienminh.tetrisgame.service.interfaces.GameService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;

@Service("multiGameService")
@RequiredArgsConstructor
@Transactional
public class MultiGameServiceImpl implements GameService {

    private static final Logger logger = LoggerFactory.getLogger(MultiGameServiceImpl.class);

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final UserScoreService userScoreService;
    private final SimpMessagingTemplate messagingTemplate;

    /** 🧠 Trạng thái game của từng player */
    private final Map<Long, GameState> playerStates = new ConcurrentHashMap<>();

    /** ⏱️ Scheduler tick cho tất cả người chơi */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    /** Task đang chạy của từng player */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /** Tính tốc độ rơi dựa theo level */
    private long getIntervalForLevel(int level) {
        return Math.max(200, 1000 - (level - 1) * 150);
    }

    // ==============================================================
    // 🏁 BẮT ĐẦU GAME MULTIPLAYER
    // ==============================================================

    @Override
    public GameState startGame(Long playerId) {
        // Multiplayer không start riêng từng người chơi
        GameState state = playerStates.get(playerId);
        if (state == null) {
            state = new GameState();
            state.start();
            playerStates.put(playerId, state);
        }
        return state;
    }

    public RoomDTO startRoomGame(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Tạo GameState cho tất cả Player
        for (Player player : room.getPlayers()) {
            GameState state = new GameState();
            state.start();
            playerStates.put(player.getId(), state);
            scheduleTick(player.getId(), roomId);
        }
        room.setRoomStatus(RoomStatus.PLAYING);
        // Gửi thông báo WebSocket
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "GAME_START",
                "roomId", roomId
        ));

        logger.info("🎮 Multiplayer game started in room {}", roomId);
        return convertToDTO(room);
    }

    // ==============================================================
    // ⏱️ Tick cho từng Player
    // ==============================================================

    private void scheduleTick(Long playerId, Long roomId) {
        GameState state = playerStates.get(playerId);
        if (state == null) return;

        long interval = getIntervalForLevel(state.getLevel());
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                tick(playerId);

                // Gửi snapshot realtime cho frontend
                messagingTemplate.convertAndSend("/topic/player/" + playerId, Map.of(
                        "type", "TICK_UPDATE",
                        "playerId", playerId,
                        "state", playerStates.get(playerId)
                ));

                // Nếu người chơi game over
                if (state.isGameOver()) {
                    handlePlayerGameOver(playerId, roomId);
                }

            } catch (Exception e) {
                logger.error("Tick error for player {}", playerId, e);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);

        scheduledTasks.put(playerId, future);
    }

    @Override
    public GameState tick(Long playerId) {
        GameState s = getState(playerId);
        if (s.isGameOver()) return s;
        s.tick();
        return s;
    }

    // ==============================================================
    // 🧱 Các hành động từ người chơi
    // ==============================================================

    @Override
    public GameState moveLeft(Long playerId) {
        GameState s = getState(playerId);
        if (s.isGameOver()) return s;
        s.moveLeft();
        return s;
    }

    @Override
    public GameState moveRight(Long playerId) {
        GameState s = getState(playerId);
        if (s.isGameOver()) return s;
        s.moveRight();
        return s;
    }

    @Override
    public GameState rotate(Long playerId) {
        GameState s = getState(playerId);
        if (s.isGameOver()) return s;
        s.rotate();
        return s;
    }

    @Override
    public GameState drop(Long playerId) {
        GameState s = getState(playerId);
        if (s.isGameOver()) return s;
        s.drop();
        return s;
    }

    // ==============================================================
    // 💀 Khi 1 người chơi game over
    // ==============================================================

    private void handlePlayerGameOver(Long playerId, Long roomId) {
        cancelTick(playerId);

        GameState state = playerStates.get(playerId);
        if (state != null) {
            try {
                userScoreService.saveScore(playerId, state.getScore());
                logger.info("✅ Saved score {} for player {}", state.getScore(), playerId);
            } catch (Exception e) {
                logger.error("❌ Failed to save score for player {}", playerId, e);
            }
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "PLAYER_GAME_OVER",
                "playerId", playerId
        ));

        // Nếu tất cả người chơi đều game over → kết thúc room
        if (isRoomGameOver(roomId)) {
            messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                    "type", "ROOM_GAME_OVER",
                    "roomId", roomId
            ));
            logger.info("🏁 Room {} game over for all players", roomId);
        }
    }

    private boolean isRoomGameOver(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> room.getPlayers().stream()
                        .allMatch(p -> {
                            GameState s = playerStates.get(p.getId());
                            return s == null || s.isGameOver();
                        }))
                .orElse(true);
    }

    private void cancelTick(Long playerId) {
        ScheduledFuture<?> future = scheduledTasks.remove(playerId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    // ==============================================================
    // 🔍 Truy cập state
    // ==============================================================

    @Override
    public GameState getState(Long playerId) {
        GameState s = playerStates.get(playerId);
        if (s == null) throw new IllegalStateException("Game not started for player " + playerId);
        return s;
    }

    /** Trả về gameState hiện tại (nếu có), không ném exception. */
    public GameState getGameState(Long playerId) {
        return playerStates.get(playerId);
    }


    /** Lấy toàn bộ gameState của người chơi trong một phòng cụ thể. */
    public Map<Long, GameState> getAllStatesByRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        Map<Long, GameState> result = new HashMap<>();
        for (Player player : room.getPlayers()) {
            GameState state = playerStates.get(player.getId());
            if (state != null) {
                result.put(player.getId(), state);
            }
        }
        return result;
    }

    @Override
    public boolean isGameOver(Long playerId) {
        return getState(playerId).isGameOver();
    }

    // ==============================================================
    // 🧹 Shutdown
    // ==============================================================

    @PreDestroy
    public void shutdown() {
        logger.info("🛑 Shutting down MultiGameService scheduler...");
        scheduler.shutdownNow();
    }

    // ==============================================================
    // 🔧 Convert Room → DTO
    // ==============================================================

    private RoomDTO convertToDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .roomName(room.getName())
                .hostUsername(room.getHost().getUsername())
                .players(room.getPlayers().stream()
                        .map(PlayerDTO::new)
                        .toList())
                .roomStatus(room.getRoomStatus())
                .build();
    }
}
