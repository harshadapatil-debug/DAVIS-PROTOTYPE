package com.davis.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@jakarta.persistence.Entity
@Table(name = "evidence")
public class Evidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evidence_id")
    private Long evidenceId;

    @JsonIgnore
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "relationship_id", nullable = false)
private Relationship relationship; 

    @Column(name = "evidence_type", nullable = false, length = 80)
    private String evidenceType;

    @Column(name = "source", nullable = false, length = 255)
    private String source;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "strength", nullable = false, length = 20)
    private String strength;

    @Column(name = "reliability", nullable = false, length = 20)
    private String reliability;

    @Column(name = "direction", nullable = false, length = 20)
    private String direction;

    @Column(name = "independence_group", nullable = false, length = 100)
    private String independenceGroup;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt;

    public Evidence() {
    }

    public Long getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(Long evidenceId) {
        this.evidenceId = evidenceId;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getReliability() {
        return reliability;
    }

    public void setReliability(String reliability) {
        this.reliability = reliability;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getIndependenceGroup() {
        return independenceGroup;
    }

    public void setIndependenceGroup(String independenceGroup) {
        this.independenceGroup = independenceGroup;
    }

    public LocalDateTime getObservedAt() {
        return observedAt;
    }

    public void setObservedAt(LocalDateTime observedAt) {
        this.observedAt = observedAt;
    }
}