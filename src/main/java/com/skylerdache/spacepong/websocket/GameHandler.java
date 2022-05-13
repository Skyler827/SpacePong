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

import java.nio.charset.StandardCharsets;

@Service
public class GameHandler extends TextWebSocketHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
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
        System.out.println(new String(message.asBytes(), StandardCharsets.US_ASCII));
        if (session.getPrincipal()==null) { throw new RuntimeException("this should never happen.");}
        if (!session.getPrincipal().getClass().equals(UsernamePasswordAuthenticationToken.class)) {
            throw new RuntimeException("this shouldn't happen");
        }
        UsernamePasswordAuthenticationToken principal = (UsernamePasswordAuthenticationToken) session.getPrincipal();
        HumanPlayer p = (HumanPlayer)principal.getPrincipal();
        System.out.println("user connected: " + p.getUsername());
        message.getPayload();
        JSONObject o = new JSONObject();
        String type;
        try {
            o.getJSONObject(message.getPayload());
            type = (String) o.get("type");
        } catch (JSONException e) {throw new RuntimeException("shouldn't happen");}
        switch (type) {
            case "playerControlMessage": {
                String messageData;
                try { messageData = (String) o.get("data"); }
                catch (JSONException e) { throw new RuntimeException("shouldn't ever happen"); }
                PlayerControlMessage msg = JSONDeserializer.deserializeObject(PlayerControlMessage.class, messageData);
                gameService.sendControlMessage(p, msg);
            }
            case "disconnect": {
                gameService.userDisconnected(p);
            }
            case "pause": {
                gameService.pause(p);
            }
            case "unpause": {
                gameService.unpause(p);
            }
            default: {
                System.err.println("invalid message");
            }
        }
    }
    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {

    }
    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {

    }
    @Override
    public boolean supportsPartialMessages() {return false;}
}
