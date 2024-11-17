package org.example.datingapp.repositories;

import org.example.datingapp.models.Profile;
import org.example.datingapp.models.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationsRepository extends JpaRepository<Relation, Long> {
    List<Relation> findAllByInitiatorId(Long id);

    List<Relation> findAllByAimId(Long id);

    Relation findRelationByInitiatorAndAim(Profile initiator, Profile aim);

    Relation findRelationByAimAndInitiator(Profile aim, Profile initiator);

    boolean existsByInitiatorAndAim(Profile initiator, Profile aim);
}
