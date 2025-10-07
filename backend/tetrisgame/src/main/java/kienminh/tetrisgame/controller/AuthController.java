package kienminh.tetrisgame.controller;

import kienminh.tetrisgame.dto.AuthRequest;
import kienminh.tetrisgame.dto.AuthResponse;
import kienminh.tetrisgame.dto.UserDTO;
import kienminh.tetrisgame.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest req) {
        UserDTO user = authService.register(req.getUsername(), req.getPassword());
        String token = authService.login(req.getUsername(), req.getPassword());
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        // Lấy user từ DB để trả đầy đủ thông tin
        UserDTO user = authService.getCurrentUser();
        return ResponseEntity.ok(new AuthResponse(token, user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = authService.getCurrentUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(user);
    }
}

