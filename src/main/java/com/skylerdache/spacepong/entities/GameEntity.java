package com.skylerdache.spacepong.entities;

import com.skylerdache.spacepong.game_elements.GameOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GameEntity {
    @Id
    @GeneratedValue
    private long id;

    @Embedded
    private GameOptions options = new GameOptions();

    private final Instant initializeTime = Instant.now();
    private Instant startTime;
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
