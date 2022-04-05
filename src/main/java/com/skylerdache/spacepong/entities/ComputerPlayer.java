package com.skylerdache.spacepong.entities;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CPU")
public class ComputerPlayer extends Player {
}
