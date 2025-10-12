package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.PlayerDTO;
import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.service.interfaces.PlayerService;
import kienminh.tetrisgame.service.interfaces.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;
    private final AuthService authService;
    private final PlayerRepository playerRepository;

    public PlayerController(PlayerService playerService,
                            AuthService authService,
                            PlayerRepository playerRepository) {
        this.playerService = playerService;
        this.authService = authService;
        this.playerRepository = playerRepository;
    }

    /**
     * 🔹 Tạo player mới hoặc lấy player hiện có cho user đang đăng nhập
     */
    @PostMapping("/create")
    public ResponseEntity<PlayerDTO> createPlayer() {
        User currentUser = authService.getAuthenticatedUser();
        Player player = playerService.createPlayer(currentUser);

        player.setOnline(true);
        player.setHost(false);
        playerRepository.save(player);

        return ResponseEntity.ok(new PlayerDTO(player));
    }

    /**
     * 🔹 Cập nhật trạng thái online/offline của player
     */
    @PutMapping("/{playerId}/status")
    public ResponseEntity<String> updateOnlineStatus(
            @PathVariable Long playerId,
            @RequestParam boolean online
    ) {
        playerService.setOnline(playerId, online);
        return ResponseEntity.ok("Player " + playerId + " is now " + (online ? "online" : "offline"));
    }

    /**
     * 🔹 Lấy player theo user đang đăng nhập (qua token JWT)
     */
    @GetMapping("/me")
    public ResponseEntity<PlayerDTO> getCurrentPlayer() {
        User currentUser = authService.getAuthenticatedUser(); // ✅ lấy user từ token
        Player player = playerService.getCurrentPlayer(currentUser); // tự tạo nếu chưa có
        return ResponseEntity.ok(new PlayerDTO(player));
    }
}
