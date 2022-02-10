package com.skylerdache.spacepong.entities;

import com.skylerdache.spacepong.converter.ColorConverter;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Entity
@Getter
@Setter
public class Player implements UserDetails {
    @Id
    @GeneratedValue
    private long id;

    private String username;
    private String password;
    @Convert(converter = ColorConverter.class)
    private Color paddleColor;
    @OneToMany(mappedBy = "player1")
    private List<Game> p1Games;
    @OneToMany(mappedBy = "player2")
    private List<Game> p2Games;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    private String getHexSegment(int i) {
        if (i >= 16) {
            return Integer.toHexString(i);
        } else {
            return "0" + Integer.toHexString(i);
        }
    }
    public String getHexColor() {
        return "#"+ getHexSegment(paddleColor.getRed())
                +getHexSegment(paddleColor.getGreen())
                +getHexSegment(paddleColor.getGreen());
    }
}
