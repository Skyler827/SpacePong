package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.enums.PlayerPosition;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class Paddle {
    public static final double DEFAULT_X_LENGTH = 20;
    public static final double DEFAULT_Y_LENGTH = 20;
    public static final double DEFAULT_Z_LENGTH = 1;
    public static final int CONTROL_ACCELERATION = 15;
    public static final double DECAY_ACCELERATION_RT = 0.5;
    public static final double EPSILON = 0.1;
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
    public Paddle(PlayerPosition p, @NotNull SpaceBounds b, double xLength, double yLength, double zLength) {
        this();
        pos = p;
        bounds = b;
        z = switch (p) {
            case P1 -> b.ZMax();
            case P2 -> b.ZMin();
        };
    }
    public Paddle() {
        x_length = 30;
        y_length = 30;
        z_length = 30;
        x = 0;
        y = 0;
        z = 0;
        vx = 0;
        vy = 0;
        vz = 0;
        pos = PlayerPosition.P1;
        bounds = new SpaceBounds();
    }
    public double xMax() {return x+x_length/2;}
    public double xMin() {return x-x_length/2;}
    public double yMax() {return y+y_length/2;}
    public double yMin() {return y-y_length/2;}
    public double zMax() {return z+z_length/2;}
    public double zMin() {return z-z_length/2;}

    /**
     * simulates the motion of the paddle
     * @param dt number of seconds
     * @param playerControlState control inputs to the paddle
     */
    public void tick(double dt, PlayerControlState playerControlState) {
        x += dt * vx;
        y += dt * vy;
        if (x > bounds.XMax() + EPSILON) {
            x = bounds.XMax();
            vx = 0;
        } else if (x < bounds.XMin() - EPSILON) {
            x = bounds.XMin();
            vx = 0;
        } else {
            switch (playerControlState.leftRightState()) {
                case LEFT -> {
                    switch (pos) {
                        case P1 -> vx -= dt*CONTROL_ACCELERATION;
                        case P2 -> vx += dt*CONTROL_ACCELERATION;
                    }
                }
                case RIGHT -> {
                    switch (pos) {
                        case P1 -> vx += dt*CONTROL_ACCELERATION;
                        case P2 -> vx -= dt*CONTROL_ACCELERATION;
                    }
                }
                case BOTH, NONE -> {}
            }
            vx *= Math.exp(-dt * DECAY_ACCELERATION_RT);
        }
        if (y > bounds.YMax() + EPSILON) {
            y = bounds.YMax();
            vy = 0;
        } else if (y < bounds.YMin() - EPSILON) {
            y = bounds.YMin();
            vy = 0;
        } else {
            switch (playerControlState.upDownState()) {
                case UP -> vy += dt*CONTROL_ACCELERATION;
                case DOWN -> vy -= dt*CONTROL_ACCELERATION;
                case BOTH, NONE -> {}
            }
            vy *= Math.exp(-dt * DECAY_ACCELERATION_RT);
        }
    }
    public boolean ballInRange(@NotNull Ball b) {
        //get the box's closest point to sphere center by clamping
        double clampX = Math.max(this.xMin(), Math.min(b.getX(), this.xMax()));
        double clampY = Math.max(this.yMin(), Math.min(b.getY(), this.yMax()));
        double clampZ = Math.max(this.zMin(), Math.min(b.getZ(), this.zMax()));
        double distance = Math.sqrt(
            Math.pow(clampX-b.getX(),2) +
            Math.pow(clampY-b.getY(),2) +
            Math.pow(clampZ-b.getZ(),2)
        );
        return distance < b.getRadius();
    }

}
