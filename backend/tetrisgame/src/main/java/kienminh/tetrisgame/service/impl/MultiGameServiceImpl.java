package kienminh.tetrisgame.service.impl;

import jakarta.annotation.PreDestroy;
import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.dto.RankingDTO;
import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.model.game.enums.GameStatus;
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
import java.util.stream.Collectors;

@Service("multiGameService")
@RequiredArgsConstructor
@Transactional
public class MultiGameServiceImpl implements GameService {

    private static final Logger logger = LoggerFactory.getLogger(MultiGameServiceImpl.class);

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;
    private final UserScoreService userScoreService;
    private final SimpMessagingTemplate messagingTemplate;

    /** 🧠 Game state for each player */
    private final Map<Long, GameState> playerStates = new ConcurrentHashMap<>();

    /** ⏱️ Scheduler for ticking all players */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    /** Scheduled tasks for each player */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /** Track finished players per room */
    private final Map<Long, Set<Long>> finishedPlayers = new ConcurrentHashMap<>();

    /** Cache rankings per room */
    private final Map<Long, List<RankingDTO>> roomRankings = new ConcurrentHashMap<>();

    /** Calculate fall speed based on level */
    private long getIntervalForLevel(int level) {
        return Math.max(200, 1000 - (level - 1) * 150);
    }

    // ==============================================================
    // 🎮 START MULTIPLAYER GAME
    // ==============================================================

