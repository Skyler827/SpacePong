package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.entities.Game;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.exceptions.NoSuchGameException;
import com.skylerdache.spacepong.services.GameService;
import com.skylerdache.spacepong.services.PlayerService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class GameController {
    private final GameService gameService;
    private final PlayerService playerService;
    public GameController(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }
    @GetMapping("/game")
    public String getGame(Model model, RedirectAttributes attributes, Principal principal) {
        final Game game = (Game) attributes.getFlashAttributes().get("game");
        if (game == null) {
            try {
                Player p = playerService.getPlayerByName(principal.getName());
                model.addAttribute("gameId", gameService.getOngoingGameByPlayer(p).getId());
            } catch (NoSuchGameException e) {
                String message = "You are not in any current game";
                System.out.println(message);
                attributes.addFlashAttribute("warning", message);
                return "redirect:/setup_game";
            }
        } else {
            model.addAttribute("gameId", game.getId());
        }
        return "game";
    }
    @MessageMapping()
    public void handleMessage() {}
}
