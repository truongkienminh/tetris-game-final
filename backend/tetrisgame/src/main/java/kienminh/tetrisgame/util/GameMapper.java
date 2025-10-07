package kienminh.tetrisgame.util;

import kienminh.tetrisgame.dto.GameStateDTO;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.model.game.Block;

public final class GameMapper {
    private GameMapper() {}

    public static GameStateDTO toDTO(GameState state) {
        if (state == null) return null;
        int[][] boardSnapshot = state.getBoard() != null ? state.getBoard().getBoardSnapshot() : new int[0][0];
        String current = null;
        String next = null;
        Block cur = state.getBoard() != null ? state.getBoard().getCurrentBlock() : null;
        Block nx = state.getBoard() != null ? state.getBoard().getNextBlock() : null;
        if (cur != null) current = cur.getType().name();
        if (nx != null) next = nx.getType().name();
        return new GameStateDTO(boardSnapshot, state.getScore(), state.getLevel(), state.getStatus().name(), current, next);
    }
}
