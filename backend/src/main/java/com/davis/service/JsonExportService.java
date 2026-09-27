package com.davis.service;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class JsonExportService {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    public JsonExportService(
            ReportService reportService,
            ObjectMapper objectMapper) {

        this.reportService = reportService;
        this.objectMapper = objectMapper;
    }

    public byte[] generateJson(Long caseId) {

        Map<String, Object> report =
                reportService.generateReport(caseId);

        String json =
                objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(report);

        return json.getBytes(StandardCharsets.UTF_8);
    }
}