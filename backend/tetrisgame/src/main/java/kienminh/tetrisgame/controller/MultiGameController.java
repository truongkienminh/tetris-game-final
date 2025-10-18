package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.service.impl.MultiGameServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/multigame")
@RequiredArgsConstructor
public class MultiGameController {

    private final MultiGameServiceImpl multiGameService;

    // ==============================================================
    // 🎮 BẮT ĐẦU GAME TRONG PHÒNG
    // ==============================================================
    @PostMapping("/start/{roomId}")
    public ResponseEntity<RoomDTO> startRoomGame(@PathVariable Long roomId) {
        RoomDTO room = multiGameService.startRoomGame(roomId);
        return ResponseEntity.ok(room);
    }

    // ==============================================================
    // 🧱 HÀNH ĐỘNG CỦA NGƯỜI CHƠI
    // ==============================================================
    @PostMapping("/player/{playerId}/moveLeft")
    public ResponseEntity<GameState> moveLeft(@PathVariable Long playerId) {
        return ResponseEntity.ok(multiGameService.moveLeft(playerId));
    }

    @PostMapping("/player/{playerId}/moveRight")
    public ResponseEntity<GameState> moveRight(@PathVariable Long playerId) {
        return ResponseEntity.ok(multiGameService.moveRight(playerId));
    }

    @PostMapping("/player/{playerId}/rotate")
    public ResponseEntity<GameState> rotate(@PathVariable Long playerId) {
        return ResponseEntity.ok(multiGameService.rotate(playerId));
    }

    @PostMapping("/player/{playerId}/drop")
    public ResponseEntity<GameState> drop(@PathVariable Long playerId) {
        return ResponseEntity.ok(multiGameService.drop(playerId));
    }

    // ==============================================================
    // 🔍 TRẠNG THÁI GAME
    // ==============================================================
    @GetMapping("/player/{playerId}/state")
    public ResponseEntity<GameState> getPlayerState(@PathVariable Long playerId) {
        GameState state = multiGameService.getGameState(playerId);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }

    @GetMapping("/states")
    public ResponseEntity<Map<Long, GameState>> getAllStates() {
        return ResponseEntity.ok(multiGameService.getAllStates());
    }

    @GetMapping("/room/{roomId}/states")
    public ResponseEntity<Map<Long, GameState>> getAllStatesByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(multiGameService.getAllStatesByRoom(roomId));
    }

    @GetMapping("/player/{playerId}/isGameOver")
    public ResponseEntity<Boolean> isGameOver(@PathVariable Long playerId) {
        return ResponseEntity.ok(multiGameService.isGameOver(playerId));
    }
}
