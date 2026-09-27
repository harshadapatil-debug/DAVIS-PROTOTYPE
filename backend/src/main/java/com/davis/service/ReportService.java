package com.davis.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportService {

    private final InvestigationService investigationService;

    public ReportService(InvestigationService investigationService) {
        this.investigationService = investigationService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> generateReport(Long caseId) {

        Map<String, Object> investigation =
                investigationService.getInvestigation(caseId);

        Map<String, Object> report =
                new LinkedHashMap<>();

        report.put("reportTitle", "DAVIS Investigation Report");
        report.put("generatedAt", LocalDateTime.now());

        report.put("case", investigation.get("case"));
        report.put("knownIndicator", investigation.get("knownIndicator"));
        report.put("summary", investigation.get("summary"));
        report.put("entities", investigation.get("entities"));
        report.put("relationships", investigation.get("relationships"));
        report.put("evidence", investigation.get("evidence"));
        report.put("confidence", investigation.get("confidence"));
        report.put("review", investigation.get("review"));
        report.put("timeline", investigation.get("timeline"));
        report.put("graph", investigation.get("graph"));

        report.put(
                "disclaimer",
                "This report is generated from a controlled synthetic "
                        + "investigation dataset. The attribution-confidence "
                        + "score is an explainable prototype assessment and "
                        + "is not identity proof or a calibrated probability."
        );

        return report;
    }
}