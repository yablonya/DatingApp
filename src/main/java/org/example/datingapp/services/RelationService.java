package org.example.datingapp.services;

import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.example.datingapp.models.enums.RelationState;
import org.example.datingapp.repositories.ProfileDAO;
import org.example.datingapp.repositories.RelationsDAO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RelationService {
    private final Logger logger;
    private final ProfileDAO profileDAO;
    private final RelationsDAO relationsDAO;

    @Autowired
    public RelationService(
            Logger prototypeLogger,
            ProfileDAO profileDAO, RelationsDAO relationsDAO) {
        this.logger = prototypeLogger;
        this.profileDAO = profileDAO;
        this.relationsDAO = relationsDAO;
    }

    public boolean saveRelationDAO(Long initiatorId, Long aimId) {
        Profile initiator = profileDAO.findById(initiatorId);
        if (initiator == null){
                throw new NoSuchElementException("Initiator profile not found.");
        }
        Profile aim = profileDAO.findById(aimId);
        if (aim == null){
                throw new NoSuchElementException("Aim profile not found.");
        }
        if (relationsDAO.existsByInitiatorAndAim(initiatorId, aimId)) {
            throw new IllegalArgumentException("Relation already exists.");
        } else if(relationsDAO.existsByInitiatorAndAim(aimId, initiatorId)) {
            Relation relation = relationsDAO.findRelationByInitiatorAndAim(aimId, initiatorId);

            relation.setRelationState(RelationState.APPROVED);
            relationsDAO.save(relation);

            return false;
        } else {
            Relation relation = new Relation();

            relation.setInitiator(initiator);
            relation.setAim(aim);
            relation.setRelationState(RelationState.PENDING);
            logger.info("Saved new relation between initiator ID: {} and aim ID: {}", initiatorId, aimId);
            logger.info("{}", (relation.getRelationState().ordinal()));
            long relationID = relationsDAO.save(relation);

            logger.info("Saved new relation with ID: {} between initiator ID: {} and aim ID: {}",relationID, initiatorId, aimId);
            return true;
        }
    }

    public void approveRelationDAO(Long aimId, Long initiatorId) {
        Relation relation = getRelationByAimAndInitiatorDAO(aimId, initiatorId);

        if(relation.getRelationState().equals(RelationState.PENDING)) {
            relation.setRelationState(RelationState.APPROVED);
            relationsDAO.update(relation);
        } else {
            throw new NoSuchElementException("Such pending relation was not found.");
        }
    }

    public void rejectRelationDAO(Long aimId, Long initiatorId) {
        Relation relation = getRelationByAimAndInitiatorDAO(aimId, initiatorId);
        logger.info("{}", relation.getRelationState().equals(RelationState.PENDING));
        if(relation.getRelationState().equals(RelationState.PENDING)) {
            relation.setRelationState(RelationState.REJECTED);
            relationsDAO.update(relation);
            logger.info("Rejected relation with ID: {}", relation.getId());
        } else {
            throw new NoSuchElementException("Such pending relation was not found.");
        }
    }

    public void deleteRelationDAO(Long initiatorId, Long relationId) {
        if (profileDAO.findById(initiatorId) == null) {
            throw new NoSuchElementException("Initiator profile not found.");
        }

        Relation relation = getRelationByIdDAO(relationId);

        if (relation.getRelationState().equals(RelationState.REJECTED)) {
            relationsDAO.delete(relation);
            logger.info("Deleted relation with ID: {}", relationId);
        } else {
            throw new NoSuchElementException("Such rejected relation was not found.");
        }
    }
    public List<Relation> getAllProfileRelations(Long profileId) {
        List<Relation> allProfileRelationsAsAim = relationsDAO.findAllByAimId(profileId);
        List<Relation> allProfileRelationsAsInitiator = relationsDAO.findAllByInitiatorId(profileId);

        List<Relation> combinedRelations = new ArrayList<>();
        combinedRelations.addAll(allProfileRelationsAsAim);
        combinedRelations.addAll(allProfileRelationsAsInitiator);

        logger.info("Retrieved {} relations for profile ID: {}", combinedRelations.size(), profileId);

        return combinedRelations;
    }

    public List<Relation> getAllRejectedRelations(Long profileId) {
        List<Relation> allProfileRelationsAsAim = relationsDAO.findAllByAimId(profileId);
        List<Relation> allProfileRelationsAsInitiator = relationsDAO.findAllByInitiatorId(profileId);

        List<Relation> combinedRelations = new ArrayList<>();
        combinedRelations.addAll(allProfileRelationsAsAim);
        combinedRelations.addAll(allProfileRelationsAsInitiator);

        logger.info("Retrieved {} relations for profile ID: {}", combinedRelations.size(), profileId);

        return combinedRelations;
    }

    public List<Relation> getAllProfileRelationsDAO(Long profileId) {
        List<Relation> allProfileRelationsAsAim = relationsDAO.findAllByAimId(profileId);
        List<Relation> allProfileRelationsAsInitiator = relationsDAO.findAllByInitiatorId(profileId);

        List<Relation> combinedRelations = new ArrayList<>();
        combinedRelations.addAll(allProfileRelationsAsAim);
        combinedRelations.addAll(allProfileRelationsAsInitiator);

        logger.info("Retrieved {} relations for profile ID: {}", combinedRelations.size(), profileId);

        return combinedRelations;
    }

    public Relation getRelationByIdDAO(Long relationId) {
        Relation relation = relationsDAO.findById(relationId);
        if (relation == null) throw new NoSuchElementException("Relation not found.");
        return relation;
    }

    public Relation getRelationByAimAndInitiatorDAO(Long aimId, Long initiatorId) {

        Profile aim = profileDAO.findById(aimId);
        if (aim == null) throw new NoSuchElementException("Aim profile not found.");

        Profile initiator = profileDAO.findById(initiatorId);
        if(initiator == null) throw new NoSuchElementException("Initiator profile not found.");

        Relation relation = relationsDAO.findRelationByInitiatorAndAim(initiatorId, aimId);
        if (relation == null) throw new NoSuchElementException("Relation not found.");

        return relation;
    }
}

