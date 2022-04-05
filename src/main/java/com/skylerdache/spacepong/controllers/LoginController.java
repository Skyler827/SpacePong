package com.skylerdache.spacepong.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {
    public LoginController() {}
    @GetMapping
    public String getLogin() {
        return "login";
    }
}
