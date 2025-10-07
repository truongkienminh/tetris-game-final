package kienminh.tetrisgame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameActionDTO {
    private String action; // move-left, move-right, rotate, drop
    private String roomId; // nếu solo thì null
}
