package kienminh.tetrisgame.service.interfaces;

import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    RoomDTO createRoom(String roomName, User hostUser);
    RoomDTO joinRoom(Long roomId, Player player);
    void leaveRoom(Long roomId, Player player);
    void deleteRoom(Long roomId);
    List<RoomDTO> getAllRooms();
    Optional<RoomDTO> getRoomById(Long id);

}
