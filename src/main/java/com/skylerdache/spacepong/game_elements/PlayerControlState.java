package com.skylerdache.spacepong.game_elements;

import com.skylerdache.spacepong.enums.LeftRightArrowState;
import com.skylerdache.spacepong.enums.UpDownArrowState;

public record PlayerControlState(LeftRightArrowState leftRightState,
                                 UpDownArrowState upDownState) {}
