package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.Room;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.service.interfaces.AuthService;
import kienminh.tetrisgame.service.interfaces.RoomService;
import kienminh.tetrisgame.service.interfaces.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final PlayerService playerService;
    private final AuthService authService;

    public RoomController(RoomService roomService,
                          PlayerService playerService,
                          AuthService authService) {
        this.roomService = roomService;
        this.playerService = playerService;
        this.authService = authService;
    }

    // 🟢 Tạo phòng (người đăng nhập là host)
    @PostMapping("/create")
    public ResponseEntity<RoomDTO> createRoom() {
        User host = authService.getAuthenticatedUser();
        if (host == null) return ResponseEntity.status(401).build();

        Room room = roomService.createRoom(host);
        RoomDTO dto = mapToDTO(room);
        return ResponseEntity.ok(dto);
    }

    // 🟢 Người chơi tham gia phòng
    @PostMapping("/{roomId}/join/{playerId}")
    public ResponseEntity<RoomDTO> joinRoom(@PathVariable Long roomId, @PathVariable Long playerId) {
        Room room = roomService.joinRoom(roomId, playerId);
        RoomDTO dto = mapToDTO(room);
        return ResponseEntity.ok(dto);
    }

    // 🟠 Người chơi rời khỏi phòng
    @PostMapping("/{roomId}/leave/{playerId}")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId, @PathVariable Long playerId) {
        roomService.leaveRoom(roomId, playerId);
        return ResponseEntity.ok().build();
    }

    // Lấy danh sách PlayerDTO trong phòng
    @GetMapping("/{roomId}/players")
    public ResponseEntity<List<PlayerDTO>> getPlayersInRoom(@PathVariable Long roomId) {
        List<PlayerDTO> players = roomService.getPlayerDTOsInRoom(roomId);
        return ResponseEntity.ok(players);
    }

    // 🧩 Mapping Entity → DTO
    private RoomDTO mapToDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getName(),
                room.getHost().getUsername(),
                room.getPlayers().stream()
                        .map(p -> new PlayerDTO(
                                p.getUser().getUsername(),
                                p.getUser().equals(room.getHost()),
                                p.getUser().getLastScore()))
                        .collect(Collectors.toList())
        );
    }
}
