package com.davis.controller;

import com.davis.model.ConfidenceScore;
import com.davis.service.ConfidenceScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/confidence")
@CrossOrigin(origins = "*")
public class ConfidenceScoreController {

    private final ConfidenceScoreService confidenceScoreService;

    public ConfidenceScoreController(
            ConfidenceScoreService confidenceScoreService) {

        this.confidenceScoreService = confidenceScoreService;
    }

    @GetMapping
    public List<ConfidenceScore> getAllConfidenceScores() {
        return confidenceScoreService.getAllConfidenceScores();
    }

    @GetMapping("/{confidenceId}")
    public ResponseEntity<ConfidenceScore> getConfidenceScoreById(
            @PathVariable Long confidenceId) {

        return confidenceScoreService.getConfidenceScoreById(confidenceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConfidenceScore> createConfidenceScore(
            @RequestBody ConfidenceScore confidenceScore) {

        ConfidenceScore savedScore =
                confidenceScoreService.saveConfidenceScore(confidenceScore);

        return ResponseEntity.ok(savedScore);
    }

    @DeleteMapping("/{confidenceId}")
    public ResponseEntity<Void> deleteConfidenceScore(
            @PathVariable Long confidenceId) {

        if (confidenceScoreService
                .getConfidenceScoreById(confidenceId)
                .isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        confidenceScoreService.deleteConfidenceScore(confidenceId);
        return ResponseEntity.noContent().build();
    }
}