package kienminh.tetrisgame.service.interfaces;

import kienminh.tetrisgame.model.game.GameState;

public interface GameService {
    GameState startGame(Long playerId);
    GameState moveLeft(Long playerId);
    GameState moveRight(Long playerId);
    GameState rotate(Long playerId);
    GameState drop(Long playerId);
    GameState tick(Long playerId);
    boolean isGameOver(Long playerId);
    GameState getState(Long playerId);
}