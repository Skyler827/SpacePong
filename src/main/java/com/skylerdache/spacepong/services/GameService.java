package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.ComputerPlayer;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.enums.GameOverReason;
import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.exceptions.GameOverException;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.game_elements.SpaceBounds;
import com.skylerdache.spacepong.repositories.GameRepository;
import com.skylerdache.spacepong.threads.GameRunner;
import com.skylerdache.spacepong.threads.GameStateSender;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Service
public class GameService {
    public static final long GAME_RUNNER_PERIOD_MILLIS = 40;
    public static final long GAME_STATE_SENDER_PERIOD_MILLIS = 80;
    private final GameRepository gameRepository;
    private final GameRunner gameRunner;
    private final GameStateSender gameStateSender;
    private final Map<Long,Long> gameIdByUserId;
    ScheduledExecutorService executor;

    public GameService(GameRepository gameRepository) {
        executor = Executors.newScheduledThreadPool(2);
        this.gameRepository = gameRepository;
        gameStateSender = new GameStateSender();
        gameRunner = new GameRunner(this, gameStateSender);
        gameIdByUserId = new HashMap<>();
        executor.scheduleAtFixedRate(gameRunner,0, GAME_RUNNER_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
        executor.scheduleAtFixedRate(gameStateSender,100, GAME_STATE_SENDER_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    }
    public void startGame(@NotNull Player p1, @NotNull Player p2, GameOptions options) {
        GameEntity newGame = new GameEntity();
        newGame.setPlayer1(p1);
        newGame.setPlayer2(p2);
        newGame.setOptions(options);
        GameEntity savedGame = gameRepository.save(newGame);
        gameIdByUserId.put(p1.getId(), savedGame.getId());
        gameIdByUserId.put(p2.getId(), savedGame.getId());
        gameRunner.newGame(savedGame, options);
        if (p2 instanceof HumanPlayer) {
            gameStateSender.addTwoPlayerGame(savedGame.getId());
        }
        if (p2 instanceof ComputerPlayer) {
            gameStateSender.addSinglePlayerGame(savedGame.getId());
            // initialize computer player and start game here
        }
    }

    public List<GameEntity> getAll() {
        return StreamSupport.stream(gameRepository.findAll().spliterator(), false).toList();
    }
    public PlayerPosition userConnected(@NotNull HumanPlayer p, WebSocketSession s) throws NoSuchElementException{
        long gameId = gameIdByUserId.get(p.getId());
        boolean allUsersConnected = gameStateSender.playerConnect(gameId, s);
        if (allUsersConnected) {
            gameRunner.unpauseGame(gameId);
            return PlayerPosition.P2;
        } else {
            return PlayerPosition.P1;
        }
    }

    public GameEntity getOngoingGameByPlayer(@NotNull Player p) throws NoSuchElementException {
        long playerId = p.getId();
        Long gameId;
        if (gameIdByUserId.containsKey(playerId)) {
            gameId = gameIdByUserId.get(playerId);
        } else {
            gameId = gameRunner.getIdByUserId(playerId);
        }
        return gameRepository.findById(gameId).orElseThrow();
    }

    public void notifyGameOver(GameEntity g, GameOverException e) {
        gameRepository.save(g);
        gameIdByUserId.remove(g.getPlayer1().getId());
        gameIdByUserId.remove(g.getPlayer2().getId());
    }

    public void sendControlMessage(HumanPlayer p, PlayerControlMessage msg) {
        gameRunner.updatePlayerControl(getOngoingGameByPlayer(p), msg);
    }

    public void userDisconnected(HumanPlayer p) {

    }

    public void pause(@NotNull HumanPlayer p) {
        gameRunner.pauseGame(gameIdByUserId.get(p.getId()));
    }
    public void unpause(@NotNull HumanPlayer p) {
        gameRunner.unpauseGame(gameIdByUserId.get(p.getId()));
    }

    public void cancelGameByPlayer(HumanPlayer p, GameOverReason p1disconnect) {
        //TODO: implement this
    }

    public SpaceBounds getBounds(GameEntity ge) {
        return gameRunner.getBounds(ge.getId());
    }
}
