# DAVIS PROTOTYPE — API CONTRACT
Version: 1.0
Status: FROZEN FOR PROTOTYPE

## 1. PURPOSE

DAVIS is a case-centric dark-web threat-actor attribution investigation prototype.

The prototype workflow is:

CREATE CASE
→ ENTER KNOWN INDICATOR
→ CONTROLLED INTELLIGENCE EXPANSION
→ ENTITY EXTRACTION
→ RELATIONSHIP DETECTION
→ EVIDENCE CAPTURE
→ RELATIONSHIP GRAPH
→ TIMELINE
→ ATTRIBUTION-CONFIDENCE SCORE
→ ANALYST REVIEW
→ STRESS TEST
→ FINAL REPORT
→ EXPORT PDF / CSV / JSON

The prototype uses controlled/synthetic or pre-collected intelligence.

The prototype does NOT claim:
- breaking Tor
- proving a real-world identity
- identifying a person with certainty
- calibrated statistical probability
- unrestricted live dark-web crawling

Use the wording:
"Attribution-confidence assessment for investigator review."

Do NOT use:
"84% probability this is the same person."
"System identified the hacker."

---

# 2. ARCHITECTURE RULE

The backend is the single source of truth.

Frontend:
- consumes backend data
- does not recreate investigation data
- does not calculate confidence
- does not invent entities or relationships
- does not permanently change data during stress testing

AI/intelligence:
- extracts entities
- suggests relationships
- produces similarity signals
- produces normalized evidence
- does not make the final attribution decision

Confidence engine:
- consumes normalized evidence
- produces an explainable score
- does not represent the score as probability

Reports/exports:
- use the same backend investigation data shown in the UI

---

# 3. BASE URL

http://localhost:8080

All APIs use:

/api/...

Content-Type:

application/json

---

# 4. GENERAL JSON RULES

IDs must remain consistent throughout the system.

Dates use ISO-8601 format.

Example:

2026-09-20T14:30:00

Confidence score:

0–100

Example:

84.0

This is a score, NOT a probability.

Enums use uppercase values.

---

# 5. CASE

A case represents one investigation.

## Case object

{
  "caseId": 101,
  "caseName": "Operation Monsoon",
  "caseType": "DARK_WEB_INTELLIGENCE",
  "category": "THREAT_ACTOR_ATTRIBUTION",
  "description": "Controlled synthetic attribution investigation.",
  "status": "OPEN",
  "createdAt": "2026-09-20T10:00:00",
  "lastScanAt": null
}

## Fields

caseId
Unique case identifier.

caseName
Human-readable investigation name.

caseType
DARK_WEB_INTELLIGENCE

category
THREAT_ACTOR_ATTRIBUTION

description
Short case description.

status
Allowed values:

OPEN
PROCESSED
UNDER_REVIEW
CLOSED

createdAt
Case creation time.

lastScanAt
Time of latest controlled intelligence analysis.

This does not imply live continuous crawling.

---

# 6. CASE APIs

## Create case

POST

/api/cases

Request:

{
  "caseName": "Operation Monsoon",
  "caseType": "DARK_WEB_INTELLIGENCE",
  "category": "THREAT_ACTOR_ATTRIBUTION",
  "description": "Controlled synthetic attribution investigation."
}

Response:

{
  "caseId": 101,
  "caseName": "Operation Monsoon",
  "caseType": "DARK_WEB_INTELLIGENCE",
  "category": "THREAT_ACTOR_ATTRIBUTION",
  "description": "Controlled synthetic attribution investigation.",
  "status": "OPEN",
  "createdAt": "2026-09-20T10:00:00",
  "lastScanAt": null
}

## List cases

GET

/api/cases

## Get case

GET

/api/cases/{caseId}

---

# 7. KNOWN INDICATOR

The known indicator is the clue with which the investigation starts.

## Indicator object

{
  "indicatorId": 501,
  "caseId": 101,
  "indicatorType": "USERNAME",
  "indicatorValue": "r4v3n_mh",
  "description": "Known starting username."
}

## Supported indicator types

USERNAME
EMAIL
PGP_KEY
WALLET
DOMAIN
IP_ADDRESS
ONION_SERVICE
OTHER

---

# 8. INDICATOR API

## Register known indicator

POST

