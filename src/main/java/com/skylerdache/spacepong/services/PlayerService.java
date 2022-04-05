package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.PlayerDto;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.repositories.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class PlayerService implements UserDetailsService {
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public PlayerService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Player getPlayerByName(String username) {
        return playerRepository.findPlayerByUsername(username);
    }

    public void registerNewPlayer(PlayerDto newPlayer) {
        HumanPlayer p = new HumanPlayer();
        p.setUsername(newPlayer.getUsername());
        p.setPassword(passwordEncoder.encode(newPlayer.getPassword()));
        p.setPaddleColor(Color.decode(newPlayer.getColorHex()));
        playerRepository.save(p);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return (HumanPlayer) playerRepository.findPlayerByUsername(username);
        } catch (ClassCastException e) {
            throw new UsernameNotFoundException(username+" is a computer player");
        }
    }
    public List<Player> getOnlineUsers() {
        return new ArrayList<Player>((Collection<? extends Player>) playerRepository.findAll());
    }
}
