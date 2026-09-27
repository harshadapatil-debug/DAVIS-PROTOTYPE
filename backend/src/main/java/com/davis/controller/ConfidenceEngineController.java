package com.davis.controller;

import com.davis.model.ConfidenceScore;
import com.davis.service.ConfidenceEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class ConfidenceEngineController {

    private final ConfidenceEngineService confidenceEngineService;

    public ConfidenceEngineController(
            ConfidenceEngineService confidenceEngineService) {

        this.confidenceEngineService = confidenceEngineService;
    }

    @PostMapping("/{caseId}/confidence/evaluate")
    public ResponseEntity<ConfidenceScore> evaluateConfidence(
            @PathVariable Long caseId) {

        ConfidenceScore result =
                confidenceEngineService.evaluateConfidence(caseId);

        return ResponseEntity.ok(result);
    }
}