package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.GameStateDTO;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.service.interfaces.GameService;
import kienminh.tetrisgame.util.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/solo")
@RequiredArgsConstructor
public class SoloGameController {

    private final GameService soloGameService;

    @PostMapping("/{playerId}/start")
    public ResponseEntity<GameStateDTO> start(@PathVariable Long playerId) {
        GameState state = soloGameService.startGame(playerId);
        return ResponseEntity.ok(GameMapper.toDTO(state));
    }


    @PostMapping("/{playerId}/action")
    public ResponseEntity<?> action(@PathVariable Long playerId,
                                    @RequestParam String action) {
        try {
            if (action == null || action.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Action cannot be empty"));
            }

            GameState state;
            switch (action.trim().toUpperCase()) {
                case "LEFT": state = soloGameService.moveLeft(playerId); break;
                case "RIGHT": state = soloGameService.moveRight(playerId); break;
                case "ROTATE": state = soloGameService.rotate(playerId); break;
                case "DROP": state = soloGameService.drop(playerId); break;
                case "TICK":
                    // Manual tick (không bắt buộc, chỉ để test)
                    state = soloGameService.tick(playerId);
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid action: " + action));
            }

            return ResponseEntity.ok(GameMapper.toDTO(state));

        } catch (IllegalStateException e) {
            // Game chưa start
            return ResponseEntity.status(400)
                    .body(Map.of("error", "Game not started"));
        }
    }

    @GetMapping("/{playerId}/state")
    public ResponseEntity<?> state(@PathVariable Long playerId) {
        try {
            GameState state = soloGameService.getState(playerId);
            return ResponseEntity.ok(GameMapper.toDTO(state));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("error", "Game not started"));
        }
    }
}
