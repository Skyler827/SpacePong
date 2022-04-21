package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.exceptions.PlayerScoreException;
import lombok.Getter;

public class GameState {
    public static final int X_MIN = 0;
    public static final int X_MAX = 100;
    public static final int Y_MIN = 0;
    public static final int Y_MAX = 100;
    public static final int Z_MIN = 0;
    public static final int Z_MAX = 100;
    public static final long tickTimeMillis = 100;
    private final Ball ball;
    private final Paddle p1Paddle;
    private final Paddle p2Paddle;
    private int p1Score = 0;
    private int p2Score = 0;
    private final int scoreThreshHold;
    private final Player player1;
    private final Player player2;
    @Getter
    private final GameEntity gameEntity;
    public GameState(GameOptions options, Player player1, Player player2, GameEntity gameEntity) {
        scoreThreshHold = options.getScoreThreshold();
        ball = new Ball(1, new SpaceBounds());
        p1Paddle = new Paddle();
        p2Paddle = new Paddle();
        this.player1 = player1;
        this.player2 = player2;
        this.gameEntity = gameEntity;
    }
    public void tick(double dt) throws GameOverException{
        p1Paddle.tick(dt, player1.getControlState());
        p2Paddle.tick(dt, player2.getControlState());
        try {
            ball.tick(dt, p1Paddle, p2Paddle);
        } catch (PlayerScoreException e) {
            switch(e.scorer) {
                case P1: {
                    p1Score += 1;
                    gameEntity.setP1Score(p1Score);
                }
                case P2: {
                    p2Score += 1;
                    gameEntity.setP2Score(p2Score);
                }
            }
            if (p1Score >= scoreThreshHold) {
                gameEntity.setWinner(player1);
                throw new GameOverException(PlayerPosition.P1);
            } else if (p2Score >= scoreThreshHold) {
                gameEntity.setWinner(player2);
                throw new GameOverException(PlayerPosition.P2);
            }
        }

    }
    public GameStateDto getGameState() {
        GameStateDto gs = new GameStateDto();
        gs.setBallX(ball.getX());
        gs.setBallY(ball.getY());
        gs.setBallZ(ball.getZ());
        gs.setBallVx(ball.getVx());
        gs.setBallVy(ball.getVy());
        gs.setBallVz(ball.getVz());

        gs.setP1PaddleX(p1Paddle.getX());
        gs.setP1PaddleY(p1Paddle.getY());
        gs.setP1PaddleZ(p1Paddle.getZ());
        gs.setP1PaddleVx(p1Paddle.getVx());
        gs.setP1PaddleVy(p1Paddle.getVy());
        gs.setP1PaddleVz(p1Paddle.getVz());

        return gs;
    }
}
