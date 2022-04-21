package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.game_elements.GameState;
import com.skylerdache.spacepong.services.GameService;

import java.util.Map;

public class GameRunner implements Runnable {
    private final Map<Long, GameState> games;
    private final GameService gameService;
    private final double tickTimeSeconds;
    public GameRunner(GameService gameService, Map<Long, GameState> games, double tickTimeSeconds) {
        this.games = games;
        this.gameService = gameService;
        this.tickTimeSeconds = tickTimeSeconds;
    }
    @Override
    public void run() {
        while (true) {
            games.forEach((Long id, GameState game) -> {
                try {
                    game.tick(tickTimeSeconds);
                } catch (GameOverException e) {
                    gameService.notifyGameOver(game.getGameEntity(), e.winner);
                }
            });
            try {
                //TODO: replace this with an accurate wait calculation
                wait((int) (tickTimeSeconds / 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
