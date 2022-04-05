package com.skylerdache.spacepong.dto;

import lombok.Data;

@Data
public class GameStateDto {
    int p1Score;
    int p2Score;
    double p1PaddleX;
    double p1PaddleY;
    double p1PaddleZ;
    double p1PaddleVx;
    double p1PaddleVy;
    double p1PaddleVz;
    double p2PaddleX;
    double p2PaddleY;
    double p2PaddleZ;
    double p2PaddleVx;
    double p2PaddleVy;
    double p2PaddleVz;
    double ballX;
    double ballY;
    double ballZ;
    double ballVx;
    double ballVy;
    double ballVz;
}
