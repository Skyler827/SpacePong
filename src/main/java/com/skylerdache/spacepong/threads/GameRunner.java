package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.game_elements.GameState;
import com.skylerdache.spacepong.services.GameService;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.*;

public class GameRunner implements Runnable {
    private final ConcurrentMap<Long, GameState> games;
    private final GameService gameService;
    private final GameStateSender gameStateSender;
    private final ConcurrentMap<Long, BlockingQueue<PlayerControlMessage>> controlMessages;
    public GameRunner(GameService gameService, GameStateSender gameStateSender) {
        this.games = new ConcurrentHashMap<>();
        this.gameService = gameService;
        this.gameStateSender = gameStateSender;
        controlMessages = new ConcurrentHashMap<>();
    }
    @Override
    public void run() {
        System.out.println("running GameRunner.run()");
        tickAllGames();
    }
    public void tickAllGames() {
        // System.out.println("now running gameRunner.tickAllGames()...");
//        if (games.size() > 0) {
//            System.out.println("paused: "+games.get(games.keySet().stream().findFirst().orElseThrow()).isPaused());
//        }
        games.forEach(this::handleMessagesAndTickGame);
    }
    private void handleMessagesAndTickGame(long id, @NotNull GameState game) {
        System.out.println("running GameRunner.handleMessagesAndTickGame()...");
        GameEntity ge = game.getGameEntity();
        if (game.isPaused()) {
            gameStateSender.updateGameDto(ge, game.getDto());
            return;
        }
        Instant currentTickStart = Instant.now();
        Instant previousTickStart = currentTickStart.minus(GameService.GAME_RUNNER_DELAY_MILLIS, ChronoUnit.MILLIS);
        // BlockingQueue<PlayerControlMessage> messages = controlMessages.get(id);
        PlayerControlMessage prevMessage = null;
        // simulate the game before and between each control message:
//        System.out.println(Thread.holdsLock(controlMessages));
//        Map<Thread, StackTraceElement[]> stackTracesByThread = Thread.getAllStackTraces();
//        stackTracesByThread.forEach((Thread t, StackTraceElement[] eArr) -> {
//            boolean skip = true;
//            for (StackTraceElement e : eArr) {
//                if (e.toString().contains("skylerdache")) {
//                    skip = false;
//                    break;
//                }
//            }
//            if (!skip) {
//                String filename = "/home/skyler/spacepong_err/"+t.getName();
//                try (PrintWriter writer = new PrintWriter(filename)){
//                    for (StackTraceElement e : eArr) {
//                        writer.write(e.toString()+"\n");
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        System.out.println("about to try to synchronize controlMessages...");
        synchronized(controlMessages) {
            System.out.println("before loop, controlMessages.get(id).size() = "+controlMessages.get(id).size());
            if (controlMessages.containsKey(id)) {
                while (!controlMessages.get(id).isEmpty()) {
                    PlayerControlMessage currentMessage;
                    try {
                        currentMessage = controlMessages.get(id).take();
                        System.out.println("handling message: " + currentMessage.getJson());
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
                System.out.println("controlMessages.get(id).size() = " + controlMessages.get(id).size());
            }
        }
        // simulate game after all control messages have arrived:
        if (prevMessage == null) {
            tickGame(game, GameService.GAME_RUNNER_DELAY_MILLIS);
        } else {
            Duration dt = Duration.between(prevMessage.time(), currentTickStart);
            double timeMillis = dt.get(ChronoUnit.MILLIS);
            tickGame(game,timeMillis);
        }
        System.out.println("before calling gameStateSender.updateGameDto()");
        gameStateSender.updateGameDto(ge, game.getDto());
        System.out.println("after calling gameStateSender.updateGameDto()");
    }
    public void updatePlayerControl(@NotNull GameEntity e, @NotNull PlayerControlMessage m) {
        // System.out.println("got to updatePlayerControl");
        // System.out.println("controlMessages was previously: "+controlMessages);
        long gameId = e.getId();
        if (controlMessages.containsKey(gameId)) {
            controlMessages.get(gameId).add(m);
        } else {
            BlockingQueue<PlayerControlMessage> q = new LinkedBlockingQueue<>();
            q.add(m);
            controlMessages.put(gameId,q);
        }
        System.out.println("now controlMessages is: " + controlMessages);
    }
    public void newGame(GameEntity e, GameOptions options) {
        GameState gs = new GameState(options, e);
        games.put(e.getId(), gs);
    }

    /**
     * simulates a game for a given number of milliseconds
     * @param game the game state
     * @param dt number of milliseconds
     */
    private void tickGame(@NotNull GameState game, double dt) {
        try {
            game.tick(dt/1000);
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
