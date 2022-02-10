package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.entities.Game;
import com.skylerdache.spacepong.repositories.GameRepository;
import com.skylerdache.spacepong.services.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/game")
public class GameController {
    private final GameRepository gameRepository;
    public GameController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }
    @GetMapping
    public String getGame(Model model) {
        Game g = new Game();
        g.setP1Score(10);
        g.setP2Score(5);
        Game newGame = gameRepository.save(g);
        model.addAttribute("newGameId", newGame.getId());
        System.out.println(newGame.getId());
        return "game";
    }
}
