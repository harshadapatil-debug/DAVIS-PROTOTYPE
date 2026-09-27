package com.davis.report;

import com.davis.report.model.ReportData;

/**
 * INTEGRATION BOUNDARY
 *
 * This interface intentionally contains NO database logic.
 *
 * IMPORTANT:
 * The prototype leader / backend owner must implement this interface
 * using the EXISTING DAVIS backend services/repositories.
 *
 * Do NOT create a second repository or database for the report module.
 *
 * Example integration later:
 *
 * Existing CaseService
 * Existing EntityService
 * Existing EvidenceService
 * Existing ConfidenceService
 * Existing ReviewService
 * Existing StressTestService
 *              ↓
 *      ReportDataMapper
 *              ↓
 * InvestigationDataProvider
 */
public interface InvestigationDataProvider {

    /**
     * Fetches the complete investigation required for reporting.
     *
     * @param caseId existing DAVIS case ID
     * @return actual investigation data from the main backend
     */
    ReportData getInvestigation(Long caseId);
}