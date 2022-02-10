package com.skylerdache.spacepong.controllers;

import com.skylerdache.spacepong.dto.PlayerDto;
import com.skylerdache.spacepong.entities.Player;
import com.skylerdache.spacepong.services.PlayerService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    private final PlayerService playerService;
    public RegistrationController(PlayerService playerService) {
        this.playerService = playerService;
    }
    @GetMapping
    public String getRegistration(WebRequest request, Model model) {
        PlayerDto playerDto = new PlayerDto();
        model.addAttribute("playerdto", playerDto);
        return "register";
    }
    @PostMapping
    public String postRegistration(@ModelAttribute("player") @Valid PlayerDto newPlayer, final BindingResult bindingResult) {
        UserDetails existingPlayer = playerService.loadUserByUsername(newPlayer.getUsername());
        if (existingPlayer != null) {
            System.out.println("Error: player already exists");
            return "redirect:/register";
        }
        playerService.registerNewPlayer(newPlayer);
        return "redirect:/login";
    }
}
