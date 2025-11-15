    package kienminh.tetrisgame.model.entity;

import jakarta.persistence.*;
import kienminh.tetrisgame.model.game.enums.RoomStatus;
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
        @JoinColumn(name = "host_id")
        private User host;

        private RoomStatus roomStatus;

        @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private Set<Player> players = new HashSet<>(); // đảm bảo luôn không null

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

