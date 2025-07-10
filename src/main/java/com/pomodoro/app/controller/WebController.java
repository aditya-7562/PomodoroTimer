package com.pomodoro.app.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    
    @GetMapping(value = "/")
    public String index() {
        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            // Redirect to dashboard if user is logged in
            return "redirect:/user/dashboard";
        }
        
        // Otherwise show the landing page
        return "index";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/user/dashboard";
    }
}