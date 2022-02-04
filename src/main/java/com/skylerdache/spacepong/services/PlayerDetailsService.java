package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.configuration.PlayerPrincipal;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.repositories.PlayerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PlayerDetailsService implements UserDetailsService {
    PlayerRepository playerRepository;
    PlayerDetailsService(PlayerRepository playerRepository) {

        this.playerRepository = playerRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Player player = playerRepository.findPlayerByUsername(username);
        if (player == null) {throw new UsernameNotFoundException("");}
        return new PlayerPrincipal(player);
    }
}
