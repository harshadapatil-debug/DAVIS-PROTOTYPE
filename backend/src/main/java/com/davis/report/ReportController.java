package com.davis.report;

import java.io.IOException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.davis.report.model.ReportData;

/**
 * REPORT CONTROLLER
 *
 * Provides endpoints for:
 * 1. Viewing the structured investigation report
 * 2. Exporting the final report as PDF
 * 3. Exporting investigation data as CSV
 * 4. Exporting investigation data as JSON
 * 5. Generating the Section 63 BSA certificate-style PDF
 *
 * This controller does NOT access the database directly.
 *
 * Data flow:
 *
 * Existing DAVIS Backend
 *          ↓
 * InvestigationDataProvider
 *          ↓
 * ReportService
 *          ↓
 * ReportController
 *          ↓
 * PDF / CSV / JSON / Certificate
 *
 * IMPORTANT:
 * The current InvestigationDataProvider may temporarily use
 * controlled test data until the real DAVIS backend is integrated.
 */
@RestController
@RequestMapping("/api/cases")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }


    // ============================================================
    // FINAL REPORT
    // ============================================================

    /**
     * Returns the complete structured investigation report.
     *
     * Endpoint:
     * GET /api/cases/{caseId}/report
     */
    @GetMapping("/{caseId}/report")
    public ResponseEntity<ReportData> getReport(
            @PathVariable Long caseId) {

        ReportData report = reportService.generateReport(caseId);

        return ResponseEntity.ok(report);
    }


    // ============================================================
    // PDF EXPORT
    // ============================================================

    /**
     * Generates and downloads the final investigation PDF.
     *
     * Endpoint:
     * GET /api/cases/{caseId}/export/pdf
     */
    @GetMapping("/{caseId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long caseId) throws IOException {

        byte[] pdf = reportService.exportPdf(caseId);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "DAVIS-Case-" +
                                caseId +
                                "-Report.pdf"
                        )
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdf);
    }


    // ============================================================
    // CSV EXPORT
    // ============================================================

    /**
     * Generates and downloads the investigation CSV.
     *
     * Endpoint:
     * GET /api/cases/{caseId}/export/csv
     */
    @GetMapping("/{caseId}/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable Long caseId) {

        byte[] csv = reportService.exportCsv(caseId);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType("text/csv")
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "DAVIS-Case-" +
                                caseId +
                                "-Investigation.csv"
                        )
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(csv);
    }


    // ============================================================
    // JSON EXPORT
    // ============================================================

    /**
     * Generates and downloads the complete investigation JSON.
     *
     * Endpoint:
     * GET /api/cases/{caseId}/export/json
     */
    @GetMapping("/{caseId}/export/json")
    public ResponseEntity<byte[]> exportJson(
            @PathVariable Long caseId) throws IOException {

        byte[] json = reportService.exportJson(caseId);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.setContentDisposition(
                ContentDisposition
                        .attachment()
                        .filename(
                                "DAVIS-Case-" +
                                caseId +
                                "-Investigation.json"
                        )
                        .build()
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(json);
    }

}