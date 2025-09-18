package com.f1soft.authserver.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // This should resolve to src/main/resources/templates/login.html
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OidcUser user, Model model) {
        if (user != null) {
            model.addAttribute("user", user.getAttributes());
            model.addAttribute("idToken", user.getIdToken().getTokenValue());
        }
        return "home"; // This should resolve to src/main/resources/templates/home.html
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }
}