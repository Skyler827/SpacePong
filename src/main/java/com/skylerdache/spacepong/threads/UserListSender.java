package com.skylerdache.spacepong.threads;

import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.services.OnlineService;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.util.Pair;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class UserListSender extends Thread {
    /**
     * <code>playerSessions</code> contains the map from a users' username to the websocket
     * session for that player.
     */
    private final ConcurrentMap<String, WebSocketSession> playerSessions;

    //messaging variables:
    /**
     * <code>newSessions</code> is a map from the player's username to their browser's websocket session
     */
    @Getter
    private final ConcurrentMap<String,WebSocketSession> newSessions;

    /**
     * <code>disconnectingPlayers</code> contains the list of players disconnecting from being online.
     */
    @Getter
    private final BlockingQueue<Pair<String, WebSocketSession>> disconnectingPlayers;

    /**
     * <code>newGameRequests</code> contains a list of maps, with each map representing a request by one user to
     * start a game with another user. Each map contains the following attributes:
     * "proposer": username of the player who requested this game
     * "receiver": username of the player whom the requester is requesting to play with
     * "timeLimited": whether this is a time limited game, literal string "true" or "false"
     * "timeLimit": string representation of the integer number of minutes in the game. Ignored if timeLimited is "false"
     * "length": a number >50, <150
     */
    private final BlockingQueue<Map<String,Object>> newGameRequests;
    @Getter
    private final BlockingQueue<String> loggingOutPlayers;

    private final ConcurrentMap<String, String> rejectedRequests;

    /**
     * <code>waitingPlayerGamesStarting</code> contains the list of players who have requested a game,
     * they've been waiting for a game to start, and their proposal has been accepted. They
     * need to be redirected into their new game.
     */
    @Getter
    private final BlockingQueue<String> waitingPlayersGameStarting;

    private JSONObject playerData;

    private static final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public UserListSender() {
        this.playerSessions = new ConcurrentHashMap<>();
        this.newSessions = new ConcurrentHashMap<>();
        this.disconnectingPlayers = new LinkedBlockingQueue<>();
        this.newGameRequests = new LinkedBlockingQueue<>();
        this.waitingPlayersGameStarting = new LinkedBlockingQueue<>();
        JSONObject playerDataObject = new JSONObject();
        try {
            playerDataObject.put("type", "username_list");
            playerDataObject.put("data", new JSONArray());
        } catch(JSONException e) {throw new RuntimeException("Shouldn't happen");}
        this.playerData = playerDataObject;
        loggingOutPlayers = new LinkedBlockingQueue<>();
        rejectedRequests = new ConcurrentHashMap<>();
    }

    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            handleNewSessions();
            handleDisconnectingSessions();
            handleLoggingOutPlayers();
            handleNewGameRequests();
            informUsersRequestsRejected();
            handleWaitingGamesStarting();
            sendListToUsers();
            try {
                //noinspection BusyWait
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Shutting down");
            }
        }
    }

    private void handleNewSessions() {
        if (!newSessions.isEmpty()) {
            boolean newSessionIsCaptured = false;
            synchronized(newSessions) {
                if (!newSessions.isEmpty()) {
                    newSessionIsCaptured = true;
                    playerSessions.putAll(newSessions);
                    newSessions.clear();
                }
            }
            if (newSessionIsCaptured) {
                System.out.println("newSession came in");
                System.out.println(playerSessions);
                playerData = playerData(playerSessions);
                System.out.println("new playerData: "+playerData);
            }
        }
    }

    private void handleDisconnectingSessions() {
        if (disconnectingPlayers.isEmpty()) {
            return;
        }
        boolean anyPlayerDisconnected = false;
        synchronized(disconnectingPlayers) {
            if (!disconnectingPlayers.isEmpty()) {
                anyPlayerDisconnected = true;
                System.out.println("removing "+disconnectingPlayers.size()+" player from online");
                for (Pair<String,WebSocketSession> disconnecting : disconnectingPlayers) {
                    String playerName = disconnecting.getFirst();
                    WebSocketSession conn = disconnecting.getSecond();
                    if (playerSessions.get(playerName) != conn) {
                        System.out.println("unable to remove player "+playerName);
                    }
                }
                disconnectingPlayers.forEach((pair) ->
                    playerSessions.remove(pair.getFirst(), pair.getSecond())
                );
                disconnectingPlayers.clear();
            }
        } if (anyPlayerDisconnected) {
            playerData = playerData(playerSessions);
            System.out.println("new playerData: "+playerData);
        }
    }
    private void handleLoggingOutPlayers() {
        if (loggingOutPlayers.isEmpty()) {return;}
        synchronized(loggingOutPlayers) {
            loggingOutPlayers.forEach(playerSessions::remove);
            loggingOutPlayers.clear();
        }
    }

    private void informUsersRequestsRejected() {
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("type", "game_reject");
            messageJson.put("data", JSONObject.NULL);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        var message = new TextMessage(messageJson.toString());
        if (!rejectedRequests.isEmpty()) {
            synchronized(rejectedRequests) {
                rejectedRequests.forEach((String rejector, String rejected) -> {
                    try {
                        playerSessions.get(rejected).sendMessage(message);
                    } catch (IOException e) {throw new RuntimeException(e);}
                });
            }
        }
    }
    private void handleWaitingGamesStarting() {
        if (waitingPlayersGameStarting.isEmpty()) {
            return;
        }
        synchronized(waitingPlayersGameStarting) {
            if (!waitingPlayersGameStarting.isEmpty()) {
                waitingPlayersGameStarting.forEach(this::sendMessageGameStarting);
                waitingPlayersGameStarting.clear();
            }
        }
    }
    private void handleNewGameRequests() {
        if (newGameRequests.isEmpty()) return;
        synchronized (newGameRequests) {
            for (Map<String, Object> requestInfo : newGameRequests) {
                JSONObject messageJSON = new JSONObject();
                JSONObject dataJSON = new JSONObject();
                for (String requestInfoKey : requestInfo.keySet()) {
                    Object value = requestInfo.get(requestInfoKey);
                    try {
                        dataJSON.put(requestInfoKey,value);
                    }
                    catch (JSONException e) {
                        try {dataJSON.put(requestInfoKey,"[invalid expression]");}
                        catch (JSONException e2) {throw new RuntimeException("Shouldn't happen");}
                    }
                }
                try {
                    messageJSON.put("type","notify_game_request");
                    messageJSON.put("data",dataJSON);
                } catch (JSONException e) {throw new RuntimeException("shouldn't happen");}
                WebSocketSession s = playerSessions.get((String)requestInfo.get("receiver"));
                try {
                    s.sendMessage(new TextMessage(messageJSON.toString()));
                } catch (IOException e) {
                    //TODO: handle this
                }
            }
            newGameRequests.clear();
        }
    }

    private void sendListToUsers() {
        ArrayList<String> usersToRemove = new ArrayList<>();
        playerSessions.forEach((String username, WebSocketSession s) -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(playerData.toString()));
                } catch (IOException e) {
                    usersToRemove.add(username);
                    e.printStackTrace();
                }
            } else {
                usersToRemove.add(username);
            }
        });
        usersToRemove.forEach(playerSessions::remove);
    }

    private static JSONObject playerData(Map<String,WebSocketSession> playerSessions) {
        JSONArray usernameList = new JSONArray();
        JSONObject messageObject = new JSONObject();
        playerSessions.forEach((username,session)->usernameList.put(username));
        try {
            messageObject.put("type", "username_list");
            messageObject.put("data", usernameList);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RuntimeException("this shouldn't happen");
        }
        return messageObject;
    }

    private void sendMessageGameStarting(String username) {
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put("type", "game_start");
            messageObject.put("data", "PROPOSAL_ACCEPTED");
        } catch (JSONException e) {throw new RuntimeException("shouldn't happen");}
        TextMessage socketMessage = new TextMessage(messageObject.toString());
        try {
            playerSessions.get(username).sendMessage(socketMessage);
        } catch (IOException e) {
            e.printStackTrace();
            playerSessions.remove(username);
        }
    }

    public Future<List<String>> getOnlineUsers() {
        var futureTask = new FutureTask<>(() -> {
            synchronized (playerSessions) {
                return playerSessions.keySet().stream().toList();
            }
        });
        threadPool.submit(futureTask);
        return futureTask;
    }

    /**
     * <code>notifyGameRequested</code> queues info about newly requested games to be sent to the subjects of those requests.
     * Data is copied into a map of strings to avoid issues with shared access to entity objects between threads.
     * @param options
     */
    public void notifyGameRequested(GameOptions options) {
        Map<String,Object> newGameRequestMap = new HashMap<>();
        newGameRequestMap.put("proposer",options.getProposerName());
        newGameRequestMap.put("receiver",options.getProposalReceiverName());
        newGameRequestMap.put("isTimeLimited",options.isTimeLimited());
        newGameRequestMap.put("timeLimit",options.getTimeLimitMinutes());
        newGameRequestMap.put("length",100);
        newGameRequests.add(newGameRequestMap);
    }
}
