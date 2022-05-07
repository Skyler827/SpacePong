package com.skylerdache.spacepong.websocket;

import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.services.OnlineService;
import com.skylerdache.spacepong.services.PlayerService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Objects;

@Service
public class ProposedGameWaitingHandler extends TextWebSocketHandler {
    private final OnlineService onlineService;
    public ProposedGameWaitingHandler(OnlineService onlineService) {
        this.onlineService = onlineService;
    }
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        @NotNull String username = Objects.requireNonNull(session.getPrincipal()).getName();
        onlineService.registerWaitingConnection(username, session);
        try {
            session.sendMessage(new TextMessage("Hello??? Are you getting this????"));
        } catch (IOException e) {e.printStackTrace();}
    }

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) {


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