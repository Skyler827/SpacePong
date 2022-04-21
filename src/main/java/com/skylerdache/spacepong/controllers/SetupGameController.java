package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.dto.GameOptionsDto;
import com.skylerdache.spacepong.entities.ComputerPlayer;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.services.OnlineService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/setup_game")
public class SetupGameController {
    private final PlayerService playerService;
    private final OnlineService onlineService;
    public SetupGameController(PlayerService playerService, OnlineService onlineService) {
        this.playerService = playerService;
        this.onlineService = onlineService;
    }
    @GetMapping
    public String getSetupGame(Model model, RedirectAttributes attributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Player> otherPlayers = onlineService.getPlayers().stream()
            .filter((HumanPlayer p) -> !p.getUsername().equals(auth.getName()))
            .collect(Collectors.toList());
        if (otherPlayers.size() == 0) {
            System.out.println("cannot start game with no other players to play against");
            attributes.addFlashAttribute("warning", "cannot start game with no other players to play against");
            return "redirect:/";
        }
        else {
            model.addAttribute("playerId", 4);
            model.addAttribute("players", otherPlayers);
            model.addAttribute("playersSize", Math.min(otherPlayers.size(), 20));
            model.addAttribute("gameSetupDto", new GameOptionsDto());
            return "setup_game";
        }
    }
    @PostMapping
    public String postSetupGame(@ModelAttribute GameOptionsDto gameSetupDto, RedirectAttributes attributes, Principal principal) {
        System.out.println(gameSetupDto.toString());
        System.out.println("postSetupGame() called");
        HumanPlayer user = (HumanPlayer) playerService.getPlayerByName(principal.getName());
        Player opponent = playerService.getPlayerByName("user2");
        if (opponent instanceof HumanPlayer humanOpponent) {
            onlineService.proposeNewGame(user, humanOpponent, gameSetupDto);
            return "redirect:/waiting";
        } else if (opponent instanceof ComputerPlayer computerOpponent) {
            //TODO: start a game immediately
            System.out.println("fighting computer player: "+computerOpponent.getUsername());
            return "redirect:/game";
        } else {
            throw new RuntimeException("this shouldn't happen");
        }
    }
}
