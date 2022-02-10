package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.PlayerDto;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.repositories.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.awt.Color;

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

    public long registerNewPlayer(PlayerDto newPlayer) {
        System.out.println("got into registerNewPlayer");
        Player p = new Player();
        p.setUsername(newPlayer.getUsername());
        p.setPassword(passwordEncoder.encode(newPlayer.getPassword()));
        p.setPaddleColor(Color.decode(newPlayer.getColorHex()));
        Player savedPlayer = playerRepository.save(p);
        return savedPlayer.getId();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerRepository.findPlayerByUsername(username);
        return player;
    }
}
