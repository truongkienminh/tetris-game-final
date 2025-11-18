package kienminh.tetrisgame.dto;

public class RankingDTO {
        private Long playerId;
        private String username;
        private int score;

        public RankingDTO(Long playerId, String username, int score) {
            this.playerId = playerId;
            this.username = username;
            this.score = score;
        }

        // Getters
        public Long getPlayerId() { return playerId; }
        public String getUsername() { return username; }
        public int getScore() { return score; }
    }