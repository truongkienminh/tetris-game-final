package kienminh.tetrisgame.service.interfaces;

import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.entity.User;

import java.util.List;
import java.util.Set;

public interface RoomService {
    Room createRoom(User host);
    Room joinRoom(Long roomId, Long playerId);
    void leaveRoom(Long roomId, Long playerId);
    List<PlayerDTO> getPlayerDTOsInRoom(Long roomId);
}
