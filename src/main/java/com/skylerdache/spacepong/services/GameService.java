package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.entities.Game;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.exceptions.NoSuchGameException;
import com.skylerdache.spacepong.repositories.GameRepository;
import com.skylerdache.spacepong.runnables.GameThread;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final HashMap<Long, GameThread> games;
    private final SimpMessagingTemplate template;

    public GameService(GameRepository gameRepository, SimpMessagingTemplate template) {
        this.gameRepository = gameRepository;
        games = new HashMap<Long, GameThread>();
        this.template = template;
    }
    public Game startNewGame(Player p1, Player p2) {
        Game newGame = new Game();
        newGame.setPlayer1(p1);
        newGame.setPlayer2(p2);
        Game savedGame = gameRepository.save(newGame);
        long id = savedGame.getId();
        GameThread newGameThread = new GameThread(id,10, this, template);
        newGameThread.start();
        games.put(id, newGameThread);
        return savedGame;
    }
    public GameStateDto getGameState(long gameId) {
        return games.get(gameId).gameState();
    }
    public void saveScore(long gameId) {
        Optional<Game> o = gameRepository.findById(gameId);
        if (o.isEmpty()) {
            throw new Error("Game does not exist");
        }
        Game g = o.get();
        GameThread gt = games.get(gameId);
        g.setP1Score(gt.getP1Score());
        g.setP2Score(gt.getP2Score());
        g.setEndTime(Instant.now());
        gameRepository.save(g);
    }

    public List<Game> getAll() {
        return StreamSupport.stream(gameRepository.findAll().spliterator(), false).toList();
    }

    public Game getOngoingGameByPlayer(Player p) throws NoSuchGameException {
        Optional<Game> g = Stream.concat(
            gameRepository.findGamesByPlayer1(p).stream(),
            gameRepository.findGamesByPlayer2(p).stream()
        )
        .filter((Game gg)->gg.getEndTime()==null)
        .max(Comparator.comparing(Game::getStartTime));
        if (g.isPresent()) return g.get();
        else throw new NoSuchGameException();
    }
    public List<Game> getGamesByPlayer(Player p) {
        return Stream.concat(
            gameRepository.findGamesByPlayer1(p).stream(),
            gameRepository.findGamesByPlayer2(p).stream()
        ).toList();
    }
}
