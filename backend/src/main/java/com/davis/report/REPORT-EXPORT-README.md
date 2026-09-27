DAVIS — Report & Export Module Integration Guide

Application.java should contain:
package com.davis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}


pom.xml should contain: 
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.davis</groupId>
    <artifactId>davis-backend</artifactId>
    <version>1.0.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.6</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- OpenPDF -->
        <!-- REPORT MODULE DEPENDENCY Integration note: 
         Keep this dependency only if OpenPDF is not already present in the main backend pom.xml.
         This module uses OpenPDF for: - Final report PDF - Section 63 BSA certificate-style PDF -->
        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf</artifactId>
            <version>2.2.5</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

        </plugins>
    </build>

</project>

1. Purpose

This document explains how to integrate the DAVIS Report + Export module with the main DAVIS backend and frontend.

The module provides:

Investigation report generation

PDF export

CSV export

JSON export

It consumes investigation data from the existing backend. It does not create a second database, duplicate investigation logic, or independently calculate attribution.

Architecture:

Existing DAVIS Backend
→ InvestigationDataProvider
→ ReportData
→ ReportService
→ Report / PDF / CSV / JSON
→ Existing DAVIS Frontend

2. Module Ownership

Owner: Shreya
Branch: feature/report-export

This module owns:

Final investigation report generation

PDF export

CSV export

JSON export

Report/export formatting

Integration interface for receiving investigation data

It does NOT own:

Case creation

Database schema

Entity extraction/discovery

Relationship discovery

Evidence collection

Confidence calculation

Stress-test calculation

Authentication

Core investigation logic

3. Temporary Test Data — IMPORTANT

The current development branch contains:

TestInvestigationDataProvider.java

This is a temporary controlled-data provider used only to test the Report + Export module before integration with the real backend.

It provides synthetic data such as:

Case ID: 101

Case: Operation Monsoon

Indicator: r4v3n_mh

3 entities

2 relationships

2 evidence records

Confidence: 84

Risk: HIGH

This is NOT production investigation data.

Before final integration

Harshada's real backend implementation of InvestigationDataProvider must replace the temporary provider.

The temporary provider must then be removed or disabled so it does not compete with the real provider as a Spring service.

Do not allow synthetic test data to appear when the frontend requests a real case.

4. Project Structure

backend/
└── src/main/java/com/davis/
    └── report/
        ├── export/
        │   ├── CsvReportExporter.java
        │   ├── JsonReportExporter.java
        │   └── PdfReportExporter.java
        ├── model/
        │   └── ReportData.java
        ├── InvestigationDataProvider.java
        ├── ReportController.java
        └── ReportService.java

5. Main Integration Boundary

The key interface is:

public interface InvestigationDataProvider {
    ReportData getInvestigation(Long caseId);
}

The report module does not directly query the database.

The intended flow is:

Existing Database
      ↓
Existing Backend Services / Repositories
      ↓
InvestigationDataProvider
      ↓
ReportService
      ↓
ReportData
      ↓
Exporters

6. What the Real Backend Must Implement

Harshada should create the real implementation of InvestigationDataProvider.

Example:

@Service
public class BackendInvestigationDataProvider
        implements InvestigationDataProvider {

    @Override
    public ReportData getInvestigation(Long caseId) {
        // Fetch existing DAVIS investigation data
        // Convert it into ReportData
        // Return completed ReportData
    }
}

The exact repository/service classes should use the existing DAVIS backend.

Do not create a second database or duplicate repositories for reports.

7. Data Required by ReportData

The real provider should populate:

Case

caseId
caseName
caseType
caseDescription
generatedAt

Known indicator

indicatorType
indicatorValue

Investigation summary

investigationSummary

Entities

id
type
value

Relationships

id
sourceEntity
relationshipType
targetEntity

Evidence

id
type
description
source
strength
reliability
direction
observedAt

Timeline

timestamp
event
source

Confidence

confidenceScore
riskLevel
confidenceExplanation

