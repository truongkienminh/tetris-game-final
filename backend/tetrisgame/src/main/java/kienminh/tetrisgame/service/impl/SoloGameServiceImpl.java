package kienminh.tetrisgame.service.impl;

import jakarta.annotation.PreDestroy;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.UserRepository;
import kienminh.tetrisgame.service.interfaces.GameService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service("soloGameService")
@RequiredArgsConstructor
public class SoloGameServiceImpl implements GameService {

    private static final Logger logger = LoggerFactory.getLogger(SoloGameServiceImpl.class);
    private final UserScoreService userScoreService;
    private final PlayerRepository playerRepository;

    /** Scheduler chung cho tất cả người chơi */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /** Trạng thái từng người chơi */
    private final Map<Long, GameState> gameStates = new ConcurrentHashMap<>();

    /** Task đang chạy tự động tick */
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /** Lưu interval hiện tại của mỗi player để kiểm tra */
    private final Map<Long, Long> currentIntervals = new ConcurrentHashMap<>();

    @Override
    public GameState startGame(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Tạo game mới
        GameState state = new GameState();
        state.start();
        gameStates.put(playerId, state);

        // Bắt đầu tick tự động
        scheduleTick(playerId);

        return state;
    }

    /** 🧭 Tính tốc độ rơi theo level */
    private long getIntervalForLevel(int level) {
        // Level càng cao -> rơi càng nhanh (min 200ms)
        return Math.max(200, 1000 - (level - 1) * 150);
    }

    /** ⏱️ Tạo hoặc cập nhật task tick cho player */
    private void scheduleTick(Long playerId) {
        GameState state = gameStates.get(playerId);
        if (state == null) return;

        // Hủy task cũ nếu có
        ScheduledFuture<?> oldTask = scheduledTasks.remove(playerId);
        if (oldTask != null && !oldTask.isCancelled()) {
            oldTask.cancel(false); // ✅ Đổi từ true → false để đợi task hoàn thành
        }

        long interval = getIntervalForLevel(state.getLevel());
        currentIntervals.put(playerId, interval);
        logger.info("▶ Start tick for player {} with interval {}ms (level {})",
                playerId, interval, state.getLevel());

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                GameState current = gameStates.get(playerId);
                if (current == null) return;

                if (current.isGameOver()) {
                    cancelTick(playerId);
                    return;
                }

                // ✅ KIỂM TRA TRƯỚC KHI TICK
                // Nếu level đã thay đổi, reschedule và KHÔNG tick lần này
                int currentLevel = current.getLevel();
                long expectedInterval = getIntervalForLevel(currentLevel);
                Long storedInterval = currentIntervals.get(playerId);

                if (storedInterval != null && expectedInterval != storedInterval) {
                    logger.info("⚡ Level {} reached -> rescheduling tick for player {}",
                            currentLevel, playerId);
                    cancelTick(playerId);
                    scheduleTick(playerId);
                    return; // ✅ RETURN sớm, không tick lần này
                }

                // ✅ CHỈ TICK MỘT LẦN
                tick(playerId);

            } catch (Exception e) {
                logger.error("Error in tick loop for player " + playerId, e);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);

        scheduledTasks.put(playerId, future);
    }

    /** 🧹 Hủy tick của player */
    private void cancelTick(Long playerId) {
        ScheduledFuture<?> future = scheduledTasks.remove(playerId);
        if (future != null && !future.isCancelled()) {
            future.cancel(false); // ✅ Đổi từ true → false
            logger.info("⏹️ Tick cancelled for player {}", playerId);
        }
        currentIntervals.remove(playerId);
    }

    /** 🧱 Tick logic */
    @Override
    public GameState tick(Long playerId) {
        GameState state = getState(playerId);
        state.tick();

        if (state.isGameOver()) {
            handleGameOver(playerId, state);
        }

        return state;
    }

    /** 💾 Khi game over */
    private void handleGameOver(Long playerId, GameState state) {
        try {
            logger.info("🎮 Attempting to save score {} for player {}", state.getScore(), playerId);
            userScoreService.saveScore(playerId, state.getScore());
            logger.info("✅ Score saved successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to save score for player {}: {}", playerId, e.getMessage(), e);
        }

        gameStates.remove(playerId);
        cancelTick(playerId);
        logger.info("💀 Game over for player {}", playerId);
    }

    // --- Các hành động từ người chơi ---
    @Override
    public GameState moveLeft(Long playerId) {
        GameState state = getState(playerId);
        state.moveLeft();
        return state;
    }

    @Override
    public GameState moveRight(Long playerId) {
        GameState state = getState(playerId);
        state.moveRight();
        return state;
    }

    @Override
    public GameState rotate(Long playerId) {
        GameState state = getState(playerId);
        state.rotate();
        return state;
    }

    @Override
    public GameState drop(Long playerId) {
        GameState state = getState(playerId);
        state.drop();
        return state;
    }

    @Override
    public boolean isGameOver(Long playerId) {
        return getState(playerId).isGameOver();
    }

    @Override
    public GameState getState(Long playerId) {
        GameState state = gameStates.get(playerId);
        if (state == null) throw new IllegalStateException("Game not started for player " + playerId);
        return state;
    }

    @PreDestroy
    public void shutdown() {
        logger.info("🛑 Shutting down SoloGameService scheduler...");
        scheduler.shutdownNow();
    }
}