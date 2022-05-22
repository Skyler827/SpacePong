package com.skylerdache.spacepong.threads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.enums.PlayerPosition;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GameStateSender implements Runnable {
    private final Map<Long, WebSocketSession> p1WebSockets;
    private final Map<Long, WebSocketSession> p2WebSockets;
    private final Map<Long, WebSocketSession> singlePlayerWebSockets;
    private final ConcurrentMap<Long, GameStateDto> singlePlayerGameStates;
    private final ConcurrentMap<Long, GameStateDto> twoPlayerGameStates;
    public GameStateSender() {
        twoPlayerGameStates = new ConcurrentHashMap<>();
        singlePlayerGameStates = new ConcurrentHashMap<>();
        p1WebSockets = new HashMap<>();
        p2WebSockets = new HashMap<>();
        singlePlayerWebSockets = new HashMap<>();
    }
    public void addSinglePlayerGame(long id) {
        singlePlayerGameStates.put(id, null);
    }
    public void addTwoPlayerGame(long id) {
        twoPlayerGameStates.put(id, new GameStateDto());
    }

    /**
     * registers a websocket connection from a user
     * @param gameId the id of the game
     * @param s the websocket session of the connecting user
     * @return true if all players are connected, false otherwise
     */
    public boolean playerConnect(long gameId, WebSocketSession s) {
        if (twoPlayerGameStates.containsKey(gameId)) {
            if (p1WebSockets.containsKey(gameId)) {
                p2WebSockets.put(gameId, s);
                return false;
            } else {
                p1WebSockets.put(gameId, s);
                return true;
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
            return true;
        }
    }
    @Override
    public void run() {
        sendAllGameStates();
    }
    private void sendAllGameStates() {
        String message = "now running gameStateSender.sendAllGameStates()... (sp size: "+
                singlePlayerGameStates.size()+", mp size:"+twoPlayerGameStates.size()+")";
        // System.out.println(message);
        synchronized(singlePlayerGameStates) {
            singlePlayerGameStates.forEach((id, gs) ->
                    sendMessages(id, createGameStateMessage(gs), singlePlayerWebSockets, PlayerPosition.P1)
            );
        }
        synchronized(twoPlayerGameStates) {
            twoPlayerGameStates.forEach((id, gs) -> {
                TextMessage msg = createGameStateMessage(gs);
                sendMessages(id, msg, p1WebSockets, PlayerPosition.P1);
                sendMessages(id, msg, p2WebSockets, PlayerPosition.P2);
            });
        }
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
        // System.out.println("sending to "+p.toString()+": "+msg.getPayload());
        try {
            sockets.get(id).sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("unable to send the following data to "+ p);
            System.out.println(msg.getPayload());
        }
    }

    public void updateGameDto(@NotNull GameEntity gameEntity, GameStateDto dto) {
        if (gameEntity.getPlayer2() instanceof HumanPlayer) {
            twoPlayerGameStates.put(gameEntity.getId(), dto);
        } else {
            singlePlayerGameStates.put(gameEntity.getId(),dto);
        }
    }

}