/api/cases/{caseId}/indicators

Request:

{
  "indicatorType": "USERNAME",
  "indicatorValue": "r4v3n_mh",
  "description": "Known starting username."
}

Response:

{
  "indicatorId": 501,
  "caseId": 101,
  "indicatorType": "USERNAME",
  "indicatorValue": "r4v3n_mh",
  "description": "Known starting username."
}

---

# 9. CONTROLLED INTELLIGENCE EXPANSION

The prototype does not perform unrestricted live dark-web crawling.

The investigation action analyzes controlled/synthetic intelligence and may
discover:

- related accounts
- emails
- PGP keys
- wallets
- marketplaces
- related handles
- domains
- onion services
- infrastructure
- certificates
- behavioural signals
- writing similarity
- activity-pattern similarity
- candidate relationships
- supporting or contradicting evidence

AI may assist extraction and correlation.

The final normalized results are stored by the backend.

---

# 10. INVESTIGATION API

POST

/api/cases/{caseId}/investigate

Request:

{
  "indicatorId": 501
}

Response:

{
  "status": "PROCESSED",
  "caseId": 101,
  "indicatorId": 501,
  "entitiesCreated": 6,
  "relationshipsCreated": 7,
  "evidenceCreated": 8,
  "confidenceCalculated": true
}

This action performs:

Known Indicator
→ Intelligence Expansion
→ Entity Extraction
→ Relationship Detection
→ Evidence Creation
→ Confidence Calculation

---

# 11. ENTITY

An entity is an observable object identified during the investigation.

## Entity object

{
  "entityId": 1,
  "entityType": "USERNAME",
  "entityValue": "r4v3n_mh",
  "description": "Known username supplied by investigator.",
  "discoveryConfidence": 0.95
}

## Required fields

entityId
entityType
entityValue
description
discoveryConfidence

discoveryConfidence is the confidence that the extraction correctly
identified the entity.

It is NOT attribution confidence.

---

# 12. ENTITY TYPES

Identity/account:

USERNAME
RELATED_HANDLE
EMAIL
ACCOUNT

Cryptographic:

PGP_KEY

Financial:

WALLET
TRANSACTION

Platform:

MARKETPLACE
FORUM

Infrastructure:

DOMAIN
IP_ADDRESS
ONION_SERVICE
SERVER
INFRASTRUCTURE
SSL_CERTIFICATE

Behaviour/persona:

BEHAVIOR_PATTERN
WRITING_SIGNATURE

---

# 13. ENTITY API

GET

/api/cases/{caseId}/entities

---

# 14. RELATIONSHIP

A relationship connects two entities.

## Relationship object

{
  "relationshipId": 301,
  "sourceEntityId": 1,
  "targetEntityId": 2,
  "relationshipType": "USES_EMAIL",
  "description": "The account is associated with the observed email.",
  "assessment": "OBSERVED"
}

## Required fields

relationshipId
sourceEntityId
targetEntityId
relationshipType
description
assessment

---

# 15. RELATIONSHIP TYPES

Identity/account:

USES_EMAIL
USES_PGP_KEY
LINKED_WALLET
ALIAS_OF
POSSIBLE_SAME_ACTOR
POSSIBLE_SAME_PERSONA

Marketplace:

ACTIVE_ON
REGISTERED_ON
CO_LISTED_WITH
MOVED_TO_PLATFORM

Cryptographic:

SHARES_PGP_KEY

Wallet:

SHARES_WALLET
TRANSACTED_WITH

Infrastructure:

HOSTED_ON
RESOLVES_TO
CERTIFICATE_MATCH
REUSES_INFRASTRUCTURE
LINKED_TO_CLEARNET

Behaviour/persona:

SIMILAR_WRITING
SIMILAR_BEHAVIOR
SIMILAR_ACTIVITY_PATTERN

---

# 16. RELATIONSHIP ASSESSMENT

Allowed values:

OBSERVED
INFERRED

OBSERVED:
Directly represented in the controlled intelligence.

INFERRED:
Produced through analysis/correlation and requires investigator review.

The frontend should visually distinguish the two.

---

# 17. RELATIONSHIP API

GET

/api/cases/{caseId}/relationships

---

# 18. EVIDENCE

Evidence explains why a relationship exists.

A relationship must not be treated as meaningful merely because an edge
exists.

