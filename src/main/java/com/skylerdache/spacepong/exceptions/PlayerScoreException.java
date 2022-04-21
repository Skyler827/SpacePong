package com.skylerdache.spacepong.exceptions;

import com.skylerdache.spacepong.enums.PlayerPosition;

public class PlayerScoreException extends Exception{
    public PlayerPosition scorer;
    public PlayerScoreException(PlayerPosition scorer) { this.scorer = scorer; }
}
