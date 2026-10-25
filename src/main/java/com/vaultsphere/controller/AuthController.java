
package com.vaultsphere.controller;

import com.vaultsphere.model.AppUser;
import com.vaultsphere.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") AppUser user,
            BindingResult result,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // Check whether the username already exists
        if (user.getUsername() != null
                && users.existsByUsername(user.getUsername().trim())) {
            result.rejectValue(
                    "username",
                    "duplicate",
                    "Username is already taken."
            );
        }

        // Check whether the email already exists
        if (user.getEmail() != null
                && users.existsByEmail(user.getEmail().trim())) {
            result.rejectValue(
                    "email",
                    "duplicate",
                    "Email is already registered."
            );
        }

        // Check password length
        if (user.getPassword() == null
                || user.getPassword().length() < 8) {
            result.rejectValue(
                    "password",
                    "weak",
                    "Password must be at least 8 characters."
            );
        }

        // Check password confirmation
        if (!java.util.Objects.equals(
                user.getPassword(), confirmPassword)) {
            model.addAttribute(
                    "passwordError",
                    "Passwords do not match."
            );
        }

        // Return to registration page if validation fails
        if (result.hasErrors()
                || model.containsAttribute("passwordError")) {
            return "register";
        }

        // Never accept a role supplied by the registration form
        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim());
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);

        users.save(user);

        return "redirect:/login?registered";
    }
}
