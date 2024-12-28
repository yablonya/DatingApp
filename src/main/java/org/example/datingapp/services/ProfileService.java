package org.example.datingapp.services;

import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.models.enums.RelationState;
import org.example.datingapp.repositories.ProfileDAO;
import org.example.datingapp.repositories.ProfileRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    private final ProfileDAO profileDAO;
    private final Logger logger;

    @Autowired
    public ProfileService(ProfileRepository profileRepository,ProfileDAO profileDAO, Logger prototypeLogger) {
        this.profileRepository = profileRepository;
        this.profileDAO = profileDAO;
        this.logger = prototypeLogger;
    }

    public Profile registerProfile(String name, String email, String password, String openInfo, String closedInfo) {
        return getProfile(name, email, password, openInfo, closedInfo, profileRepository.findByEmail(email));
    }

    public Long registerProfileAndGetId(String name, String email, String password, String openInfo, String closedInfo) {
        return getProfileDAO(name, email, password, openInfo, closedInfo);
    }

    private Long getProfileDAO(String name, String email, String password, String openInfo, String closedInfo){
        Profile profile = createProfile(name, email, password, openInfo, closedInfo);
        long id = profileDAO.save(profile);
        logger.info("Profile created successfully with id: {}", id);

        return id;
    }
    private Profile getProfile(String name, String email, String password, String openInfo, String closedInfo, Profile byEmail) {
        if (byEmail != null) {
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

    public Profile updateProfileDAO(Long profileId, Map<String, String> updatedDetails, String cookie) {
        logger.info("Test message ID {}", cookie);
        Profile profile = profileDAO.findById(profileId);
        if (profile == null) {
            logger.warn("DAO Attempt to update non-existent profile with ID: {}", profileId);
        }
        updatedDetails.forEach((key, value) -> {
            switch (key) {
                case "email":
                    Profile existingProfile = profileDAO.findByEmail(value);

                    if (existingProfile != null && !existingProfile.getId().equals(profileId)) {
                        logger.warn("Attempt to update profile ID: {} with existing email: {}", profileId, value);
                        throw new IllegalArgumentException("Profile with this email already exists.");
                    }
                    logger.info("Test message email");
                    profile.setEmail(value);
                    break;
                case "name":
                    logger.info("Test message3");
                    profile.setName(value);
                    break;
                case "password":
                    logger.info("Test message4");
                    profile.setPassword(value);
                    break;
                case "open_info":
                    logger.info("Test message5");
                    profile.setOpenInfo(value);
                    break;
                case "closed_info":
                    logger.info("Test message6");
                    profile.setClosedInfo(value);
                    break;
                default:
                    logger.warn("Unknown field: {} in update request", key);
                    break;
            }
        });
        logger.info("Test message");
        Profile updatedProfile = profileDAO.update(profile);
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
        logger.info("Logging in user with email: {}", email);
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

    public Profile loginProfileDAO(String email, String password) {
        logger.info("Logging in user with email: {}", email);
        Profile profile = profileDAO.findByEmail(email);

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

    public List<Profile> getAllWithPaginationAndKeywordDAO(int offset, int limit, String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            List<Profile> allProfiles = profileDAO.findAll();

            return allProfiles.size() - 1 >= limit
                    ? allProfiles.subList(offset, limit)
                    : allProfiles.subList(offset, allProfiles.size());
        }

        List<Profile> allProfilesWithKeyword = profileDAO.findByOpenInfoContainingIgnoreCase(keyword);

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

    public Profile getProfileById(Long id) {
        Profile profile = profileDAO.findById(id);

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

