package kienminh.tetrisgame.model.entity;

import jakarta.persistence.*;
import kienminh.tetrisgame.model.game.GameState;
import lombok.*;

import java.util.HashSet;

// ===================== Player =====================
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
    private User user;

    @Transient // trạng thái game không lưu DB
    private GameState gameState;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private boolean online;

    // tiện ích thêm vào phòng
    public void joinRoom(Room room) {
        this.room = room;
        if (room.getPlayers() == null) {
            room.setPlayers(new HashSet<>());
        }
        room.getPlayers().add(this);
    }

    public void leaveRoom() {
        if (this.room != null && this.room.getPlayers() != null) {
            this.room.getPlayers().remove(this);
        }
        this.room = null;
    }
}

