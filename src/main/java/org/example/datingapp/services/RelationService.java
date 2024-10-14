package org.example.datingapp.services;

import org.example.datingapp.models.Relation;
import org.example.datingapp.models.enums.RelationState;
import org.example.datingapp.repositories.RelationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RelationService {
    @Autowired
    private RelationsRepository relationsRepository;

    public void saveRelation(Relation relation) {
        relationsRepository.save(relation);
    }

    public Relation getRelation(Long id) {
        return relationsRepository.findById(id).orElse(null);
    }

    public void deleteRelation(Relation relation) {
        relationsRepository.delete(relation);
    }

    public List<Relation> getUserRelationsAsInitiator(Long id) {
        return relationsRepository.findAllByInitiatorId(id);
    }

    public List<Relation> getUserRelationsAsAim(Long id) {
        return relationsRepository.findAllByAimId(id);
    }

    public List<Relation> getApprovedRelationsAsAim(Long id) {
        return filterRelationsByState(getUserRelationsAsAim(id), RelationState.APPROVED);
    }

    public List<Relation> getApprovedRelationsAsInitiator(Long id) {
        return filterRelationsByState(getUserRelationsAsInitiator(id), RelationState.APPROVED);
    }

    public List<Relation> getUserRejectedRelations(Long id) {
        return filterRelationsByState(getUserRelationsAsInitiator(id), RelationState.REJECTED);
    }

    public List<Relation> filterPending(List<Relation> relations) {
        return filterRelationsByState(relations, RelationState.PENDING);
    }

    private List<Relation> filterRelationsByState(List<Relation> relations, RelationState state) {
        return relations.stream()
                .filter(relation -> relation.getRelationState() == state)
                .collect(Collectors.toList());
    }
}

