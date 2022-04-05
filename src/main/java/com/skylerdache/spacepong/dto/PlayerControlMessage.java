package com.skylerdache.spacepong.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class PlayerControlMessage {
    public enum LeftRightState {
        LEFT,
        RIGHT,
        BOTH,
        NONE
    }
    public enum UpDownState {
        UP,
        DOWN,
        BOTH,
        NONE
    }
    private LeftRightState lrState;
    private UpDownState udState;
    private long gameId;
    private String player;
    private Instant time;
}
