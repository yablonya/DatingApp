package org.example.datingapp.models;

import jakarta.persistence.*;
import org.example.datingapp.models.enums.RelationState;

@Entity
public class Relation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "initiator_id")
    private Profile initiator;

    @ManyToOne
    @JoinColumn(name = " aim_id")
    private Profile aim;
    private RelationState relationState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profile getInitiator() {
        return initiator;
    }

    public void setInitiator(Profile initiator) {
        this.initiator = initiator;
    }

    public Profile getAim() {
        return aim;
    }

    public void setAim(Profile aim) {
        this.aim = aim;
    }

    public RelationState getRelationState() {
        return relationState;
    }

    public void setRelationState(RelationState relationState) {
        this.relationState = relationState;
    }
}
