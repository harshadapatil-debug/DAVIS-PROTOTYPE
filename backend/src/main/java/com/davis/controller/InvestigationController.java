package com.davis.controller;

import com.davis.service.InvestigationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class InvestigationController {

    private final InvestigationService investigationService;

    public InvestigationController(
            InvestigationService investigationService) {

        this.investigationService = investigationService;
    }

    @PostMapping("/{caseId}/investigate")
    public ResponseEntity<Map<String, Object>> runInvestigation(
            @PathVariable Long caseId) {

        Map<String, Object> result =
                investigationService.runInvestigation(caseId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{caseId}/investigation")
    public ResponseEntity<Map<String, Object>> getInvestigation(
            @PathVariable Long caseId) {

        Map<String, Object> result =
                investigationService.getInvestigation(caseId);

        return ResponseEntity.ok(result);
    }
}