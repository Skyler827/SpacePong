package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.dto.PlayerDto;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.services.PlayerService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/login")
public class LoginController {
    private final AuthenticationManager authManager;
    private final PlayerService playerService;
    public LoginController(AuthenticationManager authManager, PlayerService playerService) {
        this.authManager = authManager;
        this.playerService = playerService;
    }
    @GetMapping
    public String getLogin(Model model) {
        model.addAttribute("playerdto", new PlayerDto());
        return "login";
    }
    @PostMapping
    public String postLogin(@ModelAttribute("playerdto") PlayerDto playerDto, Model model) {
        Player player = playerService.getPlayerByName(playerDto.getUsername());
        if (player == null) {
            model.addAttribute("error", "invalid login");
            return "login";
        }

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(
                playerDto.getUsername(), playerDto.getPassword());
        Authentication auth = authManager.authenticate(authReq);

        return "redirect:/";
    }
}