## Evidence object

{
  "evidenceId": 401,
  "relationshipId": 301,
  "evidenceType": "EMAIL_MATCH",
  "source": "Synthetic Intelligence Record S-04",
  "description": "The same email address is associated with both observed accounts.",
  "strength": "HIGH",
  "reliability": "HIGH",
  "direction": "SUPPORTS",
  "independenceGroup": "IDENTITY_CONTACT",
  "observedAt": "2026-09-19T13:20:00"
}

---

# 19. EVIDENCE TYPES

Identity/account:

USERNAME_MATCH
EMAIL_MATCH
HANDLE_SIMILARITY
ACCOUNT_ASSOCIATION

Cryptographic:

PGP_KEY_MATCH
SHARED_PGP_KEY

Financial:

WALLET_MATCH
TRANSACTION_LINK

Infrastructure:

SERVER_STATUS_EXPOSURE
SSL_CERTIFICATE_MATCH
DEFAULT_SERVICE_BANNER
DESCRIPTOR_INCONSISTENCY
CLEARNET_INFRASTRUCTURE_MATCH
INFRASTRUCTURE_REUSE

Persona/behaviour:

STYLOMETRIC_SIMILARITY
BEHAVIOURAL_SIMILARITY
ACTIVITY_PATTERN_MATCH

Marketplace:

MARKETPLACE_OVERLAP
MARKETPLACE_MIGRATION
TRUST_LINK

Other:

SOURCE_CORROBORATION
CONTRADICTING_SIGNAL
MANUAL_NOTE

---

# 20. EVIDENCE STRENGTH

Allowed values:

LOW
MEDIUM
HIGH

Strength means how strongly the evidence supports or weakens the specific
relationship.

It is separate from source reliability.

---

# 21. SOURCE RELIABILITY

Allowed values:

LOW
MEDIUM
HIGH

Reliability means how trustworthy the source is considered.

Reliability and evidence strength must not be treated as the same value.

---

# 22. EVIDENCE DIRECTION

Allowed values:

SUPPORTS
WEAKENS
CONTRADICTS
NEUTRAL

SUPPORTS:
Supports the relationship.

WEAKENS:
Reduces support for the relationship.

CONTRADICTS:
Provides evidence against the relationship.

NEUTRAL:
Provides context without directly affecting the assessment.

---

# 23. INDEPENDENCE GROUP

independenceGroup identifies evidence that is based on the same underlying
fact.

The confidence engine should avoid fully double-counting multiple signals
from the same underlying fact.

---

# 24. EVIDENCE TIMESTAMP

observedAt is required.

It is used for:

- timeline
- recency
- report
- evidence drill-down

No separate timeline table is required.

---

# 25. EVIDENCE API

GET

/api/cases/{caseId}/evidence

Optional query parameters:

from
to

Example:

/api/cases/101/evidence?from=2026-09-01&to=2026-09-30

---

# 26. CONFIDENCE

Confidence represents the prototype's assessment of how strongly the
available evidence supports the attribution-related finding.

It is:

- explainable
- evidence-backed
- deterministic/rule-based for the prototype
- reviewable

It is NOT:

- probability
- identity proof
- scientifically calibrated accuracy

---

# 27. CONFIDENCE OBJECT

{
  "caseId": 101,
  "score": 84.0,
  "riskLevel": "HIGH",
  "explanation": "The assessment is supported by multiple independent evidence types.",
  "calculatedAt": "2026-09-20T10:15:00",
  "factors": [
    {
      "evidenceId": 401,
      "effect": "SUPPORTS",
      "contribution": 18.0,
      "reason": "High-strength, high-reliability email association."
    },
    {
      "evidenceId": 402,
      "effect": "SUPPORTS",
      "contribution": 21.0,
      "reason": "Shared PGP key provides a strong cryptographic signal."
    }
  ]
}

---

# 28. CONFIDENCE SCORE

Range:

0–100

Prototype interpretation:

0–49
LOW

50–74
MEDIUM

75–100
HIGH

These are prototype assessment thresholds.

They are not scientifically validated probability thresholds.

---

# 29. CONFIDENCE INPUTS

The confidence engine considers:

1. Evidence strength
2. Source reliability
3. Evidence direction
4. Evidence independence
5. Evidence recency

