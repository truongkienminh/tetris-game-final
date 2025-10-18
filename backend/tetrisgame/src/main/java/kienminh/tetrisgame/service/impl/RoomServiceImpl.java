package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.RoomRepository;
import kienminh.tetrisgame.service.interfaces.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    /**
     * 🔹 Tạo phòng mới, gán host là người tạo
     */
    @Override
    public RoomDTO createRoom(String roomName, User hostUser) {
        Room room = Room.builder()
                .name(roomName)
                .host(hostUser)
                .build();

        Player hostPlayer = Player.builder()
                .user(hostUser)
                .online(true)
                .host(true)
                .room(room)
                .build();

        room.getPlayers().add(hostPlayer);

        Room savedRoom = roomRepository.save(room);

        return convertToDTO(savedRoom);
    }

    /**
     * 🔹 Người chơi khác tham gia phòng
     */
    @Override
    public RoomDTO joinRoom(Long roomId, Player player) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        boolean alreadyJoined = room.getPlayers().stream()
                .anyMatch(p -> p.getUser().getId().equals(player.getUser().getId()));
        if (alreadyJoined) {
            throw new IllegalStateException("User already in this room");
        }

        room.addPlayer(player);
        playerRepository.save(player);
        Room updatedRoom = roomRepository.save(room);

        return convertToDTO(updatedRoom);
    }

    /**
     * 🔹 Người chơi rời phòng
     */
    @Override
    public void leaveRoom(Long roomId, Player player) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        room.removePlayer(player);
        playerRepository.delete(player);

        if (room.getPlayers().isEmpty()) {
            roomRepository.delete(room);
        } else {
            roomRepository.save(room);
        }
    }

    /**
     * 🔹 Xóa phòng theo ID
     */
    @Override
    public void deleteRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new IllegalArgumentException("Room not found");
        }
        roomRepository.deleteById(roomId);
    }

    /**
     * 🔹 Lấy danh sách tất cả phòng (trả về DTO)
     */
    @Override
    @Transactional(readOnly = true)
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 🔹 Lấy phòng theo ID (trả về DTO)
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RoomDTO> getRoomById(Long id) {
        return roomRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * 🔹 Hàm tiện ích chuyển Room → RoomDTO
     */
    private RoomDTO convertToDTO(Room room) {
        return RoomDTO.builder()
                .id(room.getId())
                .roomName(room.getName())
                .hostUsername(room.getHost() != null ? room.getHost().getUsername() : null)
                .players(room.getPlayers().stream()
                        .map(PlayerDTO::new)
                        .collect(Collectors.toList()))
                .build();
    }
}
