package com.skylerdache.spacepong.threads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.enums.PlayerPosition;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
public class GameStateSender implements Runnable {
    private final Map<Long, WebSocketSession> p1WebSockets;
    private final Map<Long, WebSocketSession> p2WebSockets;
    private final Map<Long, WebSocketSession> singlePlayerWebSockets;
    private final ConcurrentMap<Long, GameStateDto> singlePlayerGameStates;
    private final ConcurrentMap<Long, GameStateDto> twoPlayerGameStates;
    private static final int TICK_DELAY_MILLIS_INITIAL = 4000;
    private int TICK_DELAY_MILLIS = TICK_DELAY_MILLIS_INITIAL;
    public GameStateSender() {
        twoPlayerGameStates = new ConcurrentHashMap<>();
        singlePlayerGameStates = new ConcurrentHashMap<>();
        p1WebSockets = new HashMap<>();
        p2WebSockets = new HashMap<>();
        singlePlayerWebSockets = new HashMap<>();
    }
    public synchronized void addSinglePlayerGame(long id) {
        singlePlayerGameStates.put(id, null);
    }
    public synchronized void addTwoPlayerGame(long id) {
        twoPlayerGameStates.put(id, new GameStateDto());
    }
    public synchronized void playerConnect(long gameId, WebSocketSession s) {
        if (twoPlayerGameStates.containsKey(gameId)) {
            if (p1WebSockets.containsKey(gameId)) {
                p2WebSockets.put(gameId, s);
            } else {
                p1WebSockets.put(gameId, s);
            }
        } else {
            if (singlePlayerGameStates.containsKey(gameId)) {
                singlePlayerWebSockets.put(gameId, s);
            } else {
                try {
                    String message = "not in a valid game";
                    var socketMessage = new TextMessage(message);
                    s.sendMessage(socketMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void run() {
        sendAllGameStates();
    }
    private void sendAllGameStates() {
        System.out.println("now running gameStateSender.sendGameState()...");
        singlePlayerGameStates.forEach((id, gs) ->
            sendMessages(id, createGameStateMessage(gs), singlePlayerWebSockets, PlayerPosition.P1)
        );
        twoPlayerGameStates.forEach((id, gs) -> {
            TextMessage msg = createGameStateMessage(gs);
            sendMessages(id, msg, p1WebSockets, PlayerPosition.P1);
            sendMessages(id, msg, p2WebSockets, PlayerPosition.P2);
        });
    }
    @Contract("_ -> new")
    private @NotNull TextMessage createGameStateMessage(@NotNull GameStateDto gs) {
        ObjectMapper m = new ObjectMapper();
        try {
            return new TextMessage(m.writeValueAsString(gs));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("shouldn't ever happen");
        }
    }
    private void sendMessages(long id, TextMessage msg, @NotNull Map<Long, WebSocketSession> sockets, PlayerPosition p) {
        System.out.println("sending to "+p.toString()+": "+msg.getPayload());
        try {
            sockets.get(id).sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("unable to send the following data to "+ p);
            System.out.println(msg.getPayload());
        }
    }

    public void updateGameDto(GameEntity gameEntity, GameStateDto dto) {
        if (gameEntity.getPlayer2() instanceof HumanPlayer) {
            singlePlayerGameStates.put(gameEntity.getId(),dto);
        } else {
            twoPlayerGameStates.put(gameEntity.getId(), dto);
        }
    }
}
