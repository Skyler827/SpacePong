package com.skylerdache.spacepong.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class WaitingGameController {
    @GetMapping("/waiting")
    public String getWaiting(RedirectAttributes attributes, Model model) {
        Map<String, ?> attributesMap = attributes.getFlashAttributes();
        model.addAttribute("opponent", attributesMap.get("opponent"));
        return "waiting";
    }

}
