package com.skylerdache.spacepong.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    @GetMapping
    public String getRegistration() {
        return "register";
    }
    @PostMapping
    public String postRegistration() {
        return "register";
    }
}
