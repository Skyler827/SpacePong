package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.game_elements.GameState;
import com.skylerdache.spacepong.services.GameService;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

@Service
public class GameRunner {
    private final ConcurrentMap<Long, GameState> games;
    private final GameService gameService;
    private final GameStateSender gameStateSender;
    private final ConcurrentMap<Long, BlockingQueue<PlayerControlMessage>> controlMessages;
    private static final int TICK_DELAY_MILLIS = 1000;
    public GameRunner(GameService gameService, GameStateSender gameStateSender) {
        this.games = new ConcurrentHashMap<>();
        this.gameService = gameService;
        this.gameStateSender = gameStateSender;
        controlMessages = new ConcurrentHashMap<>();
    }
    @Scheduled(fixedRate=TICK_DELAY_MILLIS, timeUnit=TimeUnit.MILLISECONDS)
    public void tickAllGames() {
        System.out.println("now running gameRunner.tickAllGames()...");
        System.out.println("size: "+games.size());
        if (games.size() > 0) {
            System.out.println("paused: "+games.get(games.keySet().stream().findFirst().orElseThrow()).isPaused());
        }
        games.forEach(this::tickOneGame);
    }
    private void tickOneGame(long id, @NotNull GameState game) {
        System.out.println("ticking game: "+ id);
        if (game.isPaused()) return;
        Instant currentTickStart = Instant.now();
        Instant previousTickStart = currentTickStart.minus(TICK_DELAY_MILLIS, ChronoUnit.MILLIS);
        BlockingQueue<PlayerControlMessage> messages = controlMessages.get(id);
        PlayerControlMessage prevMessage = null;
        // simulate the game before and between each control message:
        while (!messages.isEmpty()) {
            PlayerControlMessage currentMessage;
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
        GameEntity ge = game.getGameEntity();
        gameStateSender.updateGameDto(ge, game.getDto());
    }
    public void updatePlayerControl(@NotNull PlayerControlMessage m) {
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
    public void newGame(GameEntity e, GameOptions options) {
        GameState gs = new GameState(options, e);
        games.put(e.getId(), gs);
    }
    private void tickGame(@NotNull GameState game, double dt) {
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
