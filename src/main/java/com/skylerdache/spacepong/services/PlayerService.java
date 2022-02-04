package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.repositories.PlayerRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {
    public PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player getPlayerByName(String username) {
        return playerRepository.findPlayerByUsername(username);
    }
}
