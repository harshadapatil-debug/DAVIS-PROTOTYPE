# DAVIS — Dark-Web Threat Actor Attribution Investigation System

DAVIS is a prototype investigation platform for threat-actor attribution
using observable digital evidence.

The prototype starts from a known indicator, analyzes controlled/synthetic
intelligence, extracts entities and relationships, links them through
evidence, generates an explainable attribution-confidence assessment,
allows investigator review and stress testing, and produces a final report.

---

## IMPORTANT

This is a PROTOTYPE using CONTROLLED / SYNTHETIC DATA.

DAVIS does NOT claim to:

- break Tor
- prove a person's real-world identity
- provide calibrated identity probabilities
- perform unrestricted live dark-web crawling

The system produces an:

"Attribution-confidence assessment for investigator review."

---

# PROJECT FLOW

CASE
↓
KNOWN INDICATOR
↓
CONTROLLED INTELLIGENCE
↓
AI/ML ANALYSIS
↓
ENTITIES
↓
RELATIONSHIPS
↓
EVIDENCE
↓
GRAPH + TIMELINE
↓
ATTRIBUTION-CONFIDENCE SCORE
↓
ANALYST REVIEW
↓
STRESS TEST
↓
FINAL REPORT
↓
PDF / CSV / JSON

---

# PROJECT STRUCTURE

DAVIS-PROTOTYPE/

├── backend/
│   └── Spring Boot backend and API
│
├── aiml/
│   ├── entity_extraction/
│   ├── similarity/
│   ├── relationship_detection/
│   └── api/
│
├── frontend/
│   ├── index.html
│   ├── css/
│   └── js/
│
├── database/
│   ├── schema.sql
│   └── seed.sql
│
├── data/
│   └── intelligence/
│       └── synthetic_intelligence.json
│
├── docs/
│   ├── PROJECT_RULES.md
│   └── API_CONTRACT.md
│
├── README.md
└── .gitignore

---

# TEAM RESPONSIBILITIES

## Harshada — Backend + Database + Integration

Responsible for:

- MySQL database
- Spring Boot backend
- API implementation
- Data persistence
- Composite investigation API
- Final integration
- Main branch

---

## Frontend

Responsible for:

- Investigation UI
- Backend API integration
- Entity/relationship display
- Relationship graph
- Evidence drill-down
- Timeline
- Confidence display
- Analyst review UI
- Stress-test UI
- Report/export controls

---

## AIML — Entity + Similarity

Responsible for:

- Entity extraction
- Text similarity
- Stylometric similarity
- Behaviour/activity similarity
- Normalized entity output
- Similarity evidence

---

## AIML — Relationship Detection

Responsible for:

- Candidate relationship detection
- Cross-marketplace correlation
- Infrastructure relationship signals
- Persona relationship signals
- Supporting evidence
- Normalized relationship output

---

## Confidence + Stress Test

Responsible for:

- Explainable attribution-confidence score
- Confidence factors
- Evidence-based scoring
- Remove-one-signal stress test
- Baseline vs challenged score
- Robustness indicator

---

## Report + Export

Responsible for:

- Final investigation report
- PDF export
- CSV export
- JSON export
- Synthetic-data disclaimer
- Final case summary

---

# BEFORE CODING

EVERY TEAM MEMBER MUST READ:

1. docs/PROJECT_RULES.md
2. docs/API_CONTRACT.md
3. README.md

Then read the files belonging to their assigned feature.

API_CONTRACT.md is the authoritative integration reference.

---

# SOURCE OF TRUTH

The BACKEND is the single source of truth.

Frontend must consume backend data.

AIML must return normalized data.

Confidence must use backend evidence.

Reports and exports must use backend investigation data.

Do not create a second source of truth through hard-coded frontend data.

---

# AI/ML ROLE

AI/ML supports the investigation by:

- extracting entities
- detecting similarities
- suggesting relationships
- generating supporting evidence

AI/ML does NOT make the final attribution decision.

The final assessment is produced from the normalized evidence by the
confidence engine and remains subject to investigator review.

---

# CORE DATA MODEL

CASE
→ INDICATOR
→ ENTITY
→ RELATIONSHIP
→ EVIDENCE
→ CONFIDENCE
→ REVIEW
→ STRESS TEST
→ REPORT / EXPORT

Graph and timeline are views of this same investigation data.

---

# SUPPORTED PROTOTYPE AREAS

## Infrastructure Correlation

Examples:

- onion service
- domain
- server
- IP
- SSL certificate
- infrastructure reuse
- clearnet correlation

## Cross-Marketplace Correlation

Examples:

- usernames
- aliases
- emails
- PGP keys
- wallets
- marketplaces

## Persona / Behaviour Correlation

Examples:

- writing similarity
- behavioural similarity
- activity-pattern similarity

All of these are represented through the same
ENTITY → RELATIONSHIP → EVIDENCE model.

---

# STRESS TEST

The required prototype stress test is:

REMOVE ONE SIGNAL

The selected evidence is temporarily removed from the confidence
calculation.

Original stored evidence is NOT deleted or permanently modified.

The system shows:

- baseline score
- challenged score
- score change
- assessment-level change
- robustness indicator
- explanation

---

# FINAL DEMO

The prototype should demonstrate:

1. Create a case
2. Enter a known indicator
3. Run controlled intelligence analysis
4. Extract entities
5. Generate relationships
6. Show evidence
7. Open the relationship graph
8. Show timeline
9. Calculate attribution-confidence score
10. Show score explanation
11. Record analyst review
12. Run stress test
13. Reset stress test
14. Generate final report
15. Export PDF
16. Export CSV
17. Export JSON

The UI must clearly identify the dataset as synthetic/controlled.

---

# DEVELOPMENT RULES

- Work only on your assigned branch.
- Do not modify main directly.
- Do not change API names or fields without approval.
- Do not change the database structure without coordination.
- Do not add unnecessary frameworks or features.
- Do not hard-code final investigation results in the frontend.
- Do not delete evidence during stress testing.
- Do not claim AI proves identity.
- Keep implementation suitable for a student prototype.

If a change appears to require a contract change:

STOP → INFORM THE INTEGRATION OWNER → AGREE ON THE CHANGE → UPDATE THE
CONTRACT → THEN CODE.

---

# GIT WORKFLOW

Each feature is developed on its own branch.

Examples:

feature/frontend
feature/aiml-entity
feature/aiml-relationship
feature/confidence
feature/report-export

Harshada owns:

main

Workflow:

CREATE / SWITCH TO BRANCH
→ CODE
→ TEST
→ COMMIT
→ PUSH
→ SEND BRANCH FOR INTEGRATION
→ REVIEW
→ MERGE INTO MAIN

---

# IMPORTANT DOCUMENTS

docs/PROJECT_RULES.md
→ Rules and scope

docs/API_CONTRACT.md
→ Frozen API and integration contract

README.md
→ Project overview and development map

---

# CURRENT PROTOTYPE LIMITATION

The current implementation is intended to demonstrate the complete
analytical workflow on controlled/synthetic intelligence.

Live authorized intelligence connectors, large-scale graph infrastructure,
advanced identity resolution, calibrated attribution models, extensive
blockchain analytics, and continuous monitoring are outside the current
prototype scope.