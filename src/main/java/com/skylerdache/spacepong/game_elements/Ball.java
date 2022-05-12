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
    private double vx = 1;
    private double vy = 1;
    private double vz = 1;
    public Ball(double radius, SpaceBounds spaceBounds) {
        this.radius = radius;
        this.bounds = spaceBounds;
    }
    public void tick(double dt, Paddle p1Paddle, Paddle p2Paddle) throws PlayerScoreException {
        // bounce logic:
        // X axis (positive X is p1's right, p2's left) :
        if (x + dt*vx + radius > bounds.XMax()) { //above max, bounce down:
            x = 2*bounds.XMax() - x - vx*dt;
        }
        else if (x + dt*vx - radius < bounds.XMin()) { //below min, bounce up:
            x = 2*bounds.XMin() - x - vx*dt;
        } else { //within range:
            x += dt * vx;
        }
        // Y axis (positive Y is up, negative Y is down):
        if (y + dt*vy + radius > bounds.YMax()) { //above max, bounce down:
            y = 2 * bounds.YMax() - y - vy*dt;
        } else if (y + dt*vy - radius < bounds.YMin()) { //below min, bounce down:
            y = 2 * bounds.YMin() - y - vy*dt;
        } else { //within range:
            y += dt * vy;
        }
        // Z axis (positive Z is towards P2, negative Z is towards P1):
        if (z + dt*vz + radius > bounds.ZMax()) { //above max, check for collision with p2's paddle:
            if (p2Paddle.ballInRange(this)) {
                z = 2*bounds.ZMax() - z - vz*dt;
            } else {
                throw new PlayerScoreException(PlayerPosition.P1);
            }
        } else if (z + dt*vz - radius < bounds.ZMin()) { //below min, check for collision with p1's paddle:
            if (p1Paddle.ballInRange(this)) {
                z = 2*bounds.ZMin() - z - vz*dt;
            } else {
                throw new PlayerScoreException(PlayerPosition.P2);
            }
        } else { //within range:
            z += dt * vz;
        }
    }
    public void resetPosition() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public String getPosition() {
        return String.format("x: %1f, y: %1f, z: %1f",x, y, z);
    }
}
