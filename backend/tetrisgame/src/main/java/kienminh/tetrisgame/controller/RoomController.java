package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.service.impl.PlayerServiceImpl;
import kienminh.tetrisgame.service.impl.RoomServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomServiceImpl roomService;
    private final PlayerServiceImpl playerService;

    /**
     * 🔹 Tạo phòng mới (JWT xác thực)
     */
    @PostMapping("/create")
    public ResponseEntity<RoomDTO> createRoom(
            @RequestParam String roomName,
            @AuthenticationPrincipal User currentUser
    ) {
        RoomDTO room = roomService.createRoom(roomName, currentUser);
        return ResponseEntity.ok(room);
    }

    /**
     * 🔹 Người chơi tham gia phòng
     */
    @PostMapping("/{roomId}/join")
    public ResponseEntity<RoomDTO> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser
    ) {
        Player player = playerService.getCurrentPlayer(currentUser);
        RoomDTO updatedRoom = roomService.joinRoom(roomId, player);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * 🔹 Người chơi rời phòng
     */
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<String> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal User currentUser
    ) {
        // Lấy Player hiện tại của người dùng
        Player player = playerService.getCurrentPlayer(currentUser);

        // Gọi service để rời phòng
        roomService.leaveRoom(roomId, player);
        return ResponseEntity.ok("Player " + currentUser.getUsername() + " left room " + roomId);
    }

    /**
     * 🔹 Xóa phòng (chỉ host hoặc admin)
     */
    @DeleteMapping("/{roomId}")
    public ResponseEntity<String> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.ok("Room deleted successfully");
    }

    /**
     * 🔹 Lấy danh sách tất cả phòng
     */
    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * 🔹 Lấy thông tin chi tiết 1 phòng
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long roomId) {
        return roomService.getRoomById(roomId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
