package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.game_elements.GameState;
import com.skylerdache.spacepong.services.GameService;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

public class GameRunner {
    private final ConcurrentMap<Long, GameState> games;
    private final GameService gameService;
    private final ConcurrentMap<Long, BlockingQueue<PlayerControlMessage>> controlMessages;
    private static final int TICK_DELAY_MILLIS = 50;
    public GameRunner(GameService gameService) {
        this.games = new ConcurrentHashMap<>();
        this.gameService = gameService;
        controlMessages = new ConcurrentHashMap<>();
    }
    @Scheduled(fixedRate=TICK_DELAY_MILLIS, timeUnit=TimeUnit.MILLISECONDS)
    public void tickAllGames() {
        games.forEach(this::tickOneGame);
    }
    private void tickOneGame(long id, GameState game) {
        if (game.isPaused()) return;
        Instant currentTickStart = Instant.now();
        Instant previousTickStart = currentTickStart.minus(TICK_DELAY_MILLIS, ChronoUnit.MILLIS);
        BlockingQueue<PlayerControlMessage> messages = controlMessages.get(id);
        PlayerControlMessage prevMessage = null;
        PlayerControlMessage currentMessage;
        // simulate the game before and between each control message:
        while (!messages.isEmpty()) {
            try {
                currentMessage = messages.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Duration dt;
            if (prevMessage == null) {
                dt = Duration.between(previousTickStart, currentMessage.time());
            } else {
                dt = Duration.between(prevMessage.time(), currentMessage.time());
            }
            double timeMillis = dt.get(ChronoUnit.MILLIS);
            tickGame(game, timeMillis);
            game.update(currentMessage);
            prevMessage = currentMessage;
        }
        // simulate game after all control messages have arrived:
        if (prevMessage == null) {
            tickGame(game, TICK_DELAY_MILLIS);
        } else {
            Duration dt = Duration.between(prevMessage.time(), currentTickStart);
            double timeMillis = dt.get(ChronoUnit.MILLIS);
            tickGame(game,timeMillis);
        }
    }
    public void updatePlayerControl(PlayerControlMessage m) {
        synchronized(controlMessages) {
            if (controlMessages.containsKey(m.gameId())) {
                controlMessages.get(m.gameId()).add(m);
            } else {
                BlockingQueue<PlayerControlMessage> q = new LinkedBlockingQueue<>();
                q.add(m);
                controlMessages.put(m.gameId(),q);
            }
        }
    }
    public GameState newGame(GameEntity e, GameOptions options) {
        GameState gs = new GameState(options, e);
        games.put(e.getId(),gs);
        return gs;
    }
    private void tickGame(GameState game, double dt) {
        try {
            game.tick(dt);
        } catch (GameOverException e) {
            GameEntity ge = game.getGameEntity();
            ge.setP1Score(e.p1Score);
            ge.setP2Score(e.p2Score);
            switch (e.winner) {
                case P1: {ge.setWinner(ge.getPlayer1());}
                case P2: {ge.setWinner(ge.getPlayer2());}
            }
            gameService.notifyGameOver(ge);
        }
    }
    public void pauseGame(long id) {
        games.get(id).pause();
    }
    public void unpauseGame(long id) {
        games.get(id).unpause();
    }
}
