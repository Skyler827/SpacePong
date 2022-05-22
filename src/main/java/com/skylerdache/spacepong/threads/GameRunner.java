package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.game_elements.GameState;
import com.skylerdache.spacepong.services.GameService;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class GameRunner implements Runnable {
    private final ConcurrentMap<Long, GameState> games;
    private final GameService gameService;
    private final GameStateSender gameStateSender;
    private final Map<Long, Queue<PlayerControlMessage>> controlMessages;
    public GameRunner(GameService gameService, GameStateSender gameStateSender) {
        this.games = new ConcurrentHashMap<>();
        this.gameService = gameService;
        this.gameStateSender = gameStateSender;
        controlMessages = new ConcurrentHashMap<>();
    }
    @Override
    public void run() {
        tickAllGames();
    }
    public void tickAllGames() {
        games.forEach(this::handleMessagesAndTickGame);
    }
    private void handleMessagesAndTickGame(long id, @NotNull GameState game) {
        GameEntity ge = game.getGameEntity();
        if (game.isPaused()) {
            gameStateSender.updateGameDto(ge, game.getDto());
            return;
        }
        Instant currentTickStart = Instant.now();
        Instant previousTickStart = currentTickStart.minus(GameService.GAME_RUNNER_PERIOD_MILLIS, ChronoUnit.MILLIS);
        Queue<PlayerControlMessage> messages = controlMessages.get(id);
        PlayerControlMessage prevMessage = null;
        // simulate the game before and between each control message:
        while (!messages.isEmpty()) {
            PlayerControlMessage currentMessage;
            currentMessage = messages.remove();
            Duration dt;
            if (prevMessage == null) {
                dt = Duration.between(previousTickStart, currentMessage.time());
            } else {
                dt = Duration.between(prevMessage.time(), currentMessage.time());
            }
            double timeMillis = dt.toMillis();
            tickGame(game, timeMillis);
            game.update(currentMessage);
            prevMessage = currentMessage;
        }
        // simulate game after all control messages have arrived:
        if (prevMessage == null) {
            tickGame(game, GameService.GAME_RUNNER_PERIOD_MILLIS);
        } else {
            Duration dt = Duration.between(prevMessage.time(), currentTickStart);
            double timeMillis = dt.toMillis();
            tickGame(game,timeMillis);
        }
        gameStateSender.updateGameDto(ge, game.getDto());
    }
    public void updatePlayerControl(@NotNull GameEntity e, @NotNull PlayerControlMessage m) {
        // System.out.println("got to updatePlayerControl");
        // System.out.println("controlMessages was previously: "+controlMessages);
        long gameId = e.getId();
        controlMessages.get(gameId).add(m);
    }
    public void newGame(GameEntity e, GameOptions options) {
        GameState gs = new GameState(options, e);
        games.put(e.getId(), gs);
        controlMessages.put(e.getId(), new LinkedBlockingQueue<>());
    }

    /**
     * simulates a game for a given number of milliseconds
     * @param game the game state
     * @param dt number of milliseconds
     */
    private void tickGame(@NotNull GameState game, double dt) {
        try {
            game.tick(dt/100);
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
