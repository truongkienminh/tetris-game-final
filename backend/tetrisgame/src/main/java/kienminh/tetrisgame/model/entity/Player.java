package kienminh.tetrisgame.model.entity;

import jakarta.persistence.*;
import kienminh.tetrisgame.model.game.GameState;
import lombok.*;

import java.util.HashSet;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Transient
    private GameState gameState;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Builder.Default
    @Column(nullable = false)
    private boolean online = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean host = false;

    // Utility methods
    public void joinRoom(Room room) {
        this.room = room;
        if (room.getPlayers() == null) room.setPlayers(new HashSet<>());
        room.getPlayers().add(this);
    }

    public void leaveRoom() {
        if (this.room != null && this.room.getPlayers() != null) {
            this.room.getPlayers().remove(this);
        }
        this.room = null;
    }
}
