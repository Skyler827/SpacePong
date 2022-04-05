package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.dto.GameSetupDto;
import com.skylerdache.spacepong.entities.Game;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.services.GameService;
import com.skylerdache.spacepong.services.PlayerService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/setup_game")
public class SetupGameController {
    private final GameService gameService;
    private final PlayerService playerService;
    public SetupGameController(GameService gameService, PlayerService playerService) {
        this.gameService = gameService;
        this.playerService = playerService;
    }
    @GetMapping
    public String getSetupGame(Model model, RedirectAttributes attributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Player> otherPlayers = playerService.getOnlineUsers().stream()
            .filter((Player p) -> !p.getUsername().equals(auth.getName()))
            .collect(Collectors.toList());
        if (otherPlayers.size() == 0) {
            System.out.println("cannot start game with no other players to play against");
            attributes.addFlashAttribute("warning", "cannot start game with no other players to play against");
            return "redirect:/";
        }
        else {
            model.addAttribute("players", otherPlayers);
            model.addAttribute("playersSize", Math.min(otherPlayers.size(), 20));
            model.addAttribute("gameSetupDto", new GameSetupDto());
            return "setup_game";
        }
    }
    @PostMapping
    public String postSetupGame(@ModelAttribute GameSetupDto gameSetupDto, RedirectAttributes attributes, Principal principal) {
        System.out.println(gameSetupDto.toString());
        System.out.println("postSetupGame() called");
        Player user = playerService.getPlayerByName(principal.getName());
        Player opponent = playerService.getPlayerByName("user1");

        Game g = gameService.startNewGame(user, opponent);
        attributes.addFlashAttribute("game", g);
        return "redirect:/game";
    }
}
