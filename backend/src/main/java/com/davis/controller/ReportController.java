package com.davis.controller;

import com.davis.service.CsvExportService;
import com.davis.service.JsonExportService;
import com.davis.service.PdfExportService;
import com.davis.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final PdfExportService pdfExportService;
    private final CsvExportService csvExportService;
    private final JsonExportService jsonExportService;

    public ReportController(
            ReportService reportService,
            PdfExportService pdfExportService,
            CsvExportService csvExportService,
            JsonExportService jsonExportService) {

        this.reportService = reportService;
        this.pdfExportService = pdfExportService;
        this.csvExportService = csvExportService;
        this.jsonExportService = jsonExportService;
    }

    // GET /api/cases/{caseId}/report
    @GetMapping("/{caseId}/report")
    public ResponseEntity<Map<String, Object>> generateReport(
            @PathVariable Long caseId) {

        Map<String, Object> report =
                reportService.generateReport(caseId);

        return ResponseEntity.ok(report);
    }

    // GET /api/cases/{caseId}/export/pdf
    @GetMapping("/{caseId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long caseId)
            throws IOException {

        byte[] pdf =
                pdfExportService.generatePdf(caseId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"DAVIS_Case_"
                                + caseId
                                + "_Report.pdf\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // GET /api/cases/{caseId}/export/csv
    @GetMapping("/{caseId}/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable Long caseId) {

        byte[] csv =
                csvExportService.generateCsv(caseId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"DAVIS_Case_"
                                + caseId
                                + "_Report.csv\""
                )
                .contentType(
                        MediaType.parseMediaType("text/csv")
                )
                .body(csv);
    }

    // GET /api/cases/{caseId}/export/json
    @GetMapping("/{caseId}/export/json")
    public ResponseEntity<byte[]> exportJson(
            @PathVariable Long caseId)
            throws Exception {

        byte[] json =
                jsonExportService.generateJson(caseId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"DAVIS_Case_"
                                + caseId
                                + "_Report.json\""
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .body(json);
    }
}