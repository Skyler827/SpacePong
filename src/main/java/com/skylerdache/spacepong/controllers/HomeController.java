package com.skylerdache.spacepong.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(path= "/")
public class HomeController {
    public HomeController() {}
    @GetMapping
    public String getHome() {
        return "home";
    }
}