The score is an attribution-confidence assessment from 0–100. It is not a calibrated probability and is not identity proof.

Review

reviewStatus
reviewerName
reviewRemarks

Stress test

stressTestStatus
stressTestResult

Limitations

The report should preserve appropriate limitations, including:

SYNTHETIC / CONTROLLED DATASET — PROTOTYPE DEMONSTRATION
The result is an attribution-confidence assessment, not identity proof.
The confidence score is not a calibrated probability.

For real data, synthetic-data wording should be updated appropriately.

8. Backend API Endpoints

Generate report

GET /api/cases/{caseId}/report

Example:

GET /api/cases/101/report

Returns the report data as JSON.

PDF

GET /api/cases/{caseId}/export/pdf

CSV

GET /api/cases/{caseId}/export/csv

CSV columns:

case_id
case_name
indicator_type
indicator_value
entity_id
entity_type
entity_value
relationship_id
relationship_type
evidence_id
evidence_type
source
strength
reliability
direction
observed_at
confidence_score
risk_level
review_status

Entity and relationship columns should only be populated when the backend explicitly provides those associations. Do not invent mappings merely to fill empty CSV fields.

JSON

GET /api/cases/{caseId}/export/json

JSON contains:

case

known indicator

entities

relationships

evidence

confidence

review

timeline

9. Backend Integration Steps

Step 1

Merge the com.davis.report package into the main backend.

Do not create another Spring Boot application.

Step 2

Keep the existing main Application.java.

Step 3

Implement the real InvestigationDataProvider.

The provider should:

Receive caseId

Fetch the case

Fetch known indicator

Fetch entities

Fetch relationships

Fetch evidence

Fetch confidence

Fetch review

Fetch stress-test information when available

Build timeline data

Return ReportData

Step 4

Map existing backend/database objects into the report model.

Example:

CaseEntity      → case fields
Indicator       → indicator fields
Entity          → EntityData
Relationship    → RelationshipData
Evidence        → EvidenceData
ConfidenceScore → confidence fields
Review          → review fields
StressTest      → stress-test fields

The provider performs this mapping. Existing database models do not need to be redesigned just for reporting.

10. POM / Dependency Integration

The final project's existing pom.xml is authoritative.

The report module requires PDF-generation support through OpenPDF.

If the main backend already has the required dependency, do not duplicate it.

If needed, add the dependency to the main backend POM.

Do NOT replace Harshada's main POM with the standalone development POM used to test this module.

11. Frontend Integration

The frontend should call the backend endpoints.

Recommended case-page UI:

INVESTIGATION REPORT

[ View Report ]

Export:
[ PDF ] [ CSV ] [ JSON ]

The frontend should use the current case ID dynamically.

12. View Report

Call:

GET /api/cases/{caseId}/report

Example:

async function viewReport(caseId) {
    const response = await fetch(`/api/cases/${caseId}/report`);

    if (!response.ok) {
        throw new Error("Failed to generate report");
    }

    const report = await response.json();
    console.log(report);
}

Display:

Case information

Known indicator

Investigation summary

Entities

Relationships

Evidence

Confidence

Risk

Review

Stress test

Timeline

Limitations

13. PDF Download

function downloadPDF(caseId) {
    window.open(`/api/cases/${caseId}/export/pdf`, "_blank");
}

Or:

<a href="/api/cases/101/export/pdf">Export PDF</a>

The frontend should not generate the PDF.

14. CSV Download

function downloadCSV(caseId) {
    window.open(`/api/cases/${caseId}/export/csv`, "_blank");
}

Or:

<a href="/api/cases/101/export/csv">Export CSV</a>

15. JSON Download

function downloadJSON(caseId) {
    window.open(`/api/cases/${caseId}/export/json`, "_blank");
}

Or:

<a href="/api/cases/101/export/json">Export JSON</a>

16. Frontend Responsibility

The frontend should display backend results and provide export controls.

It should NOT independently calculate:

Confidence score

Risk level

Evidence strength

Attribution assessment

Stress-test result

The backend remains the source of truth.

17. CORS

If frontend and backend are served from the same Spring Boot application, additional CORS configuration is normally unnecessary.

If the frontend runs on a different development origin, use the existing backend CORS configuration.

Do not add unnecessarily broad CORS rules.

18. Error Handling

A nonexistent case must not return synthetic test data.

For example:

GET /api/cases/999999/report

should return an appropriate error.

The frontend should display a clear message such as:

Unable to generate report for this case.

19. Testing Already Completed

The module was tested using controlled case 101.

Tested endpoints:

GET /api/cases/101/report
GET /api/cases/101/export/pdf
GET /api/cases/101/export/csv
GET /api/cases/101/export/json

The test case contains:

3 entities

2 relationships

2 evidence records

2 timeline events

Confidence 84

Risk HIGH

Review ACCEPTED

Stress test COMPLETED

These are controlled test values only.

20. Final Integration Testing

After connecting the real backend:

Create/open a development case.

Verify the known indicator.

Verify entities.

Verify relationships.

Verify evidence.

Verify confidence.

Verify review.

Verify stress-test data when available.

Open the report.

Export PDF.

Export CSV.

Export JSON.

Confirm every output belongs to the requested case.

Confirm synthetic test data no longer appears.

21. What Must NOT Be Added

Do not add:

A second database

A second Case table

Duplicate entity logic

A second confidence engine

A second stress-test engine

Live dark-web crawling

A graph database

Research-grade ML

Invented evidence relationships

Hard-coded production investigation data

Identity-proof claims

Certificate-generation functionality

22. Certificate Functionality

Certificate functionality was intentionally removed.

There is no:

CertificatePdfExporter

and no certificate endpoint.

Current scope is only:

Report + PDF + CSV + JSON

23. Report Contents

The final report documents:

Case information

Known indicator

Investigation summary

Discovered entities

Important relationships

Supporting evidence

Evidence sources

Evidence timestamps

Attribution-confidence score

Confidence explanation

Analyst review state

Stress-test result, when performed

Timeline

Prototype/data limitations

Traceability should follow:

Finding
  ↓
Relationship
  ↓
Evidence
  ↓
Source
  ↓
Confidence
  ↓
Reasoning

24. Final Integration Checklist

Backend

Merge com.davis.report

Keep existing Spring Boot application

Implement real InvestigationDataProvider

Connect it to existing services/repositories

Map existing data into ReportData

Confirm PDF dependency

Remove/disable TestInvestigationDataProvider

Verify all four endpoints

Database

Use existing DAVIS database

Do not create a report database

Verify case/entity/relationship/evidence retrieval

Verify confidence/review/stress-test retrieval where available

Frontend

Add View Report button

Add Export PDF button

Add Export CSV button

Add Export JSON button

Pass the current case ID dynamically

Do not duplicate backend calculations

Testing

Test report

Test PDF

Test CSV

Test JSON

Test invalid case ID

Test a real development case

Confirm synthetic data is no longer returned

25. Definition of Done

The module is fully integrated when:

Existing Investigation
        ↓
Existing Database
        ↓
Existing Backend
        ↓
Real InvestigationDataProvider
        ↓
ReportData
        ↓
ReportService
        ↓
Report / PDF / CSV / JSON
        ↓
Frontend
        ↓
Investigator

The user can open a case and:

View the report

Export PDF

Export CSV

Export JSON

using real backend investigation data.

26. Final Principle

The Report + Export module is a consumer of investigation data, not a second investigation engine.

Responsibility boundary:

AIML
  ↓
Find / extract signals

Backend
  ↓
Store and expose investigation data

Confidence / Review / Stress Test
  ↓
Evaluate investigation

Report Module
  ↓
Document and export investigation state

Frontend
  ↓
Display and provide export controls

The existing backend remains the single source of truth.