package com.kienminh.model;

public class ActionDTO {
    private String action; // "moveLeft", "moveRight", "rotate", "drop"

    public ActionDTO(String action) {
        this.action = action;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
