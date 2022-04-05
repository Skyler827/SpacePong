package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.exceptions.PlayerScoreException;
import com.skylerdache.spacepong.runnables.GameThread;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ball {
    private final GameThread gameThread;
    private final double radius;
    private double x = 0;
    private double y = 0;
    private double z = 0;
    private double vx = 1;
    private double vy = 1;
    private double vz = 1;
    public Ball(GameThread gameThread, double radius) {
        this.gameThread = gameThread;
        this.radius = radius;
    }
    public void tick(double dt) throws PlayerScoreException {
        //bounce logic:
        if (x+dt*vx > GameThread.X_MAX) {
            x = 2*GameThread.X_MAX - x - vx*dt;
        }
        else if (x+dt*vx < GameThread.X_MIN) {
            x = 2*GameThread.X_MIN - x - vx*dt;
        } else {
            x += dt * vx;
        }
        if (y+dt*vy > GameThread.Y_MAX) {
            y = 2*GameThread.Y_MAX - y - vy*dt;
        } else if (y+dt*vy < GameThread.Y_MIN) {
            y = 2*GameThread.Y_MIN - y - vy*dt;
        } else {
            y += dt * vy;
        }
        if (z + dt*vz > GameThread.Z_MAX) {
            z = 2*GameThread.Z_MAX - z - vz*dt;
        } else if (z+dt*vz < GameThread.Z_MIN) {
            z = 2*GameThread.Z_MIN - z - vz*dt;
        } else {
            z += dt * vz;
        }
        if (z > GameThread.Z_MAX)
            throw new PlayerScoreException(PlayerScoreException.Player.P1);
        if (z < GameThread.Z_MIN)
            throw new PlayerScoreException(PlayerScoreException.Player.P2);
    }

    public String getPosition() {
        return String.format("x: %1f, y: %1f, z: %1f",x, y, z);
    }
}
