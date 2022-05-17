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
    private static final double DEFAULT_X_MAX = 50;
    private static final double DEFAULT_X_MIN = -50;
    private static final double DEFAULT_Y_MAX = 100;
    private static final double DEFAULT_Y_MIN = 0;
    private static final double DEFAULT_Z_MAX = 50;
    private static final double DEFAULT_Z_MIN = -50;
    public SpaceBounds() {
        this(DEFAULT_X_MIN,DEFAULT_X_MAX,DEFAULT_Y_MIN,DEFAULT_Y_MAX,DEFAULT_Z_MIN,DEFAULT_Z_MAX);
    }
    public double xRange() { return XMax - XMin; }
    public double yRange() { return YMax - YMin; }
    public double zRange() { return ZMax - ZMin; }

    public double centerX() { return XMax/2 + XMin/2;}
    public double centerY() { return YMax/2 + YMin/2;}
    public double centerZ() { return ZMax/2 + ZMin/2;}
}
