package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.PlayerDTO;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.service.interfaces.PlayerService;
import kienminh.tetrisgame.service.interfaces.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;
    private final AuthService authService;

    public PlayerController(PlayerService playerService, AuthService authService) {
        this.playerService = playerService;
        this.authService = authService;
    }

    /**
     * Tạo player mới hoặc lấy player hiện có cho user đang đăng nhập
     */
    @PostMapping("/create")
    public ResponseEntity<PlayerDTO> createPlayer() {
        User currentUser = authService.getAuthenticatedUser(); // lấy user hiện tại từ JWT
        Player player = playerService.createPlayer(currentUser);

        PlayerDTO dto = new PlayerDTO(
                currentUser.getUsername(),
                false,  // mặc định chưa phải host
                0       // điểm ban đầu
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Cập nhật trạng thái online/offline của player
     */
    @PutMapping("/{playerId}/status")
    public ResponseEntity<String> updateOnlineStatus(
            @PathVariable Long playerId,
            @RequestParam boolean online
    ) {
        playerService.setOnline(playerId, online);
        return ResponseEntity.ok("Player " + playerId + " is now " + (online ? "online" : "offline"));
    }

}
