package com.skylerdache.spacepong.game_elements;

public record SpaceBounds(
        double XMin,
        double XMax,
        double YMin,
        double YMax,
        double ZMin,
        double ZMax
) {
    public SpaceBounds() {
        this(0,100,0,100,0,100);
    }
}
