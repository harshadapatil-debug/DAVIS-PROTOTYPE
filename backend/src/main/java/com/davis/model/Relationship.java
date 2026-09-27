package com.davis.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@jakarta.persistence.Entity
@Table(name = "relationships")
public class Relationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private Long relationshipId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private Case caseEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_entity_id", nullable = false)
    private Entity sourceEntity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_entity_id", nullable = false)
    private Entity targetEntity;

    @Column(name = "relationship_type", nullable = false, length = 80)
    private String relationshipType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "assessment", nullable = false, length = 20)
    private String assessment = "INFERRED";

    public Relationship() {
    }

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    public Case getCaseEntity() {
        return caseEntity;
    }

    public void setCaseEntity(Case caseEntity) {
        this.caseEntity = caseEntity;
    }

    public Entity getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(Entity sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssessment() {
        return assessment;
    }

    public void setAssessment(String assessment) {
        this.assessment = assessment;
    }
}