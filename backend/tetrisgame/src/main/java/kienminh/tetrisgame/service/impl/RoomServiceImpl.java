package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.RoomRepository;
import kienminh.tetrisgame.service.interfaces.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final PlayerRepository playerRepository;

    public RoomServiceImpl(RoomRepository roomRepository, PlayerRepository playerRepository) {
        this.roomRepository = roomRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public Room createRoom(User host) {
        // 1️⃣ Tạo room
        Room room = Room.builder()
                .name(host.getUsername() + "'s Room")
                .host(host)
                .build();

        // 2️⃣ Khởi tạo player cho host
        Player hostPlayer = Player.builder()
                .user(host)
                .online(true) // host mặc định online   ư
                .room(room)   // gán phòng
                .build();

        // 3️⃣ Thêm host player vào room
        room.getPlayers().add(hostPlayer);

        // 4️⃣ Lưu cả room và player
        playerRepository.save(hostPlayer);
        return roomRepository.save(room);
    }


    @Override
    public Room joinRoom(Long roomId, Long playerId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Sử dụng method tiện ích trong Room/Player → đồng bộ 2 chiều
        room.addPlayer(player);

        return roomRepository.save(room);
    }

    @Override
    public void leaveRoom(Long roomId, Long playerId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Sử dụng method tiện ích → đồng bộ quan hệ hai chiều
        room.removePlayer(player);

        roomRepository.save(room);
    }

    @Override
    public List<PlayerDTO> getPlayerDTOsInRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        return room.getPlayers().stream()
                .map(p -> new PlayerDTO(
                        p.getUser().getUsername(),
                        p.isOnline(),
                        p.getUser().getLastScore()
                ))
                .collect(Collectors.toList());
    }

}

