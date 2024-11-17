package org.example.datingapp.controllers;

import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.services.ProfileService;
import org.example.datingapp.services.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final RelationService relationService;

    @Autowired
    public ProfileController(ProfileService profileService, RelationService relationService) {
        this.profileService = profileService;
        this.relationService = relationService;
    }

    @PostMapping("/register")
    public ResponseEntity<Profile> registerProfile(@RequestBody Map<String, String> profileDetails) {
        try {
            Profile registeredProfile = profileService.registerProfile(
                    profileDetails.get("name"),
                    profileDetails.get("email"),
                    profileDetails.get("password"),
                    profileDetails.get("openInfo"),
                    profileDetails.get("closedInfo")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add(
                    "Set-Cookie",
                    "profileId=" + registeredProfile.getId() + "; Path=/; HttpOnly; Max-Age=86400"
            );

            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(registeredProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Profile> updateProfile(
            @CookieValue(value = "profileId", required = false) String profileIdCookie,
            @RequestBody Map<String, String> profileDetails
    ) {
        try {
            if (profileIdCookie == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long profileId = Long.parseLong(profileIdCookie);
            Profile updatedProfile = profileService.updateProfile(profileId, profileDetails);

            HttpHeaders headers = new HttpHeaders();
            headers.add(
                    "Set-Cookie",
                    "profileId=" + updatedProfile.getId() + "; Path=/; HttpOnly; Max-Age=86400"
            );

            return ResponseEntity.ok().headers(headers).body(updatedProfile);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> delete(
            @CookieValue(value = "profileId", required = false) String profileIdCookie,
            @RequestBody Map<String, String> credentials
    ) {
        try {
            if (profileIdCookie == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long profileId = Long.parseLong(profileIdCookie);
            Profile profile = profileService.getProfile(profileId);

            if (profile != null && profile.getPassword().equals(credentials.get("password"))) {
                profileService.deleteProfile(profileId);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Set-Cookie", "profileId=; Path=/; Max-Age=0");

                return ResponseEntity.noContent().headers(headers).build();
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Profile> loginProfile(@RequestBody Map<String, String> credentials) {
        try {
            Profile profile = profileService.loginProfile(
                    credentials.get("email"),
                    credentials.get("password")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add(
                    "Set-Cookie",
                    "profileId=" + profile.getId() + "; Path=/; HttpOnly; Max-Age=86400"
            );

            return ResponseEntity.ok().headers(headers).body(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "profileId", required = false) String profileIdCookie) {
        try {
            if (profileIdCookie != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Set-Cookie", "profileId=; Path=/; Max-Age=0");

                return ResponseEntity.noContent().headers(headers).build();
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Profile>> getProfiles(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        try {
            List<Profile> profiles = profileService.getAllWithPaginationAndKeyword(page * size, size, keyword);
            if (profiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            return ResponseEntity.ok(profiles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all/approved")
    public ResponseEntity<List<Profile>> getAllApprovedProfiles(
            @CookieValue(value = "profileId", required = false) String profileIdCookie
    ) {
        try {
            if (profileIdCookie == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long profileId = Long.parseLong(profileIdCookie);
            List<Relation> profileRelations = relationService.getAllProfileRelations(profileId);
            List<Profile> approvedProfiles = profileService.getAllApprovedProfiles(profileId, profileRelations);

            if (approvedProfiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }

            return ResponseEntity.ok(approvedProfiles);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<Profile> getProfile(@PathVariable Long profileId) {
        try {
            Profile profile = profileService.getProfile(profileId);

            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(profile);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

