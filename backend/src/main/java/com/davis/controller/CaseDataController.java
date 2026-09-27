package com.davis.controller;

import com.davis.model.ConfidenceScore;
import com.davis.model.Entity;
import com.davis.model.Evidence;
import com.davis.model.Relationship;
import com.davis.service.ConfidenceScoreService;
import com.davis.service.EntityService;
import com.davis.service.EvidenceService;
import com.davis.service.RelationshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseDataController {

    private final EntityService entityService;
    private final RelationshipService relationshipService;
    private final EvidenceService evidenceService;
    private final ConfidenceScoreService confidenceScoreService;

    public CaseDataController(
            EntityService entityService,
            RelationshipService relationshipService,
            EvidenceService evidenceService,
            ConfidenceScoreService confidenceScoreService) {

        this.entityService = entityService;
        this.relationshipService = relationshipService;
        this.evidenceService = evidenceService;
        this.confidenceScoreService = confidenceScoreService;
    }

    @GetMapping("/{caseId}/entities")
    public ResponseEntity<List<Entity>> getCaseEntities(
            @PathVariable Long caseId) {

        return ResponseEntity.ok(
                entityService.getEntitiesByCaseId(caseId)
        );
    }

    @GetMapping("/{caseId}/relationships")
    public ResponseEntity<List<Relationship>> getCaseRelationships(
            @PathVariable Long caseId) {

        return ResponseEntity.ok(
                relationshipService.getRelationshipsByCaseId(caseId)
        );
    }

    @GetMapping("/{caseId}/evidence")
    public ResponseEntity<List<Evidence>> getCaseEvidence(
            @PathVariable Long caseId) {

        return ResponseEntity.ok(
                evidenceService.getEvidenceByCaseId(caseId)
        );
    }

    @GetMapping("/{caseId}/confidence")
    public ResponseEntity<ConfidenceScore> getCaseConfidence(
            @PathVariable Long caseId) {

        return confidenceScoreService
                .getConfidenceScoreByCaseId(caseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}