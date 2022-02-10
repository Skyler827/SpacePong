package com.skylerdache.spacepong.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue
    private long id;

    private Date startTime;
    private Date endTime;
    @ManyToOne
    private Player player1;
    @ManyToOne
    private Player player2;

    private int p1Score;
    private int p2Score;
    private int winningScore;
}
