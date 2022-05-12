package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.dto.GameStateDto;
import com.skylerdache.spacepong.entities.GameEntity;
import com.skylerdache.spacepong.services.GameService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameRestController {
    private final GameService gameService;
    public GameRestController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/restgames")
    List<GameEntity> all() {
        return gameService.getAll();
    }
//    @GetMapping("/restgames/{id}")
//    GameStateDto gameStatus(@PathVariable long id) {
//        System.out.println("/restgames/{id} is:");
//        System.out.println(id);
//        return gameService.getGameState(id);
//    }
}
