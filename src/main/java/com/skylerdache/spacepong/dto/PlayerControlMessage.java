package com.skylerdache.spacepong.dto;

import com.skylerdache.spacepong.enums.LeftRightArrowState;
import com.skylerdache.spacepong.enums.PlayerPosition;
import com.skylerdache.spacepong.enums.UpDownArrowState;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

public record PlayerControlMessage (
    PlayerPosition playerPosition,
    LeftRightArrowState lrState,
    UpDownArrowState udState,
    long gameId,
    Instant time
) {
    public PlayerControlMessage(JSONObject o) throws JSONException{
        this(
            PlayerPosition.valueOf(o.getString("playerPosition")),
            LeftRightArrowState.valueOf(o.getString("lrState")),
            UpDownArrowState.valueOf(o.getString("udState")),
            o.getInt("gameId"),
            Instant.parse(o.getString("time"))
        );
    }
    public PlayerControlMessage() {
        this(
            PlayerPosition.P1,
            LeftRightArrowState.NONE,
            UpDownArrowState.NONE,
            4,
            Instant.now()
        );
    }
    public JSONObject getJson() {
        try {
            JSONObject o = new JSONObject();
            o.put("playerPosition", playerPosition);
            o.put("lrState", lrState);
            o.put("udState", udState);
            o.put("gameId",gameId);
            o.put("time", time.toString());
            return o;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

