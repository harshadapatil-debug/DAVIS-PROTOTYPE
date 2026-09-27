package com.davis.controller;

import com.davis.model.Relationship;
import com.davis.service.RelationshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relationships")
@CrossOrigin(origins = "*")
public class RelationshipController {

    private final RelationshipService relationshipService;

    public RelationshipController(RelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }

    @GetMapping
    public List<Relationship> getAllRelationships() {
        return relationshipService.getAllRelationships();
    }

    @GetMapping("/{relationshipId}")
    public ResponseEntity<Relationship> getRelationshipById(
            @PathVariable Long relationshipId) {

        return relationshipService.getRelationshipById(relationshipId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Relationship> createRelationship(
            @RequestBody Relationship relationship) {

        Relationship savedRelationship =
                relationshipService.saveRelationship(relationship);

        return ResponseEntity.ok(savedRelationship);
    }

    @DeleteMapping("/{relationshipId}")
    public ResponseEntity<Void> deleteRelationship(
            @PathVariable Long relationshipId) {

        if (relationshipService.getRelationshipById(relationshipId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        relationshipService.deleteRelationship(relationshipId);
        return ResponseEntity.noContent().build();
    }
}