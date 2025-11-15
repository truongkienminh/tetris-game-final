package kienminh.tetrisgame.dto;

import kienminh.tetrisgame.model.game.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomDTO {
    private Long id;
    private String roomName;
    private String hostUsername;
    private List<PlayerDTO> players;
    private RoomStatus roomStatus;
}
