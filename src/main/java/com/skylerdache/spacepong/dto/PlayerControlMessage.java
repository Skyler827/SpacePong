package com.skylerdache.spacepong.dto;

import com.skylerdache.spacepong.enums.LeftRightArrowState;
import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.enums.UpDownArrowState;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public record PlayerControlMessage (
    PlayerPosition playerPosition,
    LeftRightArrowState lrState,
    UpDownArrowState udState,
    long gameId,
    String player,
    Instant time
) {}

