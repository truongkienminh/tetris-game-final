package kienminh.tetrisgame.dto;

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
    private List<PlayerDTO> players; // danh sách người chơi trong phòng
}
