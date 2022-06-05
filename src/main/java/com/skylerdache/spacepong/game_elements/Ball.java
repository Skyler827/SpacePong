package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.exceptions.PlayerScoreException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ball {
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
        this.resetPositionAndVelocity();
    }
    /**
     * simulates the motion of the ball
     * @param dt number of seconds
     * @param p1Paddle p1 paddle
     * @param p2Paddle p2 paddle
     * @throws PlayerScoreException if the ball goes out of bounds in a way that a player scores a point
     */
    public void tick(double dt, Paddle p1Paddle, Paddle p2Paddle) throws PlayerScoreException {
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
        if (z + dt*vz + radius > bounds.ZMax()) { //above max
            throw new PlayerScoreException(PlayerPosition.P2);
        } else
        if (z + dt*vz - radius < bounds.ZMin()) { //below min
            throw new PlayerScoreException(PlayerPosition.P1);
        } else { //within range:
            if (p1Paddle.ballInRange(this)) {
                z = 2*p1Paddle.zMin() - z - vz*dt;
                vz *= -1;
            } else if (p2Paddle.ballInRange(this)) {
                z = 2*p2Paddle.zMax() - z - vz*dt;
                vz *= -1;
            } else {
                z += dt * vz;
            }
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
