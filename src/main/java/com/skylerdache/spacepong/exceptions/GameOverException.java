package com.skylerdache.spacepong.exceptions;

import com.skylerdache.spacepong.enums.PlayerPosition;

public class GameOverException extends Throwable {
    public PlayerPosition winner;
    public GameOverException(PlayerPosition winner) {
        this.winner = winner;
    }
}
