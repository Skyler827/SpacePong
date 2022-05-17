package com.skylerdache.spacepong.game_elements;

public record SpaceBounds(
        double XMin,
        double XMax,
        double YMin,
        double YMax,
        double ZMin,
        double ZMax
) {
    /*
    Positive X is P1's right, P2's left
    Negative X is P1's left, P2's right
    Positive Y is up for both players
    Negative Y is down for both players
    Positive Z is towards P1, away from P2
    Negative Z is away from P1, towards P2
     */
    public SpaceBounds() {
        this(-50,50,0,100,-50,50);
    }
}
