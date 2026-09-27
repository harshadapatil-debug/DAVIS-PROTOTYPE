package com.davis.controller;

import com.davis.service.StressTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class StressTestController {

    private final StressTestService stressTestService;

    public StressTestController(StressTestService stressTestService) {
        this.stressTestService = stressTestService;
    }

    @PostMapping("/{caseId}/stress-test")
    public ResponseEntity<Map<String, Object>> runStressTest(
            @PathVariable Long caseId,
            @RequestBody Map<String, Long> request) {

        Long evidenceId = request.get("evidenceId");

        if (evidenceId == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> result =
                stressTestService.runStressTest(
                        caseId,
                        evidenceId
                );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/{caseId}/stress-test/reset")
    public ResponseEntity<Map<String, Object>> resetStressTest(
            @PathVariable Long caseId) {

        Map<String, Object> result =
                stressTestService.resetStressTest(caseId);

        return ResponseEntity.ok(result);
    }
}