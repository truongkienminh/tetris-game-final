package kienminh.tetrisgame.service.impl;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import kienminh.tetrisgame.repository.PlayerRepository;

import kienminh.tetrisgame.repository.UserRepository;
import kienminh.tetrisgame.service.interfaces.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    private UserRepository userRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player createPlayer(User user) {
        // Nếu user đã có Player thì trả về Player đó
        return playerRepository.findByUser(user)
                .orElseGet(() -> {
                    Player newPlayer = Player.builder()
                            .user(user)
                            .online(true)
                            .build();
                    return playerRepository.save(newPlayer);
                });
    }

    @Override
    public void setOnline(Long playerId, boolean online) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        if (player.isOnline() != online) {
            player.setOnline(online);
            playerRepository.save(player); // chỉ save khi trạng thái thay đổi
        }
    }

    @Override
    public Player getCurrentPlayer(User user) {
        return playerRepository.findByUser(user)
                .orElseGet(() -> createPlayer(user));
    }

    @Override
    public Player getPlayerByUserId(Long userId) {
        // Lấy User trước
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Lấy Player liên kết với User
        return playerRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Player not found for user id: " + userId));
    }

}

