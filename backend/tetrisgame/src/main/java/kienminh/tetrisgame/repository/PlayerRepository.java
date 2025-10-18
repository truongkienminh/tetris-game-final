package kienminh.tetrisgame.repository;

import kienminh.tetrisgame.model.entity.Player;
import kienminh.tetrisgame.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByUser(User user);
    Optional<Player> findByUser_Username(String username);

}
