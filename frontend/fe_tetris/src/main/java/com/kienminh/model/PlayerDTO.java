package com.kienminh.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private Long id;
    private String username;
    private boolean host;
    private boolean online;
    private int score;
    private int level;
    private String gameStatus;
}
