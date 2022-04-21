package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.dto.GameOptionsDto;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import javax.persistence.ManyToOne;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameOptions {
    private int scoreThreshold = 10;
    private int timeLimitMinutes = 3;
    private boolean isTimeLimited = true;
    @Transient
    @ManyToOne
    private Player p1;
    @Transient
    @ManyToOne
    private Player p2;

    public GameOptions(HumanPlayer proposer, HumanPlayer subject, GameOptionsDto options) {
        this.setP1(proposer);
        this.setP2(subject);
        this.setScoreThreshold(options.getScoreThreshold());
        this.setTimeLimited(options.isTimeLimited());
        this.setTimeLimitMinutes(options.getTimeLimitMinutes());
    }
}
