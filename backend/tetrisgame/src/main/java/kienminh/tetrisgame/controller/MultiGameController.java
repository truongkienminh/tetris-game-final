package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.model.game.GameState;
import kienminh.tetrisgame.service.impl.MultiGameServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/multigame")
@RequiredArgsConstructor
public class MultiGameController {

    private final MultiGameServiceImpl multiGameService;

    @PostMapping("/start/{roomId}")
    public ResponseEntity<String> startGame(@PathVariable Long roomId) {
        multiGameService.startGameForRoom(roomId);
        return ResponseEntity.ok("Game started for room " + roomId);
    }

    @PostMapping("/update/{playerId}")
    public ResponseEntity<String> updateState(
            @PathVariable Long playerId,
            @RequestBody GameState state) {
        multiGameService.updatePlayerState(playerId, state);
        return ResponseEntity.ok("Updated game state for player " + playerId);
    }
}
