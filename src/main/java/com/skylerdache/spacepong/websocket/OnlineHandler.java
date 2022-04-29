package com.skylerdache.spacepong.websocket;

import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.services.OnlineService;
import com.skylerdache.spacepong.services.PlayerService;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;

public class OnlineHandler extends TextWebSocketHandler {
    private final OnlineService onlineService;
    private final PlayerService playerService;
    public OnlineHandler(OnlineService onlineService, PlayerService playerService) {
        this.onlineService = onlineService;
        this.playerService = playerService;
    }
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal == null) { unAuthenticated(session); return; }
        String userName = principal.getName();
        try {
            HumanPlayer p = (HumanPlayer)playerService.getPlayerByName(userName);
            onlineService.addNewPlayer(p, session);
        }
        catch (UsernameNotFoundException | ClassCastException e) { unAuthenticated(session);}
    }
    private void unAuthenticated(WebSocketSession session) {
        /* this shouldn't ever happen */
        try { session.sendMessage(new TextMessage("UNAUTHENTICATED")); }
        catch (IOException e) { e.printStackTrace(); }
    }
    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) {
        Principal principal = session.getPrincipal();
        if (principal == null) { throw new RuntimeException("session.principal() was null :(");}
        String userName = principal.getName();
        HumanPlayer p = (HumanPlayer) playerService.getPlayerByName(userName);
        System.out.println("message.getPayload(): "+message.getPayload());
        if (message.getPayload().equals("CONNECT")) {
            onlineService.addNewPlayer(p, session);
        } else if (message.getPayload().equals("DISCONNECT")) {
            onlineService.removeSession(p, session);
        } else {
            System.out.println("no match");
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
        Principal principal = session.getPrincipal();
        if (principal == null) { throw new RuntimeException("session.principal() was null :(");}
        String userName = principal.getName();
        HumanPlayer p = (HumanPlayer) playerService.getPlayerByName(userName);
        System.out.println("removing session...");
        onlineService.removeSession(p, session);
    }
    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
        System.out.println("handleTransportError() received the following exception:");
        exception.printStackTrace();
    }
    @Override
    public boolean supportsPartialMessages() {return false;}
}
