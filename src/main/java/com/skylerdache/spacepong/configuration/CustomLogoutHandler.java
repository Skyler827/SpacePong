package com.skylerdache.spacepong.configuration;

import com.skylerdache.spacepong.entities.HumanPlayer;
import com.skylerdache.spacepong.services.OnlineService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class CustomLogoutHandler implements LogoutHandler {
    private final OnlineService onlineService;

    public CustomLogoutHandler(OnlineService onlineService) {
        this.onlineService = onlineService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        System.out.println("user "+authentication.getName()+" has logged out (CustomLogoutHandler)");
        onlineService.handleLogout((HumanPlayer) authentication.getPrincipal());
    }
}
