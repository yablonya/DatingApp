package org.example.datingapp.repositories;

import org.example.datingapp.models.Relation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationsRepository extends JpaRepository<Relation, Long> {
    List<Relation> findAllByInitiatorId(Long id);
    List<Relation> findAllByAimId(Long id);
}
