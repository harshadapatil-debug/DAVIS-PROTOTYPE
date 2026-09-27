package com.davis.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.davis.report.export.CsvReportExporter;
import com.davis.report.export.JsonReportExporter;
import com.davis.report.export.PdfReportExporter;
import com.davis.report.model.ReportData;

/**
 * REPORT SERVICE
 *
 * Responsibility:
 * 1. Fetch final investigation data
 * 2. Prepare final report
 * 3. Send the same backend data to PDF/CSV/JSON exporters
 *
 * This service does NOT perform:
 * - entity extraction
 * - relationship discovery
 * - confidence calculation
 * - evidence generation
 * - stress testing
 * - database persistence
 */
@Service
public class ReportService {

    private final InvestigationDataProvider investigationDataProvider;

    private final PdfReportExporter pdfReportExporter;
    private final CsvReportExporter csvReportExporter;
    private final JsonReportExporter jsonReportExporter;


    public ReportService(
            InvestigationDataProvider investigationDataProvider,
            PdfReportExporter pdfReportExporter,
            CsvReportExporter csvReportExporter,
            JsonReportExporter jsonReportExporter) {

        this.investigationDataProvider = investigationDataProvider;
        this.pdfReportExporter = pdfReportExporter;
        this.csvReportExporter = csvReportExporter;
        this.jsonReportExporter = jsonReportExporter;
    }


    /**
     * Generates the structured final report object.
     */
    public ReportData generateReport(Long caseId) {

        /*
         * ============================================================
         * INTEGRATION POINT
         * ============================================================
         *
         * The actual data comes from the main DAVIS backend through
         * InvestigationDataProvider.
         *
         * DO NOT replace this with hard-coded/sample data.
         */
        return investigationDataProvider.getInvestigation(caseId);
    }


    /**
     * Generates final investigation PDF.
     */
    public byte[] exportPdf(Long caseId) throws IOException {

        ReportData data = generateReport(caseId);

        return pdfReportExporter.generate(data);
    }

    /**
     * Generates CSV export.
     */
    public byte[] exportCsv(Long caseId) {

        ReportData data = generateReport(caseId);

        String csv = csvReportExporter.generate(data);

        return csv.getBytes(StandardCharsets.UTF_8);
    }


    /**
     * Generates JSON export.
     */
    public byte[] exportJson(Long caseId) throws IOException {

        ReportData data = generateReport(caseId);

        return jsonReportExporter.generate(data);
    }
}