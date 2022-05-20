package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.exceptions.PlayerScoreException;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
public class Ball {
    /** The percentage of each lateral dimension, centered in the midpoint,
     * that a ball being reset may randomly be located in */
    private static final double BOUNDS_RANGE_RANDOM_RESET = 0.8;
    private final double radius;
    private final SpaceBounds bounds;
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private double vx = 0;
    private double vy = 0;
    private double vz = 0;
    public Ball(double radius, SpaceBounds spaceBounds) {
        this.radius = radius;
        this.bounds = spaceBounds;
    }

    public Ball(double radius) {
        this(radius,new SpaceBounds());
        resetVelocity();
    }
    /**
     * simulates the motion of the ball
     * @param dt number of seconds
     * @param p1Paddle p1 paddle
     * @param p2Paddle p2 paddle
     * @throws PlayerScoreException if the ball goes out of bounds in a way that a player scores a point
     */
    public void tick(double dt, Paddle p1Paddle, Paddle p2Paddle) throws PlayerScoreException {
        double prevX = x;
        // bounce logic:
        // X axis (positive X is p1's right, p2's left) :
        if (x + dt*vx + radius > bounds.XMax()) { //above max, bounce down:
            x = 2*bounds.XMax() - x - vx*dt;
            vx = -vx;
        }
        else if (x + dt*vx - radius < bounds.XMin()) { //below min, bounce up:
            x = 2*bounds.XMin() - x - vx*dt;
            vx = -vx;
        } else { //within range:
            x += dt * vx;
        }
        // System.out.println("ball ticking: x from "+prevX+" to "+x);
        // Y axis (positive Y is up, negative Y is down):
        if (y + dt*vy + radius > bounds.YMax()) { //above max, bounce down:
            y = 2 * bounds.YMax() - y - vy*dt;
            vy = -vy;
        } else if (y + dt*vy - radius < bounds.YMin()) { //below min, bounce up:
            y = 2 * bounds.YMin() - y - vy*dt;
            vy = -vy;
        } else { //within range:
            y += dt * vy;
        }
        // Z axis (positive Z is towards P1, negative Z is towards P2):
        if (z + dt*vz + radius > bounds.ZMax()) { //above max, check for collision with p1's paddle:
            if (p1Paddle.ballInRange(this)) {
                z = 2*bounds.ZMax() - z - vz*dt;
            } else {
                throw new PlayerScoreException(PlayerPosition.P2);
            }
        } else if (z + dt*vz - radius < bounds.ZMin()) { //below min, check for collision with p2's paddle:
            if (p2Paddle.ballInRange(this)) {
                z = 2*bounds.ZMin() - z - vz*dt;
            } else {
                throw new PlayerScoreException(PlayerPosition.P1);
            }
        } else { //within range:
            z += dt * vz;
        }
    }
    public void resetPositionAndVelocity() {
        resetPosition();
        resetVelocity();
    }
    public void resetPosition() {
        this.x = bounds.centerX();
        this.y = bounds.centerY();
        this.z = bounds.centerZ();
    }
    public void randomizePosition() {
        this.x = (2*Math.random()-1)*(bounds.xRange()*BOUNDS_RANGE_RANDOM_RESET);
        this.y = (2*Math.random()-1)*(bounds.yRange()*BOUNDS_RANGE_RANDOM_RESET);
        this.z = (2*Math.random()-1)*(bounds.zRange()*BOUNDS_RANGE_RANDOM_RESET);
    }

    /**
     * The ball will have a random horizontal and vertical direction, but its direction toward
     * either paddle/score zone will be either +1 or -1; so one player or the other will score
     */
    public void resetVelocity() {
        this.vx = 2*Math.random()-1;
        this.vy = 2*Math.random()-1;
        //noinspection MagicNumber
        if (Math.random() > 0.5) {
            this.vz = 1;
        } else {
            this.vz = -1;
        }
    }

    public String getPosition() {
        return String.format("x: %1f, y: %1f, z: %1f",x, y, z);
    }
}
