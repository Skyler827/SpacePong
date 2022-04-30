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
    private Player proposer;
    @Transient
    @ManyToOne
    private Player subject;

    public GameOptions(HumanPlayer proposer, HumanPlayer subject, GameOptionsDto options) {
        this.setProposer(proposer);
        this.setSubject(subject);
        this.setScoreThreshold(options.getScoreThreshold());
        this.setTimeLimited(options.getIsTimeLimited().equals("true"));
        this.setTimeLimitMinutes(options.getTimeLimitMinutes());
    }
}
