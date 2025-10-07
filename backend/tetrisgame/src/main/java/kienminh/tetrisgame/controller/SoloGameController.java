package kienminh.tetrisgame.controller;


import kienminh.tetrisgame.dto.GameStateDTO;
import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.service.interfaces.GameService;
import kienminh.tetrisgame.util.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<GameStateDTO> action(@PathVariable Long playerId,
                                               @RequestParam String action) {
        try {
            GameState state;
            switch (action.toUpperCase()) {
                case "LEFT": state = soloGameService.moveLeft(playerId); break;
                case "RIGHT": state = soloGameService.moveRight(playerId); break;
                case "ROTATE": state = soloGameService.rotate(playerId); break;
                case "DROP": state = soloGameService.drop(playerId); break;
                case "TICK": state = soloGameService.tick(playerId); break;
                default: return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(GameMapper.toDTO(state));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(null); // game chưa start
        }
    }

    @GetMapping("/{playerId}/state")
    public ResponseEntity<GameStateDTO> state(@PathVariable Long playerId) {
        try {
            GameState state = soloGameService.getState(playerId);
            return ResponseEntity.ok(GameMapper.toDTO(state));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).build();
        }
    }
}
