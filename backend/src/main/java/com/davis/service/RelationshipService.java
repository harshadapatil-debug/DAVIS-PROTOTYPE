package com.davis.service;

import com.davis.model.Relationship;
import com.davis.repository.RelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    public RelationshipService(RelationshipRepository relationshipRepository) {
        this.relationshipRepository = relationshipRepository;
    }

    public List<Relationship> getAllRelationships() {
        return relationshipRepository.findAll();
    }

    public Optional<Relationship> getRelationshipById(
            Long relationshipId) {

        return relationshipRepository.findById(relationshipId);
    }

    public List<Relationship> getRelationshipsByCaseId(Long caseId) {
        return relationshipRepository.findRelationshipsByCaseId(caseId);
    }

    public Relationship saveRelationship(Relationship relationship) {
        return relationshipRepository.save(relationship);
    }

    public void deleteRelationship(Long relationshipId) {
        relationshipRepository.deleteById(relationshipId);
    }
}