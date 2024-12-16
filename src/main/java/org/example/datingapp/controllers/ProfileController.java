package org.example.datingapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Profiles Controller", description = "Operations for working with profiles")
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
    @Operation(
            summary = "Register a new profile",
            description = "Registers a new profile with provided details and returns the created profile.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details for registering a new profile",
                    required = true,
                    content = @Content(schema = @Schema(
                            example = "{ \"name\": \"John Doe\", \"email\": \"john.doe@example.com\", \"password\": \"securePassword\", \"openInfo\": \"Some info\", \"closedInfo\": \"Sensitive info\" }"
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Profile successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid profile details provided"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/update")
    @Operation(
            summary = "Update profile",
            description = "Updates the profile of the logged-in user based on provided details.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the profile stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details to update the profile",
                    required = true,
                    content = @Content(schema = @Schema(
                            example = "{ \"name\": \"Updated Name\", \"email\": \"updated.email@example.com\", \"openInfo\": \"Updated open info\", \"closedInfo\": \"Updated closed info\" }"
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile successfully updated"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "409", description = "Conflict - profile could not be updated"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "Delete profile",
            description = "Deletes the profile of the logged-in user if the correct password is provided.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the profile stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password for deleting the profile",
                    required = true,
                    content = @Content(schema = @Schema(
                            example = "{ \"password\": \"securePassword\" }"
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Profile successfully deleted"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - incorrect password"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
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
    @Operation(
            summary = "Log in",
            description = "Logs in a user with email and password, returning their profile and setting a cookie.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(
                            example = "{ \"email\": \"john.doe@example.com\", \"password\": \"securePassword\" }"
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid email or password"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Log out",
            description = "Logs out the user by clearing the profileId cookie.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the profile stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Logout successful"),
                    @ApiResponse(responseCode = "404", description = "No active session found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<Void> logout(@CookieValue(value = "profileId") String profileIdCookie) {
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
    @Operation(
            summary = "Get all profiles",
            description = "Retrieves a paginated list of all profiles with an optional keyword filter.",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Page number for pagination (0-based index)",
                            example = "0",
                            schema = @Schema(type = "integer", defaultValue = "0")
                    ),
                    @Parameter(
                            name = "size",
                            description = "Number of profiles per page",
                            example = "10",
                            schema = @Schema(type = "integer", defaultValue = "10")
                    ),
                    @Parameter(
                            name = "keyword",
                            description = "Optional keyword to filter profiles",
                            example = "John",
                            schema = @Schema(type = "string")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "No profiles found"),
                    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
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
    @Operation(
            summary = "Get all approved profiles",
            description = "Retrieves a list of approved profiles based on relations of the logged-in user.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the profile stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Approved profiles retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "No approved profiles found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<List<Profile>> getAllApprovedProfiles(
            @CookieValue(value = "profileId") String profileIdCookie
    ) {
        try {
            if (profileIdCookie == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Long profileId = Long.parseLong(profileIdCookie);
            List<Relation> profileRelations = relationService.getAllProfileRelations(profileId);
            List<Profile> approvedProfiles = profileService.getAllApprovedProfiles(profileId, profileRelations);

            if (approvedProfiles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            return ResponseEntity.ok(approvedProfiles);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{profileId}")
    @Operation(
            summary = "Get profile by ID",
            description = "Retrieves a profile by its unique ID.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "Unique identifier of the profile",
                            example = "123",
                            required = true,
                            schema = @Schema(type = "integer")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Profile not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<Profile> getProfile(@PathVariable Long profileId) {
        try {
            Profile profile = profileService.getProfile(profileId);

            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(profile);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

