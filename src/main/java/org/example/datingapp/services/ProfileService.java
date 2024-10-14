package org.example.datingapp.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.repositories.ProfileRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final Logger logger;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, Logger prototypeLogger) {
        this.profileRepository = profileRepository;
        this.logger = prototypeLogger;
    }

    public Profile registerUser(String name, String email, String password, String openInformation, String closedInformation) {
        if (profileRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("User with this email already exists.");
        }

        Profile profile = createProfile(name, email, password, openInformation, closedInformation);
        Profile savedProfile = profileRepository.save(profile);
        logger.info("Profile with email created: {}", savedProfile.getEmail());

        return savedProfile;
    }

    public Profile loginUser(String email, String password) {
        Profile user = profileRepository.findByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    public List<Profile> getProfiles(String keyword) {
        return (keyword == null || keyword.isEmpty()) ? profileRepository.findAll() : profileRepository.findByOpenInformationContaining(keyword);
    }

    public Profile getProfile(Long id) {
        return profileRepository.findById(id).orElse(null);
    }

    public List<Profile> getApprovedProfiles(List<Relation> asAim, List<Relation> asInitiator) {
        return Stream.concat(
                asAim.stream().map(Relation::getInitiator),
                asInitiator.stream().map(Relation::getAim)
        ).collect(Collectors.toList());
    }

    public List<Profile> filterProfilesForUser(List<Profile> profiles, Long loggedInUserId, List<Relation> relations) {
        Set<Long> aimIds = relations.stream()
                .map(relation -> relation.getAim().getId())
                .collect(Collectors.toSet());

        return profiles.stream()
                .filter(profile -> !aimIds.contains(profile.getId()) && !profile.getId().equals(loggedInUserId))
                .collect(Collectors.toList());
    }

    public void addUserIdToCookie(HttpServletResponse response, Profile user) {
        setCookie(response, "userId", String.valueOf(user.getId()), 24 * 60 * 60);
    }

    public void removeUserIdCookie(HttpServletResponse response) {
        setCookie(response, "userId", null, 0);
    }

    public void addLoggedInUserToModel(String userIdCookie, Model model) {
        if (userIdCookie != null) {
            Long loggedInUserId = Long.parseLong(userIdCookie);
            Profile currentUser = getProfile(loggedInUserId);
            model.addAttribute("user", currentUser);
        }
    }

    private Profile createProfile(
            String name,
            String email,
            String password,
            String openInformation,
            String closedInformation
    ) {
        Profile profile = new Profile();
        profile.setName(name);
        profile.setEmail(email);
        profile.setPassword(password);
        profile.setOpenInformation(openInformation);
        profile.setClosedInformation(closedInformation);
        return profile;
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}

