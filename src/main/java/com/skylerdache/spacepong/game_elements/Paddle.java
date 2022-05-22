package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.enums.PlayerPosition;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

@Getter
@Setter
public class Paddle {
    public static final int CONTROL_ACCELERATION = 15;
    public static final double DECAY_ACCELERATION_RT = 0.5;
    private final double x_length;
    private final double y_length;
    private final double z_length;
    private double x;
    private double y;
    private double z;
    private double vx;
    private double vy;
    private double vz;
    private PlayerPosition pos;
    private SpaceBounds bounds;
    @Contract(pure = true)
    public Paddle(PlayerPosition p, SpaceBounds b) {
        this();
        pos = p;
        bounds = b;
        z = switch (p) {
            case P1 -> b.ZMax();
            case P2 -> b.ZMin();
        };
    }
    public Paddle() {
        x_length = 10;
        y_length = 10;
        z_length = 10;
        x = 0;
        y = 0;
        z = 0;
        vx = 0;
        vy = 0;
        vz = 0;
        pos = PlayerPosition.P1;
        bounds = new SpaceBounds();
    }
    private double xMax() {return x+x_length/2;}
    private double xMin() {return x-x_length/2;}
    private double yMax() {return y+y_length/2;}
    private double yMin() {return y-y_length/2;}

    /**
     * simulates the motion of the paddle
     * @param dt number of seconds
     * @param playerControlState control inputs to the paddle
     */
    public void tick(double dt, PlayerControlState playerControlState) {
        x += dt * vx;
        y += dt * vy;
        if (x > bounds.XMax()) x = bounds.XMax();
        if (x < bounds.XMin()) x = bounds.XMin();
        if (y > bounds.YMax()) y = bounds.YMax();
        if (y < bounds.YMin()) y = bounds.YMin();
        switch (playerControlState.leftRightState()) {
            case LEFT -> {
                switch (pos) {
                    case P1 -> {vx -= dt*CONTROL_ACCELERATION;}
                    case P2 -> {vx += dt*CONTROL_ACCELERATION;}
                }
            }
            case RIGHT -> {
                switch (pos) {
                    case P1 -> {vx += dt*CONTROL_ACCELERATION;}
                    case P2 -> {vx -= dt*CONTROL_ACCELERATION;}
                }
            }
            case BOTH, NONE -> {}
        }
        switch (playerControlState.upDownState()) {
            case UP -> { vy += dt*CONTROL_ACCELERATION; }
            case DOWN -> { vy -= dt*CONTROL_ACCELERATION; }
            case BOTH, NONE -> {}
        }
        vx *= Math.exp(-dt * DECAY_ACCELERATION_RT);
        vy *= Math.exp(-dt * DECAY_ACCELERATION_RT);
    }
    public boolean ballInRange(Ball b) {
        if (b.getX() > this.xMax()
            || b.getX() < this.xMin()
            || b.getY() > this.yMax()
            || b.getY() < this.yMin()
        ) { return false; }
        else { return true; }
    }
}
