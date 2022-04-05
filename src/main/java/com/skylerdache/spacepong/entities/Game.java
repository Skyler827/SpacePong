package com.skylerdache.spacepong.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue
    private long id;

    private Instant startTime = Instant.now();
    private Instant endTime;
    @ManyToOne
    private Player player1;
    @ManyToOne
    private Player player2;

    @ManyToOne
    private Player winner;
    private int p1Score = 0;
    private int p2Score = 0;
}
