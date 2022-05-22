package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.enums.GameOverReason;
import com.skylerdache.spacepong.enums.LeftRightArrowState;
import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.enums.UpDownArrowState;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.exceptions.PlayerScoreException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class GameState {
    private final SpaceBounds bounds;
    private final Ball ball;
    private final Paddle p1Paddle;
    private final Paddle p2Paddle;
    private int p1Score = 0;
    private int p2Score = 0;
    private final int scoreThreshHold;
    private PlayerControlState p1Control;
    private PlayerControlState p2Control;
    @Getter
    private final GameEntity gameEntity;
    private boolean paused = true;
    public GameState(@NotNull GameOptions options, GameEntity gameEntity) {
        scoreThreshHold = options.getScoreThreshold();
        bounds = options.getBounds();
        ball = new Ball(4, bounds);
        p1Paddle = new Paddle(PlayerPosition.P1, bounds);
        p2Paddle = new Paddle(PlayerPosition.P2, bounds);
        p1Control = new PlayerControlState(LeftRightArrowState.NONE, UpDownArrowState.NONE);
        p2Control = new PlayerControlState(LeftRightArrowState.NONE, UpDownArrowState.NONE);
        this.gameEntity = gameEntity;
    }

    /**
     * simulates a game for a given number of seconds
     * @param dt number of seconds
     * @throws GameOverException if one player or another wins the game
     */
    public void tick(double dt) throws GameOverException{
        if (paused) return;
        p1Paddle.tick(dt, p1Control);
        p2Paddle.tick(dt, p2Control);
        try {
            ball.tick(dt, p1Paddle, p2Paddle);
        } catch (PlayerScoreException e) {
            switch (e.scorer) {
                case P1 -> {
                    p1Score += 1;
                    gameEntity.setP1Score(p1Score);
                    ball.resetPositionAndVelocity();
                }
                case P2 -> {
                    p2Score += 1;
                    gameEntity.setP2Score(p2Score);
                    ball.resetPositionAndVelocity();
                }
            }
            if (p1Score >= scoreThreshHold) {
                throw new GameOverException(PlayerPosition.P1, p1Score, p2Score, GameOverReason.SCORE);
            } else if (p2Score >= scoreThreshHold) {
                throw new GameOverException(PlayerPosition.P2, p1Score, p2Score, GameOverReason.SCORE);
            }
        }
    }
    public GameStateDto getDto() {
        return new GameStateDto(
            paused,
            p1Score,
            p2Score,
            Instant.now().toString(),
            p1Paddle.getX(),
            p1Paddle.getY(),
            p1Paddle.getZ(),
            p1Paddle.getVx(),
            p1Paddle.getVy(),
            p1Paddle.getVz(),
            p2Paddle.getX(),
            p2Paddle.getY(),
            p2Paddle.getZ(),
            p2Paddle.getVx(),
            p2Paddle.getVy(),
            p2Paddle.getVz(),
            ball.getX(),
            ball.getY(),
            ball.getZ(),
            ball.getVx(),
            ball.getVy(),
            ball.getVz()
        );
    }

    public void update(@NotNull PlayerControlMessage m) {
        switch (m.playerPosition()) {
            case P1 -> p1Control = new PlayerControlState(m.lrState(), m.udState());
            case P2 -> p2Control = new PlayerControlState(m.lrState(), m.udState());
        }
    }
    public boolean isPaused() { return paused; }
    public void unpause() { paused = false; }
    public void pause() { paused = true; }
}
