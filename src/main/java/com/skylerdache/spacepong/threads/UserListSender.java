package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.services.OnlineService;
import lombok.Getter;
import nonapi.io.github.classgraph.json.JSONSerializer;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

public class UserListSender extends Thread {
    /**
     * playerSessions is the main variable for storing player sessions
     */
    private final Map<HumanPlayer, WebSocketSession> playerSessions;

    //messaging variables:
    @Getter
    private final ConcurrentMap<HumanPlayer,WebSocketSession> newSessions;
    @Getter
    private final BlockingQueue<HumanPlayer> disconnectingPlayers;
    @Getter
    private final BlockingQueue<HumanPlayer> waitingPlayersGameStarting;
    private final OnlineService onlineService;
    public UserListSender(OnlineService onlineService) {
        this.playerSessions = new HashMap<>();
        this.newSessions = new ConcurrentHashMap<>();
        this.disconnectingPlayers = new LinkedBlockingQueue<>();
        this.waitingPlayersGameStarting = new LinkedBlockingQueue<>();
        this.onlineService = onlineService;
    }
    private static String combineToJsonList(Stream<String> jsonStrings) {
        String newLine = System.getProperty("line.separator");
        String results =  jsonStrings.collect(
            StringBuilder::new,
            (StringBuilder sb, String json)->
                sb.append(",")
                .append(newLine)
                .append(json),
            StringBuilder::append
        ).toString();
        String resultsWithoutLeadingComma = results.substring(1).stripLeading();
        return "["+resultsWithoutLeadingComma+"]";
    }
    public void run() {
        String playerData = JSONSerializer.serializeObject(playerSessions.keySet().stream().toList());
        //noinspection InfiniteLoopStatement
        while (true) {
            if (!newSessions.isEmpty()) {
                synchronized(newSessions) {
                    if (!newSessions.isEmpty()) {
                        playerSessions.putAll(newSessions);
                        playerData = combineToJsonList(playerSessions.keySet().stream().map(HumanPlayer::getJson));
                        System.out.println("new playerData: "+playerData);
                        onlineService.setOnlinePlayersJSON(playerData);
                        newSessions.clear();
                    }
                }
            }
            if (!disconnectingPlayers.isEmpty()) {
                synchronized(disconnectingPlayers) {
                    if (!disconnectingPlayers.isEmpty()) {
                        disconnectingPlayers.forEach(playerSessions::remove);
                        disconnectingPlayers.clear();
                    }
                }
            }
            if (!waitingPlayersGameStarting.isEmpty()) {
                synchronized(waitingPlayersGameStarting) {
                    if (!waitingPlayersGameStarting.isEmpty()) {
                        waitingPlayersGameStarting.forEach((HumanPlayer p) ->{
                            try {
                                playerSessions.get(p).sendMessage(
                                    new TextMessage("PROPOSAL_ACCEPTED"));
                            } catch (IOException e) {
                                e.printStackTrace();
                                playerSessions.remove(p);
                            }
                        });
                        waitingPlayersGameStarting.clear();
                    }
                }
            }
            ArrayList<HumanPlayer> usersToRemove = new ArrayList<>();
            String finalPlayerData = playerData;
//            System.out.println("sending the following JSON data to players:");
//            System.out.println(finalPlayerData);
            playerSessions.forEach((HumanPlayer p, WebSocketSession s) -> {
                try {
                    s.sendMessage(new TextMessage(finalPlayerData));
                } catch (IOException e) {
                    usersToRemove.add(p);
                    System.out.println("caught exception here");
                    e.printStackTrace();
                }
            });
            usersToRemove.forEach(playerSessions::remove);
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<HumanPlayer> getOnlineUsers() {
        return null;
    }
}
