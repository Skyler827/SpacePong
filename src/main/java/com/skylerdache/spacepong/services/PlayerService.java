package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.dto.PlayerDto;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.repositories.HumanPlayerRepository;
import com.skylerdache.spacepong.repositories.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService implements UserDetailsService {
    private final PlayerRepository playerRepository;
    private final HumanPlayerRepository humanPlayerRepository;
    private final PasswordEncoder passwordEncoder;

    public PlayerService(PlayerRepository playerRepository, HumanPlayerRepository humanPlayerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.humanPlayerRepository = humanPlayerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Player getPlayerByName(String username) {
        return playerRepository.findPlayerByUsername(username);
    }

    public HumanPlayer getHumanPlayerByName(String username) {
        return humanPlayerRepository.getHumanPlayerByUsername(username);
    }
    public void registerNewPlayer(PlayerDto newPlayer) {
        HumanPlayer p = new HumanPlayer();
        p.setUsername(newPlayer.getUsername());
        p.setPassword(passwordEncoder.encode(newPlayer.getPassword()));
        p.setPaddleColor(newPlayer.getColorHex());
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

    public Optional<Player> getPlayerById(int opponentId) {
        return playerRepository.findById((long) opponentId);
    }
}
