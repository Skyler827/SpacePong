package com.skylerdache.spacepong.configuration;

import com.skylerdache.spacepong.services.GameService;
import com.skylerdache.spacepong.services.OnlineService;
import com.skylerdache.spacepong.services.PlayerService;
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
    private final GameService gameService;
    private final PlayerService playerService;
    private final OnlineService onlineService;

    public WebSocketConfig(GameService gameService, PlayerService playerService, OnlineService onlineService) {
        this.gameService = gameService;
        this.playerService = playerService;
        this.onlineService = onlineService;
    }

    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry.addHandler(new GameHandler(gameService, playerService), "/game_connect");
        registry.addHandler(new OnlineHandler(onlineService, playerService), "/register_presence");
        registry.addHandler(new ProposedGameWaitingHandler(onlineService), "/register_waiting");
    }
}
