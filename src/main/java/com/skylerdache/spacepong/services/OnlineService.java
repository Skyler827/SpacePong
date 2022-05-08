package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.GameOptionsDto;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.threads.UserListSender;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
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
    private final Map<String, Map<String, GameOptions>> proposals;
    private final Map<String, WebSocketSession> waitingPlayerSessions;
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
    public void proposeNewGame(String proposerName, String proposalReceiverName, GameOptionsDto options) {
        GameOptions newGameOptions = new GameOptions(proposerName, proposalReceiverName, options);
        if (proposals.containsKey(proposalReceiverName)) {
            proposals.get(proposalReceiverName)
            .put(proposerName, newGameOptions);
        } else {
            Map<String, GameOptions> newSubjectsProposals = new HashMap<>();
            newSubjectsProposals.put(proposerName, newGameOptions);
            proposals.put(proposalReceiverName, newSubjectsProposals);
        }
        userListSender.notifyGameRequested(newGameOptions);
        System.out.println("new game just proposed from "+proposerName+" to "+proposalReceiverName);
        System.out.println(options);
    }
    public void registerWaitingConnection(String username, WebSocketSession session) {
        System.out.println("registerWaitingPlayerConnection() called");
        waitingPlayerSessions.put(username,session);
    }
    public void notifyGameAccepted(String proposerName, String acceptorName) {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("type", "game_start");
            messageJson.put("opponent", acceptorName);
        } catch (JSONException e) {
            // shouldn't ever happen
            throw new RuntimeException(e);
        }
        try {
            var message = new TextMessage(messageJson.toString());
            waitingPlayerSessions.get(proposerName).sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public GameOptions getProposedGameOptions(String proposerName, String proposalReceiverName) {
        return proposals.get(proposalReceiverName).get(proposerName);
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

    public void rejectGame(String rejectingPlayerName, String requestingPlayerName) {
        try {
            proposals.get(rejectingPlayerName).remove(requestingPlayerName);
            System.out.println("proposal removed");
        } catch (NullPointerException e) {
            System.out.println("Couldn't remove from proposals");
            System.out.println("rejectingPlayer was: "+rejectingPlayerName+", requestingPlayer was: "+requestingPlayerName);
            System.out.println("the following proposals were saved:");
            proposals.forEach((String rejectorName, Map<String, GameOptions> v) -> {
                v.forEach((String proposerName, GameOptions options)-> {
                    System.out.println(
                        "proposer: " +proposerName+
                        ", rejector: "+rejectorName+
                        ", options: "+options.toString()
                    );
                });
            });
        }
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("type", "game_reject");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        var message = new TextMessage(messageJson.toString());
        try {
            waitingPlayerSessions.get(requestingPlayerName).sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("waitingPlayerSessions did not contain a websocketSession "+
                "associated with \""+requestingPlayerName+"\"");
            System.out.println("waitingPlayerSessions has the following entries: "+waitingPlayerSessions.size());
            waitingPlayerSessions.forEach((String receiverUsername,WebSocketSession s)->{
                System.out.println(receiverUsername);
            });
            e.printStackTrace();
        }
    }
}