It must not simply count relationships.

Weak duplicated signals must not automatically create a high score.

---

# 30. PROTOTYPE SCORING RULE

Each evidence contribution is based on:

evidence type weight
× strength factor
× reliability factor
× recency factor
× independence factor
× direction factor

Strength:

LOW = 0.50
MEDIUM = 0.75
HIGH = 1.00

Reliability:

LOW = 0.50
MEDIUM = 0.75
HIGH = 1.00

Direction:

SUPPORTS = +1.00
WEAKENS = -0.50
CONTRADICTS = -1.00
NEUTRAL = 0.00

Recent evidence receives higher weight than older evidence.

Repeated evidence from the same independence group must be capped/reduced.

---

# 31. DEFAULT EVIDENCE WEIGHTS

PGP_KEY_MATCH / SHARED_PGP_KEY
= 25

WALLET_MATCH / TRANSACTION_LINK
= 20

EMAIL_MATCH / ACCOUNT_ASSOCIATION
= 20

INFRASTRUCTURE / CERTIFICATE MATCH
= 20

STYLOMETRIC / BEHAVIOURAL SIMILARITY
= 10

USERNAME / HANDLE / MARKETPLACE
= 5

These are illustrative prototype weights and are not scientifically
validated attribution weights.

---

# 32. CONFIDENCE API

POST

/api/cases/{caseId}/confidence/evaluate

Request:

{}

Response:

{
  "caseId": 101,
  "score": 84.0,
  "riskLevel": "HIGH",
  "explanation": "Assessment is supported by multiple independent evidence types.",
  "calculatedAt": "2026-09-20T10:15:00",
  "factors": []
}

---

# 33. GET CONFIDENCE

GET

/api/cases/{caseId}/confidence

Returns the latest stored confidence assessment.

---

# 34. CONFIDENCE UI REQUIREMENTS

The UI must display:

- score
- assessment level
- major supporting signals
- weakening/contradicting signals
- evidence used
- why the score changed
- calculated timestamp

Do not display:

"84% probability."

Display:

"84 attribution-confidence score."

---

# 35. ANALYST REVIEW

The investigator must be able to record the state of the generated finding.

## Review object

{
  "findingId": 701,
  "status": "FLAGGED",
  "note": "Review the wallet relationship before treating it as strong."
}

Allowed status:

PENDING
ACCEPTED
FLAGGED
REJECTED

---

# 36. REVIEW API

PATCH

/api/cases/{caseId}/review

Request:

{
  "status": "ACCEPTED",
  "note": "Evidence chain reviewed for the prototype."
}

Response:

{
  "findingId": 701,
  "status": "ACCEPTED",
  "note": "Evidence chain reviewed for the prototype."
}

The review must not modify or delete original evidence.

---

# 37. TIMELINE

The timeline is derived from:

evidence.observedAt
case.createdAt
case.lastScanAt

No separate timeline subsystem is required.

Timeline events should contain:

{
  "eventId": "EV401",
  "eventType": "EVIDENCE_OBSERVED",
  "description": "PGP key observed with the account.",
  "observedAt": "2026-09-18T12:00:00",
  "evidenceId": 401
}

The frontend sorts events chronologically.

---

# 38. GRAPH

The graph is a visualization of the same investigation data.

Entities = nodes

Relationships = edges

Evidence = explanation behind relationships

No graph database is required for the prototype.

The graph must:

- display the known indicator
- display related entities
- display relationships
- display relationship type
- distinguish observed/inferred relationships
- allow relationship selection
- open the evidence behind the selected relationship

---

# 39. GRAPH DATA

Node:

{
  "entityId": 1,
  "entityType": "USERNAME",
  "entityValue": "r4v3n_mh"
}

Edge:

{
  "relationshipId": 301,
  "sourceEntityId": 1,
  "targetEntityId": 2,
  "relationshipType": "USES_PGP_KEY",
  "assessment": "OBSERVED"
}

The frontend must never invent a graph edge that is absent from the backend.

---

# 40. STRESS TEST

Stress testing is a REQUIRED prototype feature.

Primary operation:

REMOVE ONE SIGNAL

The investigator selects an evidence record and temporarily removes it from
the confidence calculation.

Original database evidence remains unchanged.

---

