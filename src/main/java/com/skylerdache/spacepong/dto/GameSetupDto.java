package com.skylerdache.spacepong.dto;

import com.skylerdache.spacepong.entities.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GameSetupDto {
    private long opponentId;
    private int scoreThreshold = 10;
    private boolean isTimeLimited = true;
    private int timeLimitMinutes = 3;
}
