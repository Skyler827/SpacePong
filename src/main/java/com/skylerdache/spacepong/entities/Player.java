package com.skylerdache.spacepong.entities;

import com.skylerdache.spacepong.converter.ColorConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.awt.*;
import java.util.List;

@Entity
@Getter
@Setter
public class Player {
    @Id
    @GeneratedValue
    private long id;

    private String username;
    @Transient private String password;
    private String hashedPassword;
    private String salt;
    @Convert(converter = ColorConverter.class)
    private Color paddleColor;
    @OneToMany
    private List<Game> games;
}
