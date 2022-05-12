package com.skylerdache.spacepong.threads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skylerdache.spacepong.game_elements.GameState;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
public class GameStateSender {
    private final Map<Long, WebSocketSession> p1WebSockets;
    private final Map<Long, WebSocketSession> p2WebSockets;
    private final Map<Long, WebSocketSession> singlePlayerWebSockets;
    private final ConcurrentMap<Long, GameState> singlePlayerGameStates;
    private final ConcurrentMap<Long, GameState> twoPlayerGameStates;
    public GameStateSender() {
        twoPlayerGameStates = new ConcurrentHashMap<>();
        singlePlayerGameStates = new ConcurrentHashMap<>();
        p1WebSockets = new HashMap<>();
        p2WebSockets = new HashMap<>();
        singlePlayerWebSockets = new HashMap<>();
    }
    public synchronized void addSinglePlayerGame(long id, GameState gs) {
        singlePlayerGameStates.put(id, gs);
    }
    public synchronized void addTwoPlayerGame(long id, GameState gs) {
        twoPlayerGameStates.put(id, gs);
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

    @Scheduled(fixedRate = 50, timeUnit= TimeUnit.MILLISECONDS)
    public void sendGameState() {
        singlePlayerGameStates.forEach((id, gs) -> {
            sendMessages(id,createGameStateMessage(gs), singlePlayerWebSockets);
        });
        twoPlayerGameStates.forEach((id, gs) ->{
            TextMessage msg = createGameStateMessage(gs);
            sendMessages(id, msg, p1WebSockets);
            sendMessages(id, msg, p2WebSockets);
        });
    }
    @Contract("_ -> new")
    private @NotNull TextMessage createGameStateMessage(@NotNull GameState gs) {
        ObjectMapper m = new ObjectMapper();
        WebSocketMessage<String> msg;
        try {
            return new TextMessage(m.writeValueAsString(gs.getDto()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("shouldn't ever happen");
        }
    }
    private void sendMessages(long id, TextMessage msg, @NotNull Map<Long, WebSocketSession> sockets) {
        try {
            sockets.get(id).sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
