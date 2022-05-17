package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.enums.PlayerPosition;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

@Getter
@Setter
public class Paddle {
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
    @Contract(pure = true)
    public Paddle(PlayerPosition p) {
        this();
        pos = p;
        z = switch (p) {
            case P1 -> -50;
            case P2 -> 50;
        };
    }
    public Paddle() {
        x_length = 10;
        y_length = 10;
        z_length = 10;
        x = 0;
        y = 0;
        z = 0;
        vx = 1;
        vy = 1;
        vz = 1;
        pos = PlayerPosition.P1;
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
        z += dt * vz;
        switch (playerControlState.leftRightState()) {
            case LEFT: {
                vx += dt*5;
                vx *= Math.exp(-dt);
            }
            case RIGHT: {
                vx *= Math.exp(-dt);
            }
            case BOTH:
            case NONE: {
                vx *= Math.exp(-dt);
            }
        }
        vy *= Math.exp(-dt);
        vz *= Math.exp(-dt);
    }
    public void singleAccelerate(double ax, double ay, double az) {
        vx += ax;
        vy += ay;
        vz += az;
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
