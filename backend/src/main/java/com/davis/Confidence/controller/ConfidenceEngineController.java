package com.davis.controller;

import com.davis.model.ConfidenceResponse;
import com.davis.service.ConfidenceEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class ConfidenceEngineController {

    private final ConfidenceEngineService confidenceEngineService;

    public ConfidenceEngineController(
            ConfidenceEngineService confidenceEngineService
    ) {
        this.confidenceEngineService =
                confidenceEngineService;
    }

    /*
     * POST
     * /api/cases/{caseId}/confidence/evaluate
     */
    @PostMapping("/{caseId}/confidence/evaluate")
    public ResponseEntity<ConfidenceResponse> evaluateConfidence(
            @PathVariable Long caseId
    ) {

        ConfidenceResponse result =
                confidenceEngineService
                        .evaluateConfidence(caseId);

        return ResponseEntity.ok(result);
    }

    /*
     * GET
     * /api/cases/{caseId}/confidence
     */
    @GetMapping("/{caseId}/confidence")
    public ResponseEntity<ConfidenceResponse> getConfidence(
            @PathVariable Long caseId
    ) {

        ConfidenceResponse result =
                confidenceEngineService
                        .getConfidence(caseId);

        return ResponseEntity.ok(result);
    }
}