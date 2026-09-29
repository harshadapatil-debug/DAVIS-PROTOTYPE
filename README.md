DAVIS — Dark-web Actor Verification and Intelligence System

Overview

DAVIS is a digital investigation prototype designed to help investigators correlate information related to suspected threat actors across dark/restricted-web and public-web sources.

Instead of analysing isolated indicators separately, DAVIS brings together multiple evidence types such as usernames, email addresses, domains, wallet addresses, textual information, relationships, behavioural patterns, and temporal information into a single investigation workflow.

The system helps an analyst move from an initial indicator to a connected evidence view, attribution-support score, explanation, stress testing, and an auditable investigation report.

«Prototype Note: The current demonstration uses controlled/synthetic data to showcase the investigation workflow. It does not perform real-world deanonymization or identify real individuals.»

---

Problem

Digital investigations often involve fragmented information spread across multiple sources and evidence types. Manually connecting these indicators can make investigations time-consuming and difficult to audit.

DAVIS addresses this by providing a unified workflow for:

- Indicator-based investigation
- Entity and relationship extraction
- Evidence correlation
- Graph-based investigation
- Timeline analysis
- Attribution-support analysis
- Explainable scoring
- Analyst review and stress testing
- Evidence-backed report generation

---

Key Features

1. Indicator-Based Investigation

Start an investigation using available indicators such as:

- Username
- Email
- Domain
- Wallet address
- Textual indicators

2. Entity & Relationship Correlation

Extract entities and connect related evidence to identify meaningful relationships between indicators, accounts, domains and other entities.

3. Investigation Graph

Represent discovered entities and their relationships as an interactive graph to help analysts understand connections.

4. Timeline Analysis

Organise relevant events chronologically to help identify behavioural and temporal relationships.

5. Attribution-Support Score

DAVIS produces a score that represents the level of supporting evidence available for an attribution hypothesis.

The score is not an identity declaration or proof of guilt.

6. Explainable Analysis

The system provides supporting factors behind the generated score so that analysts can understand why evidence contributes to an attribution hypothesis.

7. Stress Testing

Analysts can examine how the attribution-support result changes when evidence or relationships are removed or challenged.

This helps identify dependencies, contradictions and alternative explanations.

8. Human-in-the-Loop Review

The analyst remains responsible for reviewing evidence and deciding whether the available information is sufficient for further investigation.

9. Evidence & Audit Trail

Investigation outputs can be exported for further analysis and documentation, with integrity-oriented hashing/audit mechanisms included in the workflow.

10. Report & Export

The prototype supports investigation report generation and structured exports for further use.

Supported export formats include:

- PDF
- CSV
- JSON

---

Investigation Workflow

Seed
  ↓
Discover
  ↓
Extract & Correlate
  ↓
Graph & Fuse
  ↓
Score & Explain
  ↓
Stress-Test + Analyst Review
  ↓
Investigate
  ↓
Lead & Audit

---

Technology Stack

Layer| Technology
Frontend| HTML, CSS, JavaScript
Visualization| Cytoscape.js, Chart.js
Backend| Java, Spring Boot
Database| MySQL
API| REST APIs
Build| Maven
Version Control| Git / GitHub
Reporting| PDF / structured export generation

Additional AI/ML and advanced intelligence capabilities are planned for future development beyond the current controlled prototype.

---

Prototype Scope

The current prototype demonstrates the complete investigation flow using controlled data:

1. Create investigation case
2. Add known indicator
3. Perform controlled analysis
4. Extract entities
5. Identify relationships
6. Collect evidence
7. Visualise investigation graph
8. Generate timeline
9. Calculate attribution-support score
10. Explain score
11. Perform analyst review
12. Stress-test the analysis
13. Generate final investigation report
14. Export PDF
15. Export CSV
16. Export JSON
17. Maintain investigation/audit information

---

Project Structure

DAVIS-PROTOTYPE/
│
├── backend/
│   └── src/
│       └── main/
│           └── java/
│               └── com/
│                   └── davis/
│
├── frontend/
│   ├── index.html
│   ├── css/
│   └── js/
│
├── database/
│   └── init.sql
│
├── README.md
├── RUNNING.md
└── pom.xml

«The exact directory structure may vary with the current repository version.»

---

Important Disclaimer

DAVIS is an investigation-support prototype.

Its attribution-support score is intended to organise and explain available evidence. It should not be interpreted as an automated declaration of identity, guilt, or legal responsibility.

The current demonstration uses synthetic/controlled data and is intended to demonstrate the technical workflow rather than conduct real-world investigations.

---

Future Scope

Future versions can extend DAVIS with:

- AI-aware stylometry
- AI-generated/paraphrased text detection
- Expanded dark/restricted-web intelligence integration
- Advanced entity resolution
- Behavioural correlation
- Visual similarity analysis
- Blockchain intelligence
- Public-web expansion with OPSEC controls
- Explainable AI enhancements
- Court-oriented evidence packaging
- Deployment in controlled government infrastructure

---

Repository

This repository contains the prototype implementation, source code, database setup and documentation required to understand and run DAVIS locally.
