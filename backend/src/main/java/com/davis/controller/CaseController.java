package com.davis.controller;

import com.davis.model.Case;
import com.davis.service.CaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    public List<Case> getAllCases() {
        return caseService.getAllCases();
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<Case> getCaseById(@PathVariable Long caseId) {
        return caseService.getCaseById(caseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Case> createCase(@RequestBody Case caseEntity) {
        Case savedCase = caseService.saveCase(caseEntity);
        return ResponseEntity.ok(savedCase);
    }

    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> deleteCase(@PathVariable Long caseId) {
        if (caseService.getCaseById(caseId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        caseService.deleteCase(caseId);
        return ResponseEntity.noContent().build();
    }
}