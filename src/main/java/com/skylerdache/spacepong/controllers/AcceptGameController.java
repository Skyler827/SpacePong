package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.entities.ComputerPlayer;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.services.GameService;
import com.skylerdache.spacepong.services.OnlineService;
import com.skylerdache.spacepong.services.PlayerService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class AcceptGameController {
    private final PlayerService playerService;
    private final OnlineService onlineService;
    private final GameService gameService;
    public AcceptGameController(PlayerService playerService, OnlineService onlineService, GameService gameService) {
        this.playerService = playerService;
        this.onlineService = onlineService;
        this.gameService = gameService;
    }
    @PostMapping("/accept_game")
    public String handleAcceptGame(@NotNull HumanPlayer p, int challengerUserId) {
        Optional<Player> optionalChallenger = playerService.getPlayerById(challengerUserId);
        if (optionalChallenger.isEmpty()) {
            Exception e = new RuntimeException("this shouldn't happen");
            e.printStackTrace();
            return "/error";
        }
        Player challenger = optionalChallenger.get();
        if (challenger instanceof HumanPlayer humanOpponent) {
            GameOptions options = onlineService.getProposedGameOptions(humanOpponent, p);
            gameService.startGame(p, humanOpponent, options);
            return "redirect:/game";
        } else if (challenger instanceof ComputerPlayer) {
            throw new RuntimeException("players will never accept proposed matches from computer players; only human players");
        } else {
            throw new RuntimeException("computerPlayer and humanPlayer are the only subclasses of Player");
        }
    }
}
