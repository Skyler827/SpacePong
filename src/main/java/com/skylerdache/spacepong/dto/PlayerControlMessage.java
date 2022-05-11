package com.skylerdache.spacepong.dto;

import com.skylerdache.spacepong.enums.LeftRightArrowState;
import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.enums.UpDownArrowState;
import lombok.Data;

import java.time.Instant;

@Data
public class PlayerControlMessage {
    private PlayerPosition playerPosition;
    private LeftRightArrowState lrState;
    private UpDownArrowState udState;
    private long gameId;
    private String player;
    private Instant time;
}
