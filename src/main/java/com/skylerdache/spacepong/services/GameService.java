package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.ComputerPlayer;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.game_elements.GameState;
import com.skylerdache.spacepong.repositories.GameRepository;
import com.skylerdache.spacepong.threads.GameRunner;
import com.skylerdache.spacepong.threads.GameStateSender;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final GameRunner gameRunner;
    private final GameStateSender gameStateSender;
    private final Map<Long,Long> gameIdByUserId;

    public GameService(GameRepository gameRepository, GameStateSender gameStateSender) {
        this.gameRepository = gameRepository;
        this.gameRunner = new GameRunner(this);
        this.gameStateSender = gameStateSender;
        this.gameIdByUserId = new HashMap<>();
    }
    public void startGame(@NotNull Player p1, @NotNull Player p2, GameOptions options) {
        GameEntity newGame = new GameEntity();
        newGame.setPlayer1(p1);
        newGame.setPlayer2(p2);
        newGame.setOptions(options);
        GameEntity savedGame = gameRepository.save(newGame);
        gameIdByUserId.put(p1.getId(), savedGame.getId());
        gameIdByUserId.put(p2.getId(), savedGame.getId());
        GameState gs = gameRunner.newGame(savedGame, options);
        if (p2 instanceof HumanPlayer) {
            gameStateSender.addTwoPlayerGame(savedGame.getId(), gs);
        }
        if (p2 instanceof ComputerPlayer) {
            gameStateSender.addSinglePlayerGame(savedGame.getId(), gs);
            // initialize computer player and start game here
        }
    }

    public List<GameEntity> getAll() {
        return StreamSupport.stream(gameRepository.findAll().spliterator(), false).toList();
    }
    public void userConnected(HumanPlayer p, WebSocketSession s) {
        try {
            long gameId = gameIdByUserId.get(p.getId());
            gameStateSender.playerConnect(gameId, s);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    public GameEntity getOngoingGameByPlayer(Player p) throws NoSuchElementException {
        Long gameId = gameIdByUserId.get(p.getId());
        if (gameId == null) {
            throw new NoSuchElementException("no game reference for this user");
        }
        return gameRepository.findById(gameId).orElseThrow();
    }

    public void notifyGameOver(GameEntity g) {
        gameRepository.save(g);
        gameIdByUserId.remove(g.getPlayer1().getId());
        gameIdByUserId.remove(g.getPlayer2().getId());
    }

    public void sendControlMessage(HumanPlayer p, PlayerControlMessage msg) {
        gameRunner.updatePlayerControl(msg);
    }

    public void userDisconnected(HumanPlayer p) {

    }

    public void pause(HumanPlayer p) {
        gameRunner.pauseGame(gameIdByUserId.get(p.getId()));
    }
    public void unpause(HumanPlayer p) {
        gameRunner.unpauseGame(gameIdByUserId.get(p.getId()));
    }
}