    @Override
    public GameState startGame(Long playerId) {
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

        // ✅ Clear old game states for all players
        for (Player player : room.getPlayers()) {
            cancelTick(player.getId());
            playerStates.remove(player.getId());
        }

        // Initialize tracking for this room
        finishedPlayers.remove(roomId);
        roomRankings.remove(roomId);
        finishedPlayers.put(roomId, ConcurrentHashMap.newKeySet());

        // Create GameState for all players
        for (Player player : room.getPlayers()) {
            GameState state = new GameState();
            state.start();
            playerStates.put(player.getId(), state);
            scheduleTick(player.getId(), roomId);
        }

        room.setRoomStatus(RoomStatus.PLAYING);

        // Notify WebSocket
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "GAME_START",
                "roomId", roomId,
                "message", "Game started!"
        ));

        logger.info("🎮 Multiplayer game started in room {}", roomId);
        return convertToDTO(room);
    }

    // ==============================================================
    // ⏱️ TICK SCHEDULER
    // ==============================================================

    private void scheduleTick(Long playerId, Long roomId) {
        GameState state = playerStates.get(playerId);
        if (state == null) return;

        long interval = getIntervalForLevel(state.getLevel());
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                // Tick the game
                tick(playerId);

                // Get fresh state
                GameState currentState = playerStates.get(playerId);
                if (currentState == null) return;

                // ✅ IMMEDIATE game over check
                if (currentState.isGameOver()) {
                    handlePlayerGameOver(playerId, roomId);
                    return;
                }

                // Send tick update for ongoing game
                messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                        "type", "TICK_UPDATE",
                        "playerId", playerId,
                        "score", currentState.getScore(),
                        "level", currentState.getLevel(),
                        "status", currentState.getStatus().name(),
                        "board", currentState.getBoard().getBoardSnapshot(),
                        "nextBlock", currentState.getNextBlock()
                ));

            } catch (Exception e) {
                logger.error("❌ Tick error for player {}: {}", playerId, e.getMessage());
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
    // 🧱 PLAYER ACTIONS
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
        // Game over will be detected by scheduler
        return s;
    }

    // ==============================================================
    // 👀 HANDLE PLAYER GAME OVER
    // ==============================================================

    private void handlePlayerGameOver(Long playerId, Long roomId) {
        // ✅ Prevent duplicate processing
        Set<Long> finished = finishedPlayers.get(roomId);
        if (finished != null && finished.contains(playerId)) {
            logger.warn("⚠️ Player {} already marked as finished", playerId);
            return;
        }

        // Cancel the tick task
        cancelTick(playerId);

        // Get final game state
        GameState state = playerStates.get(playerId);
        if (state != null) {
            state.setStatus(GameStatus.GAME_OVER);

            // Save score
            try {
                userScoreService.saveScore(playerId, state.getScore());
                logger.info("✅ Saved score {} for player {}", state.getScore(), playerId);
            } catch (Exception e) {
                logger.error("❌ Failed to save score for player {}: {}", playerId, e.getMessage());
            }
        }

        // Mark as finished
        if (finished != null) {
            finished.add(playerId);
        }

        // Get player info
        Player player = playerRepository.findById(playerId).orElse(null);
        String playerName = player != null ? player.getUser().getUsername() : "Unknown";

        // Send final board snapshot
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "PLAYER_GAME_OVER",
                "playerId", playerId,
                "playerName", playerName,
                "score", state != null ? state.getScore() : 0,
                "level", state != null ? state.getLevel() : 1,
                "status", GameStatus.GAME_OVER.name(),
                "finalState", Map.of(
                        "board", state != null ? state.getBoard().getBoardSnapshot() : new int[20][10],
                        "score", state != null ? state.getScore() : 0,
                        "level", state != null ? state.getLevel() : 1
                )
        ));

        logger.info("🏁 Player {} ({}) finished with score {}", playerId, playerName,
                state != null ? state.getScore() : 0);

        // Check if room is complete
        if (isRoomGameOver(roomId)) {
            finishRoomGame(roomId);
        }
    }

    private boolean isRoomGameOver(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    Set<Long> finished = finishedPlayers.get(roomId);
                    if (finished == null) return false;

                    // Check if ALL players in the room have finished
                    int totalPlayers = room.getPlayers().size();
                    int finishedCount = (int) room.getPlayers().stream()
                            .map(Player::getId)
                            .filter(finished::contains)
                            .count();

                    boolean allFinished = finishedCount == totalPlayers && totalPlayers > 0;

                    logger.info("🔍 Room {} completion check: {}/{} players finished",
                            roomId, finishedCount, totalPlayers);

                    return allFinished;
                })
                .orElse(false);
    }

    private void finishRoomGame(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        // Calculate rankings
        List<RankingDTO> rankings = room.getPlayers().stream()
                .map(player -> {
                    GameState state = playerStates.get(player.getId());
                    int score = state != null ? state.getScore() : 0;
                    return new RankingDTO(
                            player.getId(),
                            player.getUser().getUsername(),
                            score
                    );
                })
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());

        // Cache rankings
        roomRankings.put(roomId, rankings);

        // Send rankings to all players
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "ROOM_GAME_OVER",
                "roomId", roomId,
                "rankings", rankings
        ));

        // Cleanup
        finishedPlayers.remove(roomId);

        logger.info("🏆 Room {} game finished! Rankings: {}",
                roomId,
                rankings.stream()
                        .map(r -> r.getUsername() + ":" + r.getScore())
                        .collect(Collectors.joining(", ")));
    }

    private void cancelTick(Long playerId) {
        ScheduledFuture<?> future = scheduledTasks.remove(playerId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    // ==============================================================
    // 📖 STATE RETRIEVAL
    // ==============================================================

    @Override
    public GameState getState(Long playerId) {
        GameState s = playerStates.get(playerId);
        if (s == null) throw new IllegalStateException("Game not started for player " + playerId);
        return s;
    }

    public GameState getGameState(Long playerId) {
        return playerStates.get(playerId);
    }

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
        GameState state = playerStates.get(playerId);
        return state != null && state.isGameOver();
    }

    // ==============================================================
    // ✅ NEW: Room completion queries
    // ==============================================================

    public boolean isRoomComplete(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> {
                    Set<Long> finished = finishedPlayers.get(roomId);
                    if (finished == null) return false;
                    return room.getPlayers().stream()
                            .map(Player::getId)
                            .allMatch(finished::contains);
                })
                .orElse(false);
    }

    public List<RankingDTO> getRoomRankings(Long roomId) {
        // Return cached rankings or compute if not complete
        List<RankingDTO> cached = roomRankings.get(roomId);
        if (cached != null) return cached;

        return roomRepository.findById(roomId)
                .map(room -> room.getPlayers().stream()
                        .map(player -> {
                            GameState state = playerStates.get(player.getId());
                            int score = state != null ? state.getScore() : 0;
                            return new RankingDTO(
                                    player.getId(),
                                    player.getUser().getUsername(),
                                    score
                            );
                        })
                        .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    // ==============================================================
    // 🧹 SHUTDOWN
    // ==============================================================

    @PreDestroy
    public void shutdown() {
        logger.info("🛑 Shutting down MultiGameService scheduler...");
        scheduler.shutdownNow();
    }

    // ==============================================================
    // 🔧 CONVERSION
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