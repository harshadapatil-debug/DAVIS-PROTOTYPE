package com.davis.controller;

import com.davis.model.Indicator;
import com.davis.service.IndicatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/indicators")
@CrossOrigin(origins = "*")
public class IndicatorController {

    private final IndicatorService indicatorService;

    public IndicatorController(IndicatorService indicatorService) {
        this.indicatorService = indicatorService;
    }

    @GetMapping
    public List<Indicator> getAllIndicators() {
        return indicatorService.getAllIndicators();
    }

    @GetMapping("/{indicatorId}")
    public ResponseEntity<Indicator> getIndicatorById(
            @PathVariable Long indicatorId) {

        return indicatorService.getIndicatorById(indicatorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Indicator> createIndicator(
            @RequestBody Indicator indicator) {

        Indicator savedIndicator = indicatorService.saveIndicator(indicator);
        return ResponseEntity.ok(savedIndicator);
    }

    @DeleteMapping("/{indicatorId}")
    public ResponseEntity<Void> deleteIndicator(
            @PathVariable Long indicatorId) {

        if (indicatorService.getIndicatorById(indicatorId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        indicatorService.deleteIndicator(indicatorId);
        return ResponseEntity.noContent().build();
    }
}