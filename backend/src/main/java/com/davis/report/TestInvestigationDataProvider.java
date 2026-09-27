package com.davis.report;

import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.davis.report.model.ReportData;

/**
 * TEMPORARY TEST DATA PROVIDER
 *
 * This class is ONLY for testing the Report & Export module before integration
 * with the real DAVIS backend.
 *
 * It must be removed/replaced during final integration.
 */
@Service
public class TestInvestigationDataProvider
        implements InvestigationDataProvider {

    @Override
    public ReportData getInvestigation(Long caseId) {

        ReportData data = new ReportData();

        // ============================================================
        // CASE INFORMATION
        // ============================================================
        data.setCaseId(caseId);
        data.setCaseName("Operation Monsoon");
        data.setCaseType("Cyber Crime Investigation");
        data.setCaseDescription(
                "Controlled investigation case used to test the DAVIS "
                + "Report and Export module."
        );
        data.setGeneratedAt("2026-09-27T13:00:00");

        // ============================================================
        // KNOWN INDICATOR
        // ============================================================
        data.setIndicatorType("Username");
        data.setIndicatorValue("r4v3n_mh");

        // ============================================================
        // INVESTIGATION SUMMARY
        // ============================================================
        data.setInvestigationSummary(
                "The investigation started from a known username and "
                + "correlated related entities, relationships and "
                + "supporting evidence across the controlled dataset."
        );

        // ============================================================
        // ENTITIES
        // ============================================================
        ReportData.EntityData entity1 = new ReportData.EntityData();
        entity1.setId("E001");
        entity1.setType("USERNAME");
        entity1.setValue("r4v3n_mh");

        ReportData.EntityData entity2 = new ReportData.EntityData();
        entity2.setId("E002");
        entity2.setType("EMAIL");
        entity2.setValue("raven.mh@example.test");

        ReportData.EntityData entity3 = new ReportData.EntityData();
        entity3.setId("E003");
        entity3.setType("WALLET");
        entity3.setValue("0xTEST123456");

        data.setEntities(Arrays.asList(
                entity1,
                entity2,
                entity3
        ));

        // ============================================================
        // RELATIONSHIPS
        // ============================================================
        ReportData.RelationshipData relationship1
                = new ReportData.RelationshipData();

        relationship1.setId("R001");
        relationship1.setSourceEntity("E001");
        relationship1.setRelationshipType("ASSOCIATED_WITH");
        relationship1.setTargetEntity("E002");

        ReportData.RelationshipData relationship2
                = new ReportData.RelationshipData();

        relationship2.setId("R002");
        relationship2.setSourceEntity("E001");
        relationship2.setRelationshipType("LINKED_TO");
        relationship2.setTargetEntity("E003");

        data.setRelationships(Arrays.asList(
                relationship1,
                relationship2
        ));

        // ============================================================
        // EVIDENCE
        // ============================================================
        ReportData.EvidenceData evidence1
                = new ReportData.EvidenceData();

        evidence1.setId("EV001");
        evidence1.setType("TEXT");
        evidence1.setDescription(
                "Matching username observed across two controlled sources."
        );
        evidence1.setSource("Controlled Forum Dataset");
        evidence1.setStrength("HIGH");
        evidence1.setReliability("MEDIUM");
        evidence1.setDirection("SUPPORTING");
        evidence1.setObservedAt("2026-09-20T10:15:00");

        ReportData.EvidenceData evidence2
                = new ReportData.EvidenceData();

        evidence2.setId("EV002");
        evidence2.setType("WALLET");
        evidence2.setDescription(
                "Wallet identifier associated with the known username."
        );
        evidence2.setSource("Controlled Blockchain Dataset");
        evidence2.setStrength("MEDIUM");
        evidence2.setReliability("MEDIUM");
        evidence2.setDirection("SUPPORTING");
        evidence2.setObservedAt("2026-09-21T14:30:00");

        data.setEvidence(Arrays.asList(
                evidence1,
                evidence2
        ));

        // ============================================================
        // TIMELINE
        // ============================================================
        ReportData.TimelineData timeline1
                = new ReportData.TimelineData();

        timeline1.setTimestamp("2026-09-20T10:15:00");
        timeline1.setEvent("Known username identified in controlled dataset.");
        timeline1.setSource("Controlled Forum Dataset");

        ReportData.TimelineData timeline2
                = new ReportData.TimelineData();

        timeline2.setTimestamp("2026-09-21T14:30:00");
        timeline2.setEvent("Related wallet identifier observed.");
        timeline2.setSource("Controlled Blockchain Dataset");

        data.setTimeline(Arrays.asList(
                timeline1,
                timeline2
        ));

        // ============================================================
        // CONFIDENCE
        // ============================================================
        data.setConfidenceScore(84.0);
        data.setRiskLevel("HIGH");

        data.setConfidenceExplanation(
                "Multiple independent signals in the controlled dataset "
                + "support the attribution assessment."
        );

        // ============================================================
        // REVIEW
        // ============================================================
        data.setReviewStatus("ACCEPTED");
        data.setReviewerName("Test Investigator");
        data.setReviewRemarks(
                "Controlled test review completed."
        );

        // ============================================================
        // STRESS TEST
        // ============================================================
        data.setStressTestStatus("COMPLETED");
        data.setStressTestResult(
                "Assessment remained supported after removal of a "
                + "non-critical supporting signal."
        );

        // ============================================================
        // LIMITATIONS
        // ============================================================
        data.setLimitations(Arrays.asList(
                "SYNTHETIC / CONTROLLED DATASET — PROTOTYPE DEMONSTRATION",
                "The result is an attribution-confidence assessment, not identity proof.",
                "The confidence score is not a calibrated probability.",
                "Real-world investigation data has not yet been integrated."
        ));

        return data;
    }
}
