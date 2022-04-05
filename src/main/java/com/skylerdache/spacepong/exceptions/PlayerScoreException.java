package com.skylerdache.spacepong.exceptions;

public class PlayerScoreException extends Exception{
    public enum Player {P1, P2}
    public Player scorer;
    public PlayerScoreException(Player scorer) { this.scorer = scorer; }
}
