package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.game_elements.GameState;
import com.skylerdache.spacepong.services.GameService;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

public class GameRunner implements Runnable {
    private final ConcurrentMap<Long, GameState> games;
    private final GameService gameService;
    private final double tickTimeSeconds = 0.1;
    private final ConcurrentMap<Long, BlockingQueue<PlayerControlMessage>> controlMessages;
    public GameRunner(GameService gameService) {
        this.games = new ConcurrentHashMap<>();
        this.gameService = gameService;
        controlMessages = new ConcurrentHashMap<>();
    }
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            games.forEach((Long id, GameState game) -> {
                double dt = 0.01;
                var x = Instant.now();
                var y = Instant.now();
                Duration.between(x,y);
                controlMessages.get(id).forEach(m->{
                    tickGame(game, dt);
                    game.update(m);
                });
                tickGame(game, dt);
            });
            try {
                //TODO: replace this with an accurate wait calculation
                wait((int) (tickTimeSeconds / 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void updatePlayerControl(PlayerControlMessage m) {
        synchronized(controlMessages) {
            if (controlMessages.containsKey(m.getGameId())) {
                controlMessages.get(m.getGameId()).add(m);
            } else {
                BlockingQueue<PlayerControlMessage> q = new LinkedBlockingQueue<>();
                q.add(m);
                controlMessages.put(m.getGameId(),q);
            }
        }
    }
    public void newGame(long id) {
//        games.put(id,)
    }
    private void tickGame(GameState game, double dt) {
        try {
            game.tick(dt);
        } catch (GameOverException e) {
            gameService.notifyGameOver(game.getGameEntity(), e.winner);
        }
    }
}
