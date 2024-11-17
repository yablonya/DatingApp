package org.example.datingapp.services;

import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.models.enums.RelationState;
import org.example.datingapp.repositories.ProfileRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final Logger logger;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, Logger prototypeLogger) {
        this.profileRepository = profileRepository;
        this.logger = prototypeLogger;
    }

    public Profile registerProfile(String name, String email, String password, String openInfo, String closedInfo) {
        if (profileRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Profile with this email already exists.");
        }

        Profile profile = createProfile(name, email, password, openInfo, closedInfo);
        Profile savedProfile = profileRepository.save(profile);
        logger.info("Profile created successfully with email: {}", savedProfile.getEmail());

        return savedProfile;
    }

    public Profile updateProfile(Long profileId, Map<String, String> updatedDetails) {
        Profile profile = profileRepository.findById(profileId).orElseThrow(() -> {
            logger.warn("Attempt to update non-existent profile with ID: {}", profileId);

            return new IllegalArgumentException("Profile not found.");
        });

        updatedDetails.forEach((key, value) -> {
            switch (key) {
                case "email":
                    Profile existingProfile = profileRepository.findByEmail(value);

                    if (existingProfile != null && !existingProfile.getId().equals(profileId)) {
                        logger.warn("Attempt to update profile ID: {} with existing email: {}", profileId, value);
                        throw new IllegalArgumentException("Profile with this email already exists.");
                    }
                    profile.setEmail(value);
                    break;
                case "name":
                    profile.setName(value);
                    break;
                case "password":
                    profile.setPassword(value);
                    break;
                case "openInformation":
                    profile.setOpenInfo(value);
                    break;
                case "closedInformation":
                    profile.setClosedInfo(value);
                    break;
                default:
                    logger.warn("Unknown field: {} in update request", key);
            }
        });

        Profile updatedProfile = profileRepository.save(profile);
        logger.info("Profile with ID: {} updated successfully", profileId);

        return updatedProfile;
    }

    public void deleteProfile(Long profileId) {
        Profile profile = profileRepository.findById(profileId).orElse(null);

        if (profile == null) {
            throw new IllegalArgumentException("Profile not found.");
        }

        profileRepository.delete(profile);
        logger.info("Profile with ID: {} deleted successfully", profileId);
    }

    public Profile loginProfile(String email, String password) {
        Profile profile = profileRepository.findByEmail(email);

        if (profile == null) {
            throw new IllegalArgumentException("Profile not found.");
        }

        if (!profile.getPassword().equals(password)) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        logger.info("User logged in successfully with email: {}", email);
        return profile;
    }

    public List<Profile> getAllWithPaginationAndKeyword(int offset, int limit, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            List<Profile> allProfiles = profileRepository.findAll();

            return allProfiles.size() - 1 >= limit
                    ? allProfiles.subList(offset, limit)
                    : allProfiles.subList(offset, allProfiles.size());
        }

        List<Profile> allProfilesWithKeyword = profileRepository.findByOpenInfoContainingIgnoreCase(keyword);

        return allProfilesWithKeyword.size() - 1 >= limit
                ? allProfilesWithKeyword.subList(offset, limit)
                : allProfilesWithKeyword.subList(offset, allProfilesWithKeyword.size());
    }

    public Profile getProfile(Long id) {
        Profile profile = profileRepository.findById(id).orElse(null);

        if (profile != null) {
            logger.info("Retrieved profile with ID: {}", id);
        } else {
            logger.warn("Profile with ID: {} not found", id);
        }

        return profile;
    }

    public List<Profile> getAllApprovedProfiles(Long profileId, List<Relation> profileRelations) {
        List<Profile> approvedProfiles = new ArrayList<>();

        for (Relation relation : profileRelations) {
            if (RelationState.APPROVED.equals(relation.getRelationState())) {
                if (relation.getInitiator().getId().equals(profileId)) {
                    approvedProfiles.add(relation.getAim());
                } else if (relation.getAim().getId().equals(profileId)) {
                    approvedProfiles.add(relation.getInitiator());
                }
            }
        }

        logger.info("Retrieved {} approved profiles for profile ID: {}", approvedProfiles.size(), profileId);

        return approvedProfiles;
    }

    private Profile createProfile(
            String name,
            String email,
            String password,
            String openInfo,
            String closedInfo
    ) {
        Profile profile = new Profile();
        profile.setName(name);
        profile.setEmail(email);
        profile.setPassword(password);
        profile.setOpenInfo(openInfo);
        profile.setClosedInfo(closedInfo);
        return profile;
    }
}

