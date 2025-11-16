package kienminh.tetrisgame.service.interfaces;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;

public interface PlayerService {
    Player createPlayer(User user);
    void setOnline(Long playerId, boolean online);
    Player getCurrentPlayer(User user);
    Player getPlayerByUserId(Long userId);
}