# 41. STRESS TEST API

POST

/api/cases/{caseId}/stress-test

Request:

{
  "evidenceId": 402
}

Response:

{
  "caseId": 101,
  "removedEvidenceId": 402,
  "baselineScore": 84.0,
  "challengedScore": 61.0,
  "scoreChange": -23.0,
  "baselineRiskLevel": "HIGH",
  "challengedRiskLevel": "MEDIUM",
  "robustnessIndicator": 72.6,
  "explanation": "Removing the selected signal reduced the attribution-confidence assessment, but other independent signals continue to support the assessment."
}

---

# 42. ROBUSTNESS INDICATOR

Prototype definition:

robustnessIndicator =
(challengedScore / baselineScore) × 100

Example:

61 / 84 × 100 = 72.6

This is only a prototype robustness indicator.

It is NOT:

- model accuracy
- probability
- statistical confidence
- identity certainty

---

# 43. STRESS TEST RESET

POST

/api/cases/{caseId}/stress-test/reset

Response:

{
  "caseId": 101,
  "status": "RESET",
  "currentScore": 84.0,
  "currentRiskLevel": "HIGH"
}

Reset must restore the normal evidence-based assessment.

---

# 44. STRESS TEST RULES

Stress testing:

- must not delete evidence
- must not permanently alter evidence
- must not permanently alter the baseline score
- must not permanently alter relationships
- must explain the selected signal
- must show baseline score
- must show challenged score
- must show score difference
- must show changed assessment level
- must explain what evidence still supports the assessment
- must support reset

---

# 45. COMPOSITE INVESTIGATION API

GET

/api/cases/{caseId}/investigation

Optional:

from
to

Example:

/api/cases/101/investigation?from=2026-09-01&to=2026-09-30

This is the main integration endpoint for the frontend.

---

# 46. COMPOSITE INVESTIGATION RESPONSE

{
  "case": {
    "caseId": 101,
    "caseName": "Operation Monsoon",
    "caseType": "DARK_WEB_INTELLIGENCE",
    "category": "THREAT_ACTOR_ATTRIBUTION",
    "description": "Controlled synthetic attribution investigation.",
    "status": "UNDER_REVIEW",
    "createdAt": "2026-09-20T10:00:00",
    "lastScanAt": "2026-09-20T10:15:00"
  },

  "knownIndicator": {
    "indicatorId": 501,
    "indicatorType": "USERNAME",
    "indicatorValue": "r4v3n_mh",
    "description": "Known starting username."
  },

  "entities": [
    {
      "entityId": 1,
      "entityType": "USERNAME",
      "entityValue": "r4v3n_mh",
      "description": "Known starting username.",
      "discoveryConfidence": 1.0
    }
  ],

  "relationships": [
    {
      "relationshipId": 301,
      "sourceEntityId": 1,
      "targetEntityId": 2,
      "relationshipType": "USES_EMAIL",
      "description": "Account associated with observed email.",
      "assessment": "OBSERVED"
    }
  ],

  "evidence": [
    {
      "evidenceId": 401,
      "relationshipId": 301,
      "evidenceType": "EMAIL_MATCH",
      "source": "Synthetic Intelligence Record S-04",
      "description": "Same email observed in two account records.",
      "strength": "HIGH",
      "reliability": "HIGH",
      "direction": "SUPPORTS",
      "independenceGroup": "IDENTITY_CONTACT",
      "observedAt": "2026-09-19T13:20:00"
    }
  ],

  "confidence": {
    "caseId": 101,
    "score": 84.0,
    "riskLevel": "HIGH",
    "explanation": "Multiple independent evidence types support the assessment.",
    "calculatedAt": "2026-09-20T10:15:00",
    "factors": []
  },

  "review": {
    "findingId": 701,
    "status": "PENDING",
    "note": null
  },

  "timeline": [
    {
      "eventId": "EV401",
      "eventType": "EVIDENCE_OBSERVED",
      "description": "Email relationship observed.",
      "observedAt": "2026-09-19T13:20:00",
      "evidenceId": 401
    }
  ]
}

---

# 47. WHY THE COMPOSITE ENDPOINT EXISTS

The frontend should be able to load a complete case through one request:

case
+ known indicator
+ entities
+ relationships
+ evidence
+ confidence
+ review
+ timeline

Individual endpoints remain available for development/debugging.

---

# 48. REPORT

The final report is generated from the backend investigation state.

The report must contain:

1. Case information
2. Known indicator
3. Investigation summary
4. Discovered entities
5. Important relationships
6. Supporting evidence
7. Evidence sources
8. Evidence timestamps
9. Attribution-confidence score
10. Confidence explanation
11. Analyst review state
12. Stress-test result if performed
13. Timeline
14. Prototype limitations
15. Synthetic-data disclaimer

---

# 49. REPORT API

GET

/api/cases/{caseId}/report

Response:

{
  "caseId": 101,
  "caseName": "Operation Monsoon",
  "generatedAt": "2026-09-20T10:30:00",

  "summary": {
    "knownIndicator": "r4v3n_mh",
    "entityCount": 6,
    "relationshipCount": 7,
    "evidenceCount": 8,
    "confidenceScore": 84.0,
    "riskLevel": "HIGH",
    "reviewStatus": "ACCEPTED"
  },

  "assessment": {
    "explanation": "Multiple independent signals support the attribution assessment.",
    "limitations": [
      "Dataset is controlled/synthetic.",
      "Score is not a calibrated probability.",
      "Assessment is not proof of real-world identity."
    ]
  }
}

---

# 50. PDF EXPORT

GET

/api/cases/{caseId}/export/pdf

Returns:

PDF file

Suggested filename:

DAVIS-Case-{caseId}-Report.pdf

The PDF must contain the final case report.

---

# 51. CSV EXPORT

GET

/api/cases/{caseId}/export/csv

CSV should contain:

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

The CSV must represent the actual investigation data.

---

# 52. JSON EXPORT

GET

/api/cases/{caseId}/export/json

The JSON export must contain the complete investigation structure.

It should match the composite investigation response so that the exported
JSON remains directly traceable to the API data.

---

# 53. SOURCE TRACEABILITY

Every evidence record must provide:

source
evidenceType
description
observedAt
reliability

The UI must support the investigation chain:

Finding
→ Relationship
→ Evidence
→ Source
→ Confidence
→ Reasoning

---

# 54. THREE PROBLEM-STATEMENT AREAS

All three problem-statement capabilities use the same
entity/relationship/evidence model.

No separate software subsystem is required.

## Infrastructure / hidden-service correlation

Entities may include:

ONION_SERVICE
DOMAIN
IP_ADDRESS
SERVER
SSL_CERTIFICATE
INFRASTRUCTURE

Evidence may include:

SERVER_STATUS_EXPOSURE
SSL_CERTIFICATE_MATCH
DEFAULT_SERVICE_BANNER
DESCRIPTOR_INCONSISTENCY
CLEARNET_INFRASTRUCTURE_MATCH
INFRASTRUCTURE_REUSE

## Cross-marketplace actor mapping

Entities may include:

USERNAME
RELATED_HANDLE
MARKETPLACE
PGP_KEY
WALLET
EMAIL

Relationships may include:

ALIAS_OF
ACTIVE_ON
SHARED_PGP_KEY
SHARED_WALLET
USES_EMAIL
POSSIBLE_SAME_ACTOR

## Persona / behavioural linkage

Entities may include:

USERNAME
RELATED_HANDLE
WRITING_SIGNATURE
BEHAVIOR_PATTERN

Evidence may include:

STYLOMETRIC_SIMILARITY
BEHAVIOURAL_SIMILARITY
ACTIVITY_PATTERN_MATCH

Relationships may include:

SIMILAR_WRITING
SIMILAR_BEHAVIOR
SIMILAR_ACTIVITY_PATTERN
POSSIBLE_SAME_PERSONA

These are supporting signals only.

They are not proof of identity.

---

# 55. SYNTHETIC DATA LABEL

The frontend and generated report must clearly display:

"SYNTHETIC / CONTROLLED DATASET — PROTOTYPE DEMONSTRATION"

This must appear in:

- investigation results
- final report
- PDF export

The system must not imply that synthetic intelligence is live intelligence.

---

# 56. VALIDATION RULES

Case:

caseName is required.

Indicator:

indicatorType is required.
indicatorValue is required.

Entity:

entityType is required.
entityValue is required.

Relationship:

sourceEntityId is required.
targetEntityId is required.
relationshipType is required.

Evidence:

relationshipId is required.
evidenceType is required.
source is required.
description is required.
strength is required.
reliability is required.
direction is required.
observedAt is required.

Confidence:

score must be between 0 and 100.

Stress test:

evidenceId must belong to the requested case.

---

# 57. ERROR FORMAT

All API errors should use:

{
  "timestamp": "2026-09-20T10:30:00",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Indicator value is required.",
  "path": "/api/cases/101/indicators"
}

Common status codes:

200 OK
201 CREATED
400 BAD_REQUEST
404 NOT_FOUND
409 CONFLICT
500 INTERNAL_SERVER_ERROR

---

# 58. GOLDEN DEMO

The final prototype must successfully perform:

1. Create case:
   Operation Monsoon

2. Enter known indicator:
   r4v3n_mh

3. Trigger controlled intelligence expansion.

4. Extract entities such as:
   username
   email
   PGP key
   wallet
   marketplace
   related handle
   and/or infrastructure/persona entities where supported by the demo data.

5. Create relationships.

6. Attach multiple evidence types.

7. Open relationship graph.

8. Select an important relationship.

9. Open evidence drill-down.

10. Show:
    source
    evidence type
    strength
    reliability
    timestamp

11. Show attribution-confidence score.

12. Show score explanation.

13. Show timeline.

14. Record analyst review.

15. Select one important evidence signal.

16. Run stress test.

17. Show:
    baseline score
    challenged score
    score change
    changed assessment level
    robustness indicator
    explanation

18. Reset stress test.

19. Generate final case report.

20. Export:
    PDF
    CSV
    JSON

21. Clearly display:
    synthetic/controlled dataset
    attribution-confidence assessment
    not identity proof

---

# 59. DEFINITION OF DONE

The prototype is complete only when all of the following work:

[ ] Case creation works.

[ ] Known indicator registration works.

[ ] Controlled intelligence expansion works.

[ ] AI/intelligence can return normalized entities.

[ ] Relationships are created.

[ ] Evidence is attached to relationships.

[ ] Evidence contains source.

[ ] Evidence contains strength.

[ ] Evidence contains reliability.

[ ] Evidence contains direction.

[ ] Evidence contains timestamp.

[ ] Observed/inferred relationships can be distinguished.

[ ] Timeline is generated from timestamps.

[ ] Graph is generated from backend data.

[ ] Graph can display entities and relationships.

[ ] Relationship selection opens supporting evidence.

[ ] Confidence is calculated from stored evidence.

[ ] Confidence has explainable factors.

[ ] Confidence is shown as a score, not a probability.

[ ] Analyst review can be recorded.

[ ] Stress test can remove one signal temporarily.

[ ] Stress test shows baseline and challenged scores.

[ ] Stress test shows score change.

[ ] Stress test shows robustness indicator.

[ ] Stress test can be reset.

[ ] Original evidence remains unchanged after stress testing.

[ ] Final report is generated from backend data.

[ ] PDF export works.

[ ] CSV export works.

[ ] JSON export works.

[ ] Synthetic-data disclaimer appears.

[ ] Frontend does not depend on hard-coded investigation results.

[ ] One complete investigation works from beginning to end.

---

# 60. FINAL ARCHITECTURAL CONTRACT

The prototype has one investigation model:

CASE
↓
INDICATOR
↓
ENTITY
↓
RELATIONSHIP
↓
EVIDENCE
↓
CONFIDENCE
↓
REVIEW
↓
STRESS TEST
↓
REPORT / EXPORT

Graph = visualization of entities + relationships.

Timeline = visualization of evidence timestamps.

AI = extraction/correlation assistant.

Confidence = explainable evidence-based score.

Stress test = temporary evidence challenge.

Backend = single source of truth.

Investigator = final reviewer.

No additional subsystem is required for the agreed prototype.

DO NOT expand this contract with:

- live dark-web crawling
- collection-run management
- separate source-management subsystem
- graph database
- advanced identity resolution
- research-grade ML
- large-scale blockchain tracing
- image/location intelligence
- MFA/security infrastructure
- immutable audit infrastructure
- continuous monitoring
- multi-case actor evolution
- additional dashboards

unless the prototype scope is explicitly re-opened and this contract is
formally changed.