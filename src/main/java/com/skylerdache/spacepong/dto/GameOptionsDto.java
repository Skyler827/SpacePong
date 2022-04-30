package com.skylerdache.spacepong.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GameOptionsDto {
    private int length = 100;
    private int width = 100;
    private int height = 100;
    private int opponentId = 2;
    private int scoreThreshold = 10;
    private String isTimeLimited = "true";
    private int timeLimitMinutes = 3;
}
