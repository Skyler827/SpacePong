package com.skylerdache.spacepong.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@Controller
@RequestMapping(path= "/")
public class HomeController {
    public HomeController() {}
    @GetMapping
    public String getHome(Model model, Principal principal) {
        if (principal == null) {
            model.addAttribute("unauthenticated", "unauthenticated");
            model.addAttribute("welcomeMessage", "Welcome guest!");
        } else {
            model.addAttribute("username",principal.getName());
            model.addAttribute("welcomeMessage", "Welcome, "+principal.getName()+"!");
        }
        return "home";
    }
}
