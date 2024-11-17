package org.example.datingapp.services;

import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.models.enums.RelationState;
import org.example.datingapp.repositories.ProfileRepository;
import org.example.datingapp.repositories.RelationsRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RelationService {
    private final RelationsRepository relationsRepository;
    private final ProfileRepository profileRepository;
    private final Logger logger;

    @Autowired
    public RelationService(
            RelationsRepository relationsRepository,
            ProfileRepository profileRepository,
            Logger prototypeLogger
    ) {
        this.relationsRepository = relationsRepository;
        this.profileRepository = profileRepository;
        this.logger = prototypeLogger;
    }

    public boolean saveRelation(Long initiatorId, Long aimId) {
        Profile initiator = profileRepository.findById(initiatorId)
                .orElseThrow(() -> new NoSuchElementException("Initiator profile not found."));
        Profile aim = profileRepository.findById(aimId)
                .orElseThrow(() -> new NoSuchElementException("Aim profile not found."));

        if (relationsRepository.existsByInitiatorAndAim(initiator, aim)) {
            throw new IllegalArgumentException("Relation already exists.");
        } else if(relationsRepository.existsByInitiatorAndAim(aim, initiator)) {
            Relation relation = relationsRepository.findRelationByInitiatorAndAim(aim, initiator);

            relation.setRelationState(RelationState.APPROVED);
            relationsRepository.save(relation);

            return false;
        } else {
            Relation relation = new Relation();

            relation.setInitiator(initiator);
            relation.setAim(aim);
            relation.setRelationState(RelationState.PENDING);
            relationsRepository.save(relation);

            logger.info("Saved new relation between initiator ID: {} and aim ID: {}", initiatorId, aimId);
            return true;
        }
    }

    public void approveRelation(Long aimId, Long initiatorId) {
        Relation relation = getRelationByAimAndInitiator(aimId, initiatorId);

        if(relation.getRelationState().equals(RelationState.PENDING)) {
            relation.setRelationState(RelationState.APPROVED);
            relationsRepository.save(relation);
            logger.info("Approved relation with ID: {}", relation.getId());
        } else {
            throw new NoSuchElementException("Such pending relation was not found.");
        }
    }

    public void rejectRelation(Long aimId, Long initiatorId) {
        Relation relation = getRelationByAimAndInitiator(aimId, initiatorId);

        if(relation.getRelationState().equals(RelationState.PENDING)) {
            relation.setRelationState(RelationState.REJECTED);
            relationsRepository.save(relation);
            logger.info("Rejected relation with ID: {}", relation.getId());
        } else {
            throw new NoSuchElementException("Such pending relation was not found.");
        }
    }

    public void deleteRelation(Long initiatorId, Long relationId) {
        if (!profileRepository.existsById(initiatorId)) {
            throw new NoSuchElementException("Initiator profile not found.");
        }

        Relation relation = getRelationById(relationId);

        if (relation.getRelationState().equals(RelationState.REJECTED)) {
            relationsRepository.delete(relation);
            logger.info("Deleted relation with ID: {}", relationId);
        } else {
            throw new NoSuchElementException("Such rejected relation was not found.");
        }
    }

    public List<Relation> getAllProfileRelations(Long profileId) {
        List<Relation> allProfileRelationsAsAim = relationsRepository.findAllByAimId(profileId);
        List<Relation> allProfileRelationsAsInitiator = relationsRepository.findAllByInitiatorId(profileId);

        List<Relation> combinedRelations = new ArrayList<>();
        combinedRelations.addAll(allProfileRelationsAsAim);
        combinedRelations.addAll(allProfileRelationsAsInitiator);

        logger.info("Retrieved {} relations for profile ID: {}", combinedRelations.size(), profileId);

        return combinedRelations;
    }

    public Relation getRelationById(Long relationId) {
        return relationsRepository.findById(relationId)
                .orElseThrow(() -> new NoSuchElementException("Relation not found."));
    }

    public Relation getRelationByAimAndInitiator(Long aimId, Long initiatorId) {
        Profile aim = profileRepository.findById(aimId)
                .orElseThrow(() -> new NoSuchElementException("Aim profile not found."));
        Profile initiator = profileRepository.findById(initiatorId)
                .orElseThrow(() -> new NoSuchElementException("Initiator profile not found."));

        Relation relation = relationsRepository.findRelationByAimAndInitiator(aim, initiator);

        if (relation == null) {
            throw new NoSuchElementException("Relation not found.");
        }

        return relation;
    }
}

