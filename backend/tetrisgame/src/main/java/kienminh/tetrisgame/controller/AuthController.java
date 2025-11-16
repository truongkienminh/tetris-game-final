package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.AuthRequest;
import kienminh.tetrisgame.dto.AuthResponse;
import kienminh.tetrisgame.dto.UserDTO;
import kienminh.tetrisgame.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Đăng ký tài khoản mới */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest req) {
        // 1️⃣ Tạo user mới
        UserDTO user = authService.register(req.getUsername(), req.getPassword());

        // 2️⃣ Tạo token JWT cho user mới
        String token = authService.login(req.getUsername(), req.getPassword());

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    /** Đăng nhập */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        // 1️⃣ Xác thực user và trả về token JWT
        String token = authService.login(req.getUsername(), req.getPassword());

        // 2️⃣ Lấy thông tin user từ service
        UserDTO user = authService.getCurrentUser(); // Hoặc getUserByUsername nếu cần

        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    /** Lấy thông tin người dùng hiện tại */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = authService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7); // loại bỏ "Bearer "
        authService.logout(token); // chỉ log token, không blacklist

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
