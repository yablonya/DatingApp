package org.example.datingapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.datingapp.models.Relation;
import org.example.datingapp.services.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@Tag(name = "Relations Controller", description = "Operations for working with relations")
@RequestMapping("/api/relations")
public class RelationController {
    private final RelationService relationService;

    @Autowired
    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    @PostMapping("/like/{aimId}")
    @Operation(
            summary = "Like a profile",
            description = "Creates a relation where the logged-in user likes another profile.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the logged-in user stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            name = "aimId",
                            description = "ID of the profile being liked",
                            required = true,
                            schema = @Schema(type = "integer", example = "123")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Relation created successfully"),
                    @ApiResponse(responseCode = "200", description = "Relation already exists"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<Void> likeProfile(
            @CookieValue(value = "profileId", required = false) String profileIdCookie,
            @PathVariable Long aimId
    ) {
        if (profileIdCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Long profileId = Long.parseLong(profileIdCookie);
            boolean isRelationNew = relationService.saveRelation(profileId, aimId);

            return isRelationNew
                    ? ResponseEntity.status(HttpStatus.CREATED).build()
                    : ResponseEntity.status(HttpStatus.OK).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/approve/{initiatorId}")
    @Operation(
            summary = "Approve a relation",
            description = "Approves a relation initiated by another user.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the logged-in user stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            name = "initiatorId",
                            description = "ID of the profile that initiated the relation",
                            required = true,
                            schema = @Schema(type = "integer", example = "123")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Relation approved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "404", description = "Relation not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<Void> approveRelation(
            @CookieValue(value = "profileId", required = false) String profileIdCookie,
            @PathVariable Long initiatorId
    ) {
        if (profileIdCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Long aimId = Long.parseLong(profileIdCookie);
            relationService.approveRelation(aimId, initiatorId);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reject/{initiatorId}")
    @Operation(
            summary = "Reject a relation",
            description = "Rejects a relation initiated by another user.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the logged-in user stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            name = "initiatorId",
                            description = "ID of the profile that initiated the relation",
                            required = true,
                            schema = @Schema(type = "integer", example = "123")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Relation rejected successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "404", description = "Relation not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<Void> rejectRelation(
            @CookieValue(value = "profileId", required = false) String profileIdCookie,
            @PathVariable Long initiatorId
    ) {
        if (profileIdCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Long aimId = Long.parseLong(profileIdCookie);
            relationService.rejectRelation(aimId, initiatorId);

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{relationId}")
    @Operation(
            summary = "Delete a relation",
            description = "Deletes a relation by its ID.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the logged-in user stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            name = "relationId",
                            description = "ID of the relation to be deleted",
                            required = true,
                            schema = @Schema(type = "integer", example = "123")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Relation deleted successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "404", description = "Relation not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<Void> deleteRelation(
            @CookieValue(value = "profileId", required = false) String profileIdCookie,
            @PathVariable Long relationId
    ) {
        if (profileIdCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Long profileId = Long.parseLong(profileIdCookie);
            relationService.deleteRelation(profileId, relationId);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get all relations",
            description = "Retrieves all relations for the logged-in user.",
            parameters = {
                    @Parameter(
                            name = "profileId",
                            description = "ID of the logged-in user stored in a cookie",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Relations retrieved successfully"),
                    @ApiResponse(responseCode = "204", description = "No relations found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - no profileId cookie found"),
                    @ApiResponse(responseCode = "400", description = "Invalid profileId format"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public ResponseEntity<List<Relation>> getRelations(
            @CookieValue(value = "profileId", required = false) String profileIdCookie
    ) {
        if (profileIdCookie == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Long profileId = Long.parseLong(profileIdCookie);
            List<Relation> relations = relationService.getAllProfileRelations(profileId);
            if (relations.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            return ResponseEntity.ok(relations);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
