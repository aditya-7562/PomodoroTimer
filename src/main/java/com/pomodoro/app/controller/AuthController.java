package com.pomodoro.app.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.pomodoro.app.dto.UserLoginDto;
import com.pomodoro.app.dto.UserRegistrationDto;
import com.pomodoro.app.model.User;
import com.pomodoro.app.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userRegistrationDto") UserRegistrationDto registrationDto,
                              BindingResult result, Model model) {
        // Check if passwords match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.userRegistrationDto", "Passwords do not match");
        }

        // Check if username already exists
        if (userService.findByUsername(registrationDto.getUsername()).isPresent()) {
            result.rejectValue("username", "error.userRegistrationDto", "Username already exists");
        }

        // Check if email already exists
        if (userService.findByEmail(registrationDto.getEmail()).isPresent()) {
            result.rejectValue("email", "error.userRegistrationDto", "Email already exists");
        }

        if (result.hasErrors()) {
            return "register";
        }

        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(registrationDto.getPassword());

        userService.registerNewUser(user);

        return "redirect:/login?registered";
    }
}