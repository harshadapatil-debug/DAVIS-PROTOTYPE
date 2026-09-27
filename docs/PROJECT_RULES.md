# DAVIS Prototype — Project Rules

## 1. Purpose

DAVIS is a student hackathon prototype for dark-web threat-actor attribution.

The prototype demonstrates:

- Investigation from a known indicator
- Entity and relationship analysis
- Graphical relationship representation
- Explainable attribution-confidence scoring
- Attribution stress testing
- Investigation report generation
- CSV, JSON and PDF export

The prototype uses controlled/synthetic demonstration intelligence.

---

## 2. Repository Rule

The `main` branch is the stable branch.

Only the Integration Owner may merge code into `main`.

No teammate may directly push feature work to `main`.

---

## 3. Branch Rule

Each teammate must work on their assigned feature branch.

Example branches:

- `feature/frontend`
- `feature/graph`
- `feature/confidence`
- `feature/intelligence`
- `feature/report-export`

Do not work directly on `main`.

---

## 4. File Ownership Rule

Each teammate works only on the files assigned to them.

Do not modify another teammate's module unless the Integration Owner approves it.

If a task requires changes outside the assigned files, inform the Integration Owner first.

---

## 5. Scope Rule

The following prototype features are frozen:

1. Investigation flow
2. Graphical representation
3. Explainable attribution-confidence score
4. Stress testing
5. Report generation
6. CSV / JSON / PDF export

Do not add new major features without approval from the Integration Owner.

---

## 6. Technology Rule

Current prototype architecture:

Frontend:
- HTML
- CSS
- JavaScript

Backend:
- Java
- Spring Boot

Database:
- MySQL

Communication:
- REST APIs
- JSON

Do not introduce React, Angular, Vue, a new backend framework, a new database, or another major technology without approval.

---

## 7. AI Coding Rule

AI may be used to generate code.

However:

- AI-generated code must be reviewed by the teammate who requested it.
- The teammate must understand the basic purpose of the generated code.
- The code must be tested before submission.
- AI must not rewrite unrelated files.
- AI must not change API contracts without approval.
- AI must not change the database schema without approval.

Use small, specific AI coding tasks.

Do not ask AI to "rewrite the whole project."

---

## 8. Data Rule

The frontend must not invent investigation results.

The backend/database is the source of truth for the final integrated investigation.

Demo intelligence must be clearly identified as controlled/synthetic data.

Do not claim that the prototype performs unrestricted live dark-web crawling.

---

## 9. API Rule

API field names are frozen in `API_CONTRACT.md`.

Do not rename fields such as:

- caseId
- entityId
- relationshipId
- evidenceId
- score

without approval.

---

## 10. Confidence Rule

The prototype output is an:

"Attribution Confidence Assessment"

It is not:

- proof of identity
- probability that two people are the same person
- a declaration that a person is a threat actor

The score must be explainable through contributing evidence/signals.

---

## 11. Stress-Test Rule

Stress testing must challenge the attribution assessment without permanently modifying the original case evidence.

Examples:

- Remove one supporting signal
- Reduce a signal's contribution
- Simulate AI paraphrasing
- Introduce contradictory evidence

The original case must be restorable.

---

## 12. Testing Rule

Every feature must have a simple test procedure.

Before a branch is merged, the teammate must provide:

1. Files changed
2. What changed
3. How it was tested
4. Expected result
5. Actual result

---

## 13. Integration Rule

A feature is not considered complete merely because it works on the teammate's computer.

It must work after integration with the rest of DAVIS.

The Integration Owner performs final integration testing.

---

## 14. No Hard-Coded Final Results

Do not hard-code:

- final confidence scores
- graph relationships
- investigation results
- report values

The final demo must obtain these from the agreed data flow.

---

## 15. No Unapproved Refactoring

Do not "clean up" or refactor unrelated code.

Working code should remain unchanged unless there is a specific reason to modify it.

---

## 16. Final Freeze

After DAVIS reaches the final demo state:

- No new features
- No framework changes
- No database redesign
- No major UI redesign

Only bug fixes and necessary demo reliability improvements are allowed.

---

## 17. Golden Demo

The final prototype must support this complete path:

Create Case
→ Enter Known Indicator
→ Run Investigation
→ View Entities
→ View Evidence
→ View Relationship Graph
→ View Attribution Confidence
→ View Explanation
→ Run Stress Test
→ Generate Report
→ Export PDF / CSV / JSON