package com.skylerdache.spacepong.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skylerdache.spacepong.converter.ColorConverter;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.awt.Color;
import java.util.List;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="PLAYER_TYPE")
@Getter
@Setter
public abstract class Player {
    @Id
    @GeneratedValue
    private long id;

    private String username;
    @Convert(converter = ColorConverter.class)
    @JsonIgnore
    private Color paddleColor;
    @OneToMany(mappedBy = "player1")
    @JsonIgnore
    private List<Game> p1Games;
    @OneToMany(mappedBy = "player2")
    @JsonIgnore
    private List<Game> p2Games;

    private static @NotNull String getHexSegment(int i) {
        if (i >= 16) {
            return Integer.toHexString(i);
        } else {
            return "0" + Integer.toHexString(i);
        }
    }
    public @NotNull String getHexColor() {
        return "#"+ getHexSegment(paddleColor.getRed())
                +getHexSegment(paddleColor.getGreen())
                +getHexSegment(paddleColor.getGreen());
    }
}
