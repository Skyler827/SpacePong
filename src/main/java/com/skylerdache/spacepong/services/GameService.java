package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.exceptions.NoSuchGameException;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.repositories.GameRepository;
import com.skylerdache.spacepong.threads.GameRunner;
import com.skylerdache.spacepong.threads.GameStateSender;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final GameRunner gameRunner;
    private final GameStateSender gameStateSender;

    public GameService(GameRepository gameRepository, GameStateSender gameStateSender) {
        this.gameRepository = gameRepository;
        this.gameRunner = new GameRunner(this);
        this.gameStateSender = gameStateSender;
    }
    public void startGame(@NotNull Player p1, @NotNull Player p2, GameOptions options) {
        GameEntity newGame = new GameEntity();
        newGame.setPlayer1(p1);
        newGame.setPlayer2(p2);
        newGame.setOptions(options);
        GameEntity savedGame = gameRepository.save(newGame);
        gameRunner.newGame(savedGame, options);
    }
    public Future<GameStateDto> getGameState(long gameId) {
        return null;
    }

    public List<GameEntity> getAll() {
        return StreamSupport.stream(gameRepository.findAll().spliterator(), false).toList();
    }
    public void userConnected(HumanPlayer p, WebSocketSession s) {
        try {
            GameEntity e = getOngoingGameByPlayer(p);

        } catch (NoSuchGameException e) {}
    }

    public GameEntity getOngoingGameByPlayer(Player p) throws NoSuchGameException {
        Optional<GameEntity> g = Stream.concat(
            gameRepository.findGamesByPlayer1(p).stream(),
            gameRepository.findGamesByPlayer2(p).stream()
        )
        .filter((GameEntity gg)->gg.getEndTime()==null)
        .max(Comparator.comparing(GameEntity::getStartTime));
        if (g.isPresent()) return g.get();
        else throw new NoSuchGameException();
    }
    public List<GameEntity> getGamesByPlayer(Player p) {
        return Stream.concat(
            gameRepository.findGamesByPlayer1(p).stream(),
            gameRepository.findGamesByPlayer2(p).stream()
        ).toList();
    }


    public void notifyGameOver(GameEntity g, PlayerPosition winner) {
        gameRepository.save(g);
    }

    public void sendControlMessage(HumanPlayer p, PlayerControlMessage msg) {
        gameRunner.updatePlayerControl(msg);
    }
}
