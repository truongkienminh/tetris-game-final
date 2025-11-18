// ============================================================
// FILE 1: MultiGameController.java
// ============================================================
package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.GameStateDTO;
import kienminh.tetrisgame.dto.RoomDTO;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.service.impl.MultiGameServiceImpl;
import kienminh.tetrisgame.util.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<GameStateDTO> moveLeft(@PathVariable Long playerId) {
        GameState state = multiGameService.moveLeft(playerId);
        return ResponseEntity.ok(GameMapper.toDTO(state));
    }

    @PostMapping("/player/{playerId}/moveRight")
    public ResponseEntity<GameStateDTO> moveRight(@PathVariable Long playerId) {
        GameState state = multiGameService.moveRight(playerId);
        return ResponseEntity.ok(GameMapper.toDTO(state));
    }

    @PostMapping("/player/{playerId}/rotate")
    public ResponseEntity<GameStateDTO> rotate(@PathVariable Long playerId) {
        GameState state = multiGameService.rotate(playerId);
        return ResponseEntity.ok(GameMapper.toDTO(state));
    }

    @PostMapping("/player/{playerId}/drop")
    public ResponseEntity<GameStateDTO> drop(@PathVariable Long playerId) {
        GameState state = multiGameService.drop(playerId);
        return ResponseEntity.ok(GameMapper.toDTO(state));
    }

    // ==============================================================
    // ⏱️ API: Tick thủ công từ người chơi
    // ==============================================================
    @PostMapping("/player/{playerId}/tick")
    public ResponseEntity<GameStateDTO> tick(@PathVariable Long playerId) {
        try {
            GameState state = multiGameService.tick(playerId);
            return ResponseEntity.ok(GameMapper.toDTO(state));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // ==============================================================
    // 📖 TRẠNG THÁI GAME
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

    @GetMapping("/room/{roomId}/states")
    public ResponseEntity<Map<Long, GameStateDTO>> getAllStatesByRoom(@PathVariable Long roomId) {
        Map<Long, GameState> states = multiGameService.getAllStatesByRoom(roomId);
        Map<Long, GameStateDTO> dtoMap = states.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> GameMapper.toDTO(e.getValue())));
        return ResponseEntity.ok(dtoMap);
    }

    @GetMapping("/player/{playerId}/isGameOver")
    public ResponseEntity<Boolean> isGameOver(@PathVariable Long playerId) {
        return ResponseEntity.ok(multiGameService.isGameOver(playerId));
    }

    // ==============================================================
    // ✅ NEW: Check room completion status
    // ==============================================================
    @GetMapping("/room/{roomId}/isComplete")
    public ResponseEntity<Boolean> isRoomComplete(@PathVariable Long roomId) {
        return ResponseEntity.ok(multiGameService.isRoomComplete(roomId));
    }

    @GetMapping("/room/{roomId}/rankings")
    public ResponseEntity<?> getRoomRankings(@PathVariable Long roomId) {
        return ResponseEntity.ok(multiGameService.getRoomRankings(roomId));
    }
}