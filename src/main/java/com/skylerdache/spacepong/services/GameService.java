package com.skylerdache.spacepong.services;

import com.skylerdache.spacepong.repositories.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

}
