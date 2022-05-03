package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.entities.ComputerPlayer;
import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.game_elements.GameOptions;
import com.skylerdache.spacepong.services.GameService;
import com.skylerdache.spacepong.services.OnlineService;
import com.skylerdache.spacepong.services.PlayerService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/handle_proposal")
public class AcceptGameController {
    private final PlayerService playerService;
    private final OnlineService onlineService;
    private final GameService gameService;
    public AcceptGameController(PlayerService playerService, OnlineService onlineService, GameService gameService) {
        this.playerService = playerService;
        this.onlineService = onlineService;
        this.gameService = gameService;
    }
    @PostMapping("accept")
    public String handleAcceptGame(@RequestParam String username, Principal principal) {
        HumanPlayer challenger = playerService.getHumanPlayerByName(username);
        HumanPlayer acceptingPlayer = playerService.getHumanPlayerByName(principal.getName());
        GameOptions options = onlineService.getProposedGameOptions(challenger, acceptingPlayer);
        gameService.startGame(acceptingPlayer, challenger, options);
        return "redirect:/game";
    }
    @PostMapping("reject")
    public ResponseEntity<?> handleRejectGame(@RequestParam("username") String requesterUsername, Principal principal) {
        System.out.println("got here");
        HumanPlayer rejector = playerService.getHumanPlayerByName(principal.getName());
        HumanPlayer requestingPlayer = playerService.getHumanPlayerByName(requesterUsername);
        onlineService.rejectGame(rejector, requestingPlayer);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
