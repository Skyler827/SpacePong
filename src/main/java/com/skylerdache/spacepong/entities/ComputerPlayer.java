package com.skylerdache.spacepong.entities;

import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.game_elements.PlayerControlState;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CPU")
public class ComputerPlayer extends Player {
}
