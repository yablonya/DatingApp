package org.example.datingapp.controllers;

import org.example.datingapp.models.Relation;
import org.example.datingapp.services.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/relations")
public class RelationController {
    private final RelationService relationService;

    @Autowired
    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    @PostMapping("/like/{aimId}")
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
