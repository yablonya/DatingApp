package org.example.datingapp.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.example.datingapp.models.Profile;
import org.example.datingapp.services.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class HomeController {
    private ProfileService profileService;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/")
    public String home(@CookieValue(value = "userId", required = false) String userIdCookie, Model model) {
        profileService.addLoggedInUserToModel(userIdCookie, model);
        return "home";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String openInformation,
            @RequestParam String closedInformation,
            HttpServletResponse response
    ) {
        try {
            Profile registeredUser = profileService.registerUser(name, email, password, openInformation, closedInformation);
            profileService.addUserIdToCookie(response, registeredUser);
            return "redirect:/profiles";
        } catch (IllegalArgumentException e) {
            logger.error("Registration error: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response,
            Model model
    ) {
        try {
            Profile user = profileService.loginUser(email, password);
            profileService.addUserIdToCookie(response, user);
            return "redirect:/profiles";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "home";
        }
    }

    @PostMapping("/logout")
    public String logout(
            @CookieValue(value = "userId", required = false) String userIdCookie,
            HttpServletResponse response
    ) {
        if (userIdCookie != null) {
            profileService.removeUserIdCookie(response);
        }
        return "redirect:/";
    }
}
