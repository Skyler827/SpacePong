package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.GameOptionsDto;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.threads.UserListSender;
import lombok.Getter;
import lombok.Setter;
import nonapi.io.github.classgraph.json.JSONSerializer;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

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
        userListSender = new UserListSender(this);
        userListSender.start();
    }
    public void addNewPlayer(HumanPlayer p, WebSocketSession session) {
        userListSender.getNewSessions().put(p,session);
    }
    public void removeSession(HumanPlayer p) {
        userListSender.getDisconnectingPlayers().add(p);
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
        System.out.println("new game just proposed from "+proposer.getUsername()+" to "+subject.getUsername());
        System.out.println(options);
    }
    public void registerWaitingConnection(HumanPlayer p, WebSocketSession session) {
        waitingPlayerSessions.put(p,session);
    }
    public void removeWaitingConnection(HumanPlayer p) {
        waitingPlayerSessions.remove(p);
    }
    public void notifyGameAccepted(HumanPlayer proposer) {
        userListSender.getWaitingPlayersGameStarting().add(proposer);
    }
    public GameOptions getProposedGameOptions(HumanPlayer proposer, HumanPlayer subject) {
        return proposals.get(subject).get(proposer);
    }
    public List<HumanPlayer> getPlayers() {
        return userListSender.getOnlineUsers();
    }
}
