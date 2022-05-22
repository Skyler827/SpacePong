package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.dto.GameOptionsDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameOptions {
    private int scoreThreshold = 10;
    private int timeLimitMinutes = 3;
    private boolean isTimeLimited = true;
    private String proposerName;
    private String proposalReceiverName;

    public GameOptions(String proposerName, String proposalReceiverName, GameOptionsDto options) {
        this.proposerName = proposerName;
        this.proposalReceiverName = proposalReceiverName;
        this.scoreThreshold = options.getScoreThreshold();
        this.isTimeLimited = options.getIsTimeLimited().equals("true");
        this.timeLimitMinutes = options.getTimeLimitMinutes();
    }

    public SpaceBounds getBounds() {
        return new SpaceBounds();
    }
}
