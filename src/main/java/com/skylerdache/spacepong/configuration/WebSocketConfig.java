package com.skylerdache.spacepong.configuration;

import com.skylerdache.spacepong.websocket.GameHandler;
import com.skylerdache.spacepong.websocket.OnlineHandler;
import com.skylerdache.spacepong.websocket.ProposedGameWaitingHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameHandler gameHandler;
    private final OnlineHandler onlineHandler;
    private final ProposedGameWaitingHandler proposedGameWaitingHandler;
    public WebSocketConfig(GameHandler gameHandler, OnlineHandler onlineHandler, ProposedGameWaitingHandler proposedGameWaitingHandler) {
        this.gameHandler = gameHandler;
        this.onlineHandler = onlineHandler;
        this.proposedGameWaitingHandler = proposedGameWaitingHandler;
    }

    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry.addHandler(gameHandler, "/game_connect");
        registry.addHandler(onlineHandler, "/register_presence");
        registry.addHandler(proposedGameWaitingHandler, "/register_waiting");
    }
}
