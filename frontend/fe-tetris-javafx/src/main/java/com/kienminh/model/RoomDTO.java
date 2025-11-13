package com.kienminh.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class RoomDTO {
    private Long id;
    @JsonProperty("roomName")
    private String name;

    @JsonProperty("hostUsername")
    private String host;

    private Set<PlayerDTO> players;

    // getters và setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Set<PlayerDTO> getPlayers() { return players; }
    public void setPlayers(Set<PlayerDTO> players) { this.players = players; }
}
