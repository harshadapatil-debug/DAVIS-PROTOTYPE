package com.davis.controller;

import com.davis.model.Evidence;
import com.davis.service.EvidenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evidence")
@CrossOrigin(origins = "*")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @GetMapping
    public List<Evidence> getAllEvidence() {
        return evidenceService.getAllEvidence();
    }

    @GetMapping("/{evidenceId}")
    public ResponseEntity<Evidence> getEvidenceById(
            @PathVariable Long evidenceId) {

        return evidenceService.getEvidenceById(evidenceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Evidence> createEvidence(
            @RequestBody Evidence evidence) {

        Evidence savedEvidence = evidenceService.saveEvidence(evidence);
        return ResponseEntity.ok(savedEvidence);
    }

    @DeleteMapping("/{evidenceId}")
    public ResponseEntity<Void> deleteEvidence(
            @PathVariable Long evidenceId) {

        if (evidenceService.getEvidenceById(evidenceId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        evidenceService.deleteEvidence(evidenceId);
        return ResponseEntity.noContent().build();
    }
}