package com.kienminh.model;

public class UserDTO {
    private Long id;
    private String username;
    private int lastScore;

    // getters và setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getLastScore() { return lastScore; }
    public void setLastScore(int lastScore) { this.lastScore = lastScore; }
}
