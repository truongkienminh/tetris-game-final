// src/main/java/kienminh/tetrisgame/service/impl/UserScoreService.java
package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.repository.PlayerRepository;
import kienminh.tetrisgame.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserScoreService {
    private static final Logger logger = LoggerFactory.getLogger(UserScoreService.class);

    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveScore(Long playerId, int score) {
        // Fetch với join để tránh LazyInitializationException
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));

        // Lấy User qua repository để đảm bảo managed entity
        User user = userRepository.findById(player.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setLastScore(score);
        userRepository.save(user);

        logger.info("💾 Saved lastScore={} for user={}", score, user.getUsername());
    }
}
