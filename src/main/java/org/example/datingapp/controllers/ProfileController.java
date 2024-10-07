package org.example.datingapp.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.models.enums.RelationState;
import org.example.datingapp.services.ProfileService;
import org.example.datingapp.services.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final RelationService relationService;

    @Autowired
    public ProfileController(ProfileService profileService, RelationService relationService) {
        this.profileService = profileService;
        this.relationService = relationService;
    }

    @GetMapping
    public String getProfiles(
            @CookieValue(value = "userId", required = false) String stringUserId,
            @RequestParam(required = false) String keyword,
            Model model) {
        List<Profile> profiles = profileService.getProfiles(keyword);
        addLoggedInUserToModel(stringUserId, model, profiles);
        return "profiles";
    }

    @GetMapping("/{profileId}")
    public String cabinet(
            @CookieValue(value = "userId", required = false) String stringUserId,
            @PathVariable Long profileId,
            Model model) {
        Profile profile = profileService.getProfile(profileId);
        model.addAttribute("profile", profile);

        if (stringUserId != null && profileId.equals(Long.parseLong(stringUserId))) {
            loadUserCabinetData(profileId, model);
        }

        return "profile";
    }

    @PostMapping("/like/{aimId}")
    public String likeProfile(
            @CookieValue(value = "userId", required = false) String stringUserId,
            @PathVariable Long aimId,
            HttpServletRequest request) {
        processRelation(stringUserId, aimId);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/approve/{relationId}")
    public String approveRelation(
            @CookieValue(value = "userId", required = false) String stringUserId,
            @PathVariable Long relationId,
            HttpServletRequest request) {
        updateRelationState(stringUserId, relationId, RelationState.APPROVED);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/reject/{relationId}")
    public String rejectRelation(
            @CookieValue(value = "userId", required = false) String stringUserId,
            @PathVariable Long relationId,
            HttpServletRequest request) {
        updateRelationState(stringUserId, relationId, RelationState.REJECTED);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/delete/{relationId}")
    public String deleteRelation(
            @CookieValue(value = "userId", required = false) String stringUserId,
            @PathVariable Long relationId,
            HttpServletRequest request) {
        if (stringUserId != null) {
            Long loggedInUserId = Long.parseLong(stringUserId);
            Relation relation = relationService.getRelation(relationId);
            if (relation.getInitiator().getId().equals(loggedInUserId)) {
                relationService.deleteRelation(relation);
            }
        }
        return "redirect:" + request.getHeader("Referer");
    }

    private void addLoggedInUserToModel(String stringUserId, Model model, List<Profile> profiles) {
        if (stringUserId != null) {
            Long loggedInUserId = Long.parseLong(stringUserId);
            Profile user = profileService.getProfile(loggedInUserId);
            List<Relation> relations = relationService.getUserRelationsAsInitiator(loggedInUserId);
            profiles = profileService.filterProfilesForUser(profiles, loggedInUserId, relations);
            model.addAttribute("user", user);
            model.addAttribute("profiles", profiles);
        }
    }

    private void loadUserCabinetData(Long loggedInUserId, Model model) {
        List<Relation> relationsAsInitiator = relationService.getUserRelationsAsInitiator(loggedInUserId);
        List<Relation> relationsAsAim = relationService.getUserRelationsAsAim(loggedInUserId);
        List<Relation> approvedAsAim = relationService.getApprovedRelationsAsAim(loggedInUserId);
        List<Relation> approvedAsInitiator = relationService.getApprovedRelationsAsInitiator(loggedInUserId);
        List<Profile> approvedProfiles = profileService.getApprovedProfiles(approvedAsAim, approvedAsInitiator);
        List<Relation> rejectedRelations = relationService.getUserRejectedRelations(loggedInUserId);

        model.addAttribute("relationsAsInitiator", relationService.filterPending(relationsAsInitiator));
        model.addAttribute("relationsAsAim", relationService.filterPending(relationsAsAim));
        model.addAttribute("approvedProfiles", approvedProfiles);
        model.addAttribute("rejectedRelations", rejectedRelations);
    }

    private void processRelation(String stringUserId, Long aimId) {
        if (stringUserId != null) {
            Long loggedInUserId = Long.parseLong(stringUserId);
            Profile initiator = profileService.getProfile(loggedInUserId);
            Profile aim = profileService.getProfile(aimId);

            Relation relation = new Relation();
            relation.setInitiator(initiator);
            relation.setAim(aim);
            relation.setRelationState(RelationState.PENDING);
            relationService.saveRelation(relation);
        }
    }

    private void updateRelationState(String stringUserId, Long relationId, RelationState newState) {
        if (stringUserId != null) {
            Long loggedInUserId = Long.parseLong(stringUserId);
            Relation relation = relationService.getRelation(relationId);
            if (relation.getAim().getId().equals(loggedInUserId)) {
                relation.setRelationState(newState);
                relationService.saveRelation(relation);
            }
        }
    }
}

