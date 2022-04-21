package com.skylerdache.spacepong.websocket;

import com.skylerdache.spacepong.services.OnlineService;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ProposedGameWaitingHandler extends TextWebSocketHandler {
    private final OnlineService onlineService;
    public ProposedGameWaitingHandler(OnlineService onlineService) {
        this.onlineService = onlineService;
    }
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {

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