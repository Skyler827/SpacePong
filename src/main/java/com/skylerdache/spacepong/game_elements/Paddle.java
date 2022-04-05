package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.exceptions.PlayerScoreException;
import com.skylerdache.spacepong.runnables.GameThread;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Paddle {
    private final GameThread gameThread;
    private final double x_length;
    private final double y_length;
    private final double z_length;
    private double x;
    private double y;
    private double z;
    private double vx;
    private double vy;
    private double vz;
    public Paddle(GameThread gameThread) {
        this.gameThread = gameThread;
        x_length = 10;
        y_length = 10;
        z_length = 10;
        x = 0;
        y = 0;
        z = 0;
        vx = 1;
        vy = 1;
        vz = 1;
    }

    public void tick(double dt) {
        x += dt * vx;
        y += dt * vy;
        z += dt * vz;
        vx *= Math.exp(-dt);
        vy *= Math.exp(-dt);
        vz *= Math.exp(-dt);
    }
    public void singleAccelerate(double ax, double ay, double az) {
        vx += ax;
        vy += ay;
        vz += az;
    }
}
