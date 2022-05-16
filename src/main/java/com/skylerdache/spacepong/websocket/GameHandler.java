package com.skylerdache.spacepong.websocket;

import com.skylerdache.spacepong.dto.PlayerControlMessage;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.services.GameService;
import com.skylerdache.spacepong.services.PlayerService;
import nonapi.io.github.classgraph.json.JSONDeserializer;
import nonapi.io.github.classgraph.json.JSONSerializer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Service
public class GameHandler extends TextWebSocketHandler {
    private final GameService gameService;
    private final PlayerService playerService;

    public GameHandler(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        HumanPlayer p;
        if (session.getPrincipal()==null) { throw new RuntimeException("this should never happen.");}
        if (session.getPrincipal().getClass().equals(UsernamePasswordAuthenticationToken.class)) {
            UsernamePasswordAuthenticationToken principal = (UsernamePasswordAuthenticationToken) session.getPrincipal();
            p = (HumanPlayer)principal.getPrincipal();
            System.out.println("user connected: " + p.getUsername());
            gameService.userConnected(p, session);
        } else {
            System.out.println(session.getPrincipal());
            System.out.println("principal class: "+session.getPrincipal().getClass());
        }
        System.out.println("Principal: "+session.getPrincipal());
        System.out.println(session);
        System.out.println(JSONSerializer.serializeObject(session.getAttributes()));
    }
    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) {
        System.out.println("new message:");
        System.out.println(message.getPayload());
        if (session.getPrincipal()==null) { throw new RuntimeException("this should never happen.");}
        if (!session.getPrincipal().getClass().equals(UsernamePasswordAuthenticationToken.class)) {
            throw new RuntimeException("this shouldn't happen");
        }
        UsernamePasswordAuthenticationToken principal = (UsernamePasswordAuthenticationToken) session.getPrincipal();
        HumanPlayer p = playerService.getHumanPlayerByName(principal.getName());
        System.out.println("user connected: " + p.getUsername());
        JSONObject o;
        String type;
        try {
            o = new JSONObject(message.getPayload());
        } catch (JSONException e) {
            throw new RuntimeException("Invalid JSON from incoming message:" + message.getPayload());
        }
        try {
            type = (String) o.get("type");
        } catch (JSONException e) {
            throw new RuntimeException("JSON object from websocket message must have a \"type\" property");
        }
        switch (type) {
            case "playerControlMessage" -> {
                try {
                    JSONObject messageData = o.getJSONObject("data");
                    PlayerControlMessage msg = new PlayerControlMessage(messageData);
                    gameService.sendControlMessage(p, msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            case "disconnect" -> gameService.userDisconnected(p);
            case "pause" -> gameService.pause(p);
            case "unpause" -> gameService.unpause(p);
            default -> throw new RuntimeException("invalid type: \"" + type + "\"; " +
                        "websocket message type should be one of: " +
                        "playerControlMessage, disconnect, pause, or unpause");
        }
    }
    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
        // TODO: close game
    }
    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
        // TODO: close game
    }
    @Override
    public boolean supportsPartialMessages() {return false;}
}
