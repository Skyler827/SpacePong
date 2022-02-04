package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.services.PlayerService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        return "login";
    }
    @PostMapping
    public String postLogin(@RequestParam String username, @RequestParam String password, Model model) {
        Player player = playerService.getPlayerByName(username);
        if (player == null) {
            model.addAttribute("error", "invalid login");
            return "login";
        }

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authManager.authenticate(authReq);

        return "login";
    }
}
