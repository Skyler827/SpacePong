package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.dto.GameOptionsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameOptions {
    private int scoreThreshold = 10;
    private int timeLimitMinutes = 3;
    private boolean isTimeLimited = true;
    private String proposerName;
    private String proposalReceiverName;
    private double paddleXLength;
    private double paddleYLength;
    private double paddleZLength;

    public GameOptions(String proposerName, String proposalReceiverName, @NotNull GameOptionsDto options) {
        this.proposerName = proposerName;
        this.proposalReceiverName = proposalReceiverName;
        this.scoreThreshold = options.getScoreThreshold();
        this.isTimeLimited = options.getIsTimeLimited().equals("true");
        this.timeLimitMinutes = options.getTimeLimitMinutes();
        this.paddleXLength = Paddle.DEFAULT_X_LENGTH;
        this.paddleYLength = Paddle.DEFAULT_Y_LENGTH;
        this.paddleZLength = Paddle.DEFAULT_Z_LENGTH;
    }

    public SpaceBounds getBounds() {
        return new SpaceBounds();
    }

    public JSONObject getJsonObject() {
        JSONObject o = new JSONObject();
        try {
            o.put("scoreThreshold", scoreThreshold);
            o.put("timeLimitMinutes", timeLimitMinutes);
            o.put("isTimeLimited", isTimeLimited);
            o.put("paddleXLength", paddleXLength);
            o.put("paddleYLength", paddleYLength);
            o.put("paddleZLength", paddleZLength);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return o;
    }
}
