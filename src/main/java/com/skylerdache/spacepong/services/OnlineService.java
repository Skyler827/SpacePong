package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.GameOptionsDto;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.threads.UserListSender;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class OnlineService {
    /**
     * proposals contains a map from a subject player to a map of players who have
     * proposed a game to the subject, to the proposed game options.
     */
    private final Map<HumanPlayer, Map<HumanPlayer, GameOptions>> proposals;
    private final Map<HumanPlayer, WebSocketSession> waitingPlayerSessions;
    private final UserListSender userListSender;

    @Getter @Setter
    private String onlinePlayersJSON;
    public OnlineService() {
        proposals = new HashMap<>();
        waitingPlayerSessions = new HashMap<>();
        userListSender = new UserListSender();
        userListSender.start();
    }
    public void addNewPlayer(HumanPlayer p, WebSocketSession session) {
        userListSender.getNewSessions().put(p.getUsername(),session);
    }
    public void removeSession(HumanPlayer p, WebSocketSession s) {
        userListSender.getDisconnectingPlayers().add(Pair.of(p.getUsername(), s));
    }
    public void proposeNewGame(HumanPlayer proposer, HumanPlayer subject, GameOptionsDto options) {
        GameOptions newGameOptions = new GameOptions(proposer, subject, options);
        if (proposals.containsKey(subject)) {
            proposals.get(subject)
            .put(proposer, newGameOptions);
        } else {
            Map<HumanPlayer, GameOptions> newSubjectsProposals = new HashMap<>();
            newSubjectsProposals.put(proposer, newGameOptions);
            proposals.put(subject, newSubjectsProposals);
        }
        userListSender.notifyGameRequested(newGameOptions);
        System.out.println("new game just proposed from "+proposer.getUsername()+" to "+subject.getUsername());
        System.out.println(options);
    }
    public void registerWaitingConnection(HumanPlayer p, WebSocketSession session) {
        waitingPlayerSessions.put(p,session);
    }
    public void notifyGameAccepted(HumanPlayer proposer, HumanPlayer acceptor) {
        try {
            waitingPlayerSessions.get(proposer).sendMessage(new TextMessage("ok"));
        } catch (IOException e) {
            // gameService.terminateGame(acceptor);
        }
    }
    public GameOptions getProposedGameOptions(HumanPlayer proposer, HumanPlayer subject) {
        try {
            return proposals.get(subject).get(proposer);
        } catch (NullPointerException e) {
            return null;
        }
    }
    public List<String> getPlayers() {
        try {
            return userListSender.getOnlineUsers().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    public void handleLogout(HumanPlayer p) {
        userListSender.getLoggingOutPlayers().add(p.getUsername());
    }

    public void rejectGame(HumanPlayer rejectingPlayer, HumanPlayer requestingPlayer) {
        proposals.get(rejectingPlayer).remove(requestingPlayer);
    }
}
