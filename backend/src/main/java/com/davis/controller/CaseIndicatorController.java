package com.davis.controller;

import com.davis.model.Indicator;
import com.davis.service.IndicatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class CaseIndicatorController {

    private final IndicatorService indicatorService;

    public CaseIndicatorController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    // POST /api/cases/{caseId}/indicators
    @PostMapping("/{caseId}/indicators")
    public ResponseEntity<Indicator> addIndicatorToCase(
            @PathVariable Long caseId,
            @RequestBody Map<String, String> request) {

        String indicatorType =
                request.get("indicatorType");

        String indicatorValue =
                request.get("indicatorValue");

        String description =
                request.get("description");

        if (indicatorType == null
                || indicatorType.isBlank()
                || indicatorValue == null
                || indicatorValue.isBlank()) {

            return ResponseEntity.badRequest().build();
        }

        Indicator indicator =
                indicatorService.addIndicatorToCase(
                        caseId,
                        indicatorType,
                        indicatorValue,
                        description
                );

        return ResponseEntity.ok(indicator);
    }
}