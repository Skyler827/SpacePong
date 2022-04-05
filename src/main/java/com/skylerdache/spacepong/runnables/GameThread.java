package com.skylerdache.spacepong.runnables;

import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.exceptions.PlayerScoreException;
import com.skylerdache.spacepong.game_elements.Ball;
import com.skylerdache.spacepong.game_elements.Paddle;
import com.skylerdache.spacepong.services.GameService;
import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameThread extends Thread {
    public static final int X_MIN = 0;
    public static final int X_MAX = 100;
    public static final int Y_MIN = 0;
    public static final int Y_MAX = 100;
    public static final int Z_MIN = 0;
    public static final int Z_MAX = 100;
    public static final long tickTimeMillis = 100;
    private final SimpMessagingTemplate template;
    private final Ball gameBall;
    private final Paddle p1Paddle;
    private final Paddle p2Paddle;
    ScheduledExecutorService executor;
    @Getter
    private int p1Score = 0;
    @Getter
    private int p2Score = 0;
    private final int scoreThreshHold;
    private final GameService gameService;
    private final long gameId;
    public GameThread(long gameId, int scoreMax, GameService gameService, SimpMessagingTemplate template) {
        this.gameId = gameId;
        this.gameService = gameService;
        gameBall = new Ball(this,1);
        p1Paddle = new Paddle(this);
        p2Paddle = new Paddle(this);
        executor = Executors.newSingleThreadScheduledExecutor();
        scoreThreshHold = scoreMax;
        this.template = template;
    }
    @Override
    public void run() {
        executor.scheduleAtFixedRate(() -> {
            this.tick(((double)tickTimeMillis)/1000);
        }, 0, tickTimeMillis, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(this::sendGameData,0,1,TimeUnit.SECONDS);
    }
    private void tick(double dt) {
        p1Paddle.tick(dt);
        p2Paddle.tick(dt);
        try {
            gameBall.tick(dt);
        } catch (PlayerScoreException e) {
            switch(e.scorer) {
                case P1: {p1Score += 1;}
                case P2: {p2Score += 1;}
            }
            if (p1Score >= scoreThreshHold) { // p1 wins!
                executor.shutdown();
                gameService.saveScore(gameId);

            } else if (p2Score >= scoreThreshHold) { //p2 wins!
                executor.shutdown();
                gameService.saveScore(gameId);
            }
        } finally {
            System.out.println(gameBall.getPosition());
        }
    }
    public GameStateDto gameState() {
        GameStateDto gs = new GameStateDto();
        gs.setP1Score(p1Score);
        gs.setP2Score(p2Score);

        gs.setBallX(gameBall.getX());
        gs.setBallY(gameBall.getY());
        gs.setBallZ(gameBall.getZ());
        gs.setBallVx(gameBall.getVx());
        gs.setBallVy(gameBall.getVy());
        gs.setBallVz(gameBall.getVz());

        gs.setP1PaddleX(p1Paddle.getX());
        gs.setP1PaddleY(p1Paddle.getY());
        gs.setP1PaddleZ(p1Paddle.getZ());
        gs.setP1PaddleVx(p1Paddle.getVx());
        gs.setP1PaddleVy(p1Paddle.getVy());
        gs.setP1PaddleVz(p1Paddle.getVz());

        gs.setP2PaddleX(p2Paddle.getX());
        gs.setP2PaddleY(p2Paddle.getY());
        gs.setP2PaddleZ(p2Paddle.getZ());
        gs.setP2PaddleVx(p2Paddle.getVx());
        gs.setP2PaddleVy(p2Paddle.getVy());
        gs.setP2PaddleVz(p2Paddle.getVz());

        return gs;
    }
    public void sendGameData() {
        template.convertAndSendToUser("user1", "/destination", gameState());
    }

}
