    package kienminh.tetrisgame.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

    // ===================== Room =====================
    @Entity
    @Table(name = "rooms")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Room {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @OneToOne
        private User host;

        @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<Player> players = new HashSet<>();

        // tiện ích thêm/xóa player
        public void addPlayer(Player player) {
            if (players == null) players = new HashSet<>();
            players.add(player);
            player.setRoom(this);
        }

        public void removePlayer(Player player) {
            if (players != null) {
                players.remove(player);
                player.setRoom(null);
            }
        }
    }

