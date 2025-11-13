package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.service.impl.MultiGameServiceImpl;
import kienminh.tetrisgame.util.GameMapper;
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
    // ⏱️ API: Tick thủ công từ người chơi
    // ==============================================================
    @PostMapping("/player/{playerId}/tick")
    public ResponseEntity<GameState> tick(@PathVariable Long playerId) {
        try {
            GameState state = multiGameService.tick(playerId);
            return ResponseEntity.ok(state);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(null);
        }
    }

    // ==============================================================
    // 🔍 TRẠNG THÁI GAME
    // ==============================================================
    @GetMapping("/player/{playerId}/state")
    public ResponseEntity<?> getPlayerState(@PathVariable Long playerId) {
        try {
            GameState state = multiGameService.getGameState(playerId);
            return ResponseEntity.ok(GameMapper.toDTO(state));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "Game not started"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage()));
        }
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
