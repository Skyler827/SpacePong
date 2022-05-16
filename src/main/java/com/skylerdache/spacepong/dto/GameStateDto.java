package com.skylerdache.spacepong.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.json.JSONObject;

import java.io.Serializable;
import java.time.Instant;

@JsonSerialize
public record GameStateDto (
    boolean paused,
    int p1Score,
    int p2Score,
    String tickInstant,
    double p1PaddleX,
    double p1PaddleY,
    double p1PaddleZ,
    double p1PaddleVx,
    double p1PaddleVy,
    double p1PaddleVz,
    double p2PaddleX,
    double p2PaddleY,
    double p2PaddleZ,
    double p2PaddleVx,
    double p2PaddleVy,
    double p2PaddleVz,
    double ballX,
    double ballY,
    double ballZ,
    double ballVx,
    double ballVy,
    double ballVz
) implements Serializable {
    public GameStateDto() {
        this(
            true,
            0,
            0,
            Instant.now().toString(),
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
        );
    }
}
