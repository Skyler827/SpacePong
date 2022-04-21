package com.skylerdache.spacepong.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.game_elements.PlayerControlState;
import lombok.Getter;
import lombok.Setter;
import nonapi.io.github.classgraph.json.JSONSerializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@DiscriminatorValue("Human")
public class HumanPlayer extends Player implements UserDetails {
    @Getter
    @Setter
    @JsonIgnore
    private String password;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        var auths = new ArrayList<GrantedAuthority>();
        auths.add(new SimpleGrantedAuthority("USER"));
        return auths;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

}
