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

    public PlayerController(PlayerService playerService, AuthService authService, PlayerRepository playerRepository) {
        this.playerService = playerService;
        this.authService = authService;
        this.playerRepository = playerRepository;
    }

    /**
     * Tạo player mới hoặc lấy player hiện có cho user đang đăng nhập
     */
    @PostMapping("/create")
    public ResponseEntity<PlayerDTO> createPlayer() {
        // Lấy user hiện tại từ JWT
        User currentUser = authService.getAuthenticatedUser();

        // Tạo player mới hoặc lấy player đã tồn tại
        Player player = playerService.createPlayer(currentUser);

        // Đảm bảo player vừa tạo đang online
        player.setOnline(true);
        player.setHost(false); // mặc định chưa phải host
        playerRepository.save(player); // lưu player mới hoặc cập nhật

        // Chuyển thành DTO mới
        PlayerDTO dto = new PlayerDTO(player);

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
