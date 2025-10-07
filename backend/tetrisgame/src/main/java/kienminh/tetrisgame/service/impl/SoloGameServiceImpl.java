package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.model.game.enums.GameStatus;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.UserRepository;
import kienminh.tetrisgame.service.interfaces.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service("soloGameService")
@RequiredArgsConstructor
@Transactional
public class SoloGameServiceImpl implements GameService {

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    // Lưu trạng thái game theo playerId (in-memory)
    private final Map<Long, GameState> gameStates = new ConcurrentHashMap<>();

    @Override
    public GameState startGame(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        GameState state = new GameState();
        state.start();

        gameStates.put(playerId, state); // lưu vào map
        return state;
    }

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
    public GameState tick(Long playerId) {
        GameState state = getState(playerId);
        state.tick();

        // Nếu game over, cập nhật lastScore của user
        if (state.getStatus() == GameStatus.GAME_OVER) {
            Player player = playerRepository.findById(playerId).orElseThrow();
            User user = player.getUser();
            user.setLastScore(state.getScore());
            userRepository.save(user);
            gameStates.remove(playerId); // xóa game đã kết thúc khỏi memory
        }

        return state;
    }

    @Override
    public boolean isGameOver(Long playerId) {
        GameState state = getState(playerId);
        return state.getStatus() == GameStatus.GAME_OVER;
    }

    @Override
    public GameState getState(Long playerId) {
        GameState state = gameStates.get(playerId);
        if (state == null) throw new IllegalStateException("Game not started");
        return state;
    }
}


