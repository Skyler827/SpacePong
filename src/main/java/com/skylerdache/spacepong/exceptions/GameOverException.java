package com.skylerdache.spacepong.exceptions;

import com.skylerdache.spacepong.enums.GameOverReason;
import com.skylerdache.spacepong.enums.PlayerPosition;

public class GameOverException extends Throwable {
    public PlayerPosition winner;
    public int p1Score;
    public int p2Score;
    public GameOverReason reason;

    public GameOverException(PlayerPosition winner, int p1Score, int p2Score, GameOverReason reason) {
        this.winner = winner;
        this.p1Score = p1Score;
        this.p2Score = p2Score;
        this.reason = reason;
    }
}
