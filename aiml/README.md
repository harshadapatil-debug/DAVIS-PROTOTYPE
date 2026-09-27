# DAVIS — AIML MODULE

## PURPOSE

The AIML module supports the DAVIS investigation by analyzing controlled /
synthetic intelligence and producing structured investigation signals.

AIML is an ASSISTIVE ANALYSIS LAYER.

AIML does NOT make the final attribution decision.

---

# AIML RESPONSIBILITIES

The AIML module has three main responsibilities:

1. ENTITY EXTRACTION
2. SIMILARITY / BEHAVIOURAL ANALYSIS
3. RELATIONSHIP CANDIDATE DETECTION

The output of AIML is passed to the backend, where it becomes part of the
official investigation data.

---

# AIML FLOW

CONTROLLED INTELLIGENCE
        ↓
ENTITY EXTRACTION
        ↓
SIMILARITY / BEHAVIOURAL ANALYSIS
        ↓
RELATIONSHIP CANDIDATES
        ↓
SUPPORTING EVIDENCE
        ↓
BACKEND
        ↓
CONFIDENCE ENGINE
        ↓
INVESTIGATOR REVIEW

---

# FOLDER STRUCTURE

aiml/

├── README.md
├── requirements.txt
│
├── entity_extraction/
│   └── entity_extractor.py
│
├── similarity/
│   ├── text_similarity.py
│   └── behavior_similarity.py
│
├── relationship_detection/
│   └── relationship_detector.py
│
└── api/
    └── main.py

---

# INPUT

AIML receives controlled/synthetic intelligence related to a case.

Example:

{
  "caseId": 101,
  "indicatorId": 501,
  "indicatorType": "USERNAME",
  "indicatorValue": "r4v3n_mh",
  "intelligence": [
    {
      "source": "Synthetic Intelligence Record S-01",
      "text": "Account r4v3n_mh was active on DarkBazaar."
    },
    {
      "source": "Synthetic Intelligence Record S-02",
      "text": "The account used PGP key 0xA1B2C3D4."
    }
  ]
}

---

# ENTITY EXTRACTION

AIML identifies observable entities from the supplied intelligence.

Possible entity types:

USERNAME
RELATED_HANDLE
EMAIL
ACCOUNT
PGP_KEY
WALLET
TRANSACTION
MARKETPLACE
FORUM
DOMAIN
IP_ADDRESS
ONION_SERVICE
SERVER
INFRASTRUCTURE
SSL_CERTIFICATE
BEHAVIOR_PATTERN
WRITING_SIGNATURE

Example output:

{
  "entityType": "PGP_KEY",
  "entityValue": "0xA1B2C3D4",
  "description": "PGP key mentioned in the intelligence record.",
  "discoveryConfidence": 0.92
}

AIML MUST NOT invent an entity that is not supported by the input.

---

# SIMILARITY ANALYSIS

AIML may identify similarity between controlled accounts/personas.

Supported prototype signals include:

STYLOMETRIC_SIMILARITY
BEHAVIOURAL_SIMILARITY
ACTIVITY_PATTERN_MATCH

Example:

{
  "type": "STYLOMETRIC_SIMILARITY",
  "score": 0.88,
  "description": "The controlled text samples show stylistic similarity."
}

IMPORTANT:

A similarity score is NOT an attribution probability.

0.88 similarity does NOT mean:

"88% probability that both accounts belong to the same person."

It is only an analytical signal.

---

# RELATIONSHIP CANDIDATES

AIML may suggest relationships between extracted entities.

Example:

{
  "sourceEntityId": 1,
  "targetEntityId": 6,
  "relationshipType": "SIMILAR_WRITING",
  "description": "The writing samples show stylistic similarity.",
  "assessment": "INFERRED"
}

AI-generated relationships should normally use:

INFERRED

because they are analytical findings rather than directly observed facts.

---

# SUPPORTING EVIDENCE

Every important AIML-generated relationship must have supporting evidence.

Example:

{
  "evidenceType": "STYLOMETRIC_SIMILARITY",
  "source": "Synthetic Intelligence Record S-03",
  "description": "Text samples from the two accounts show similar writing characteristics.",
  "strength": "MEDIUM",
  "reliability": "MEDIUM",
  "direction": "SUPPORTS",
  "independenceGroup": "BEHAVIORAL",
  "observedAt": "2026-09-19T14:00:00"
}

The backend will use this evidence for the investigation and confidence
calculation.

---

# INFRASTRUCTURE SIGNALS

When supported by the controlled intelligence, AIML may identify:

ONION_SERVICE
DOMAIN
IP_ADDRESS
SERVER
SSL_CERTIFICATE
INFRASTRUCTURE

Possible evidence types:

SERVER_STATUS_EXPOSURE
SSL_CERTIFICATE_MATCH
DEFAULT_SERVICE_BANNER
DESCRIPTOR_INCONSISTENCY
CLEARNET_INFRASTRUCTURE_MATCH
INFRASTRUCTURE_REUSE

---

# CROSS-MARKETPLACE SIGNALS

AIML may correlate:

USERNAME
RELATED_HANDLE
EMAIL
PGP_KEY
WALLET
MARKETPLACE

Possible relationships:

ALIAS_OF
ACTIVE_ON
SHARED_PGP_KEY
SHARED_WALLET
USES_EMAIL
POSSIBLE_SAME_ACTOR

---

# PERSONA SIGNALS

AIML may produce supporting signals involving:

- writing style
- behaviour
- activity patterns

Possible relationships:

SIMILAR_WRITING
SIMILAR_BEHAVIOR
SIMILAR_ACTIVITY_PATTERN
POSSIBLE_SAME_PERSONA

These are supporting signals only.

They are not identity proof.

---

# AIML CONFIDENCE VS DAVIS CONFIDENCE

These are different things.

AIML may produce:

ENTITY EXTRACTION CONFIDENCE
SIMILARITY SCORE
RELATIONSHIP DETECTION CONFIDENCE

DAVIS separately produces:

ATTRIBUTION-CONFIDENCE SCORE

The AIML values must NOT be treated as the final attribution-confidence
score.

---

# OUTPUT CONTRACT

AIML should return normalized results in this general structure:

{
  "caseId": 101,
  "entities": [],
  "candidateRelationships": [],
  "evidence": []
}

The exact fields MUST follow:

docs/API_CONTRACT.md

API_CONTRACT.md is the authoritative integration contract.

---

# IMPORTANT RULES

AIML MUST:

- use controlled/synthetic intelligence
- return normalized data
- provide supporting evidence
- use API_CONTRACT.md field names
- keep entity extraction separate from attribution decision
- handle missing/uncertain information safely
- avoid inventing unsupported entities or relationships

AIML MUST NOT:

- claim identity proof
- make the final attribution decision
- create a final confidence score for the case
- build unrestricted live dark-web crawling
- build research-grade stylometry
- build advanced graph ML
- build large-scale blockchain analytics
- add unrelated ML features

---

# TEAM SPLIT

ENTITY + SIMILARITY:

entity_extraction/
similarity/

Main responsibilities:
- entity extraction
- text similarity
- stylometric similarity
- behavioural/activity similarity

RELATIONSHIP + INTELLIGENCE:

relationship_detection/

Main responsibilities:
- candidate relationship detection
- infrastructure signals
- cross-marketplace correlation
- persona relationship signals
- supporting evidence

---

# BEFORE CODING

Read:

1. ../docs/PROJECT_RULES.md
2. ../docs/API_CONTRACT.md
3. ../README.md
4. This README
5. data/intelligence/synthetic_intelligence.json

Then start coding only inside the AIML area assigned to you.

If a required change affects the API contract:

STOP → ASK THE INTEGRATION OWNER → UPDATE THE CONTRACT → THEN CODE.

---

# FINAL RULE

AIML is an analysis assistant.

The pipeline is:

INTELLIGENCE
→ AI-ASSISTED EXTRACTION
→ ENTITIES
→ RELATIONSHIPS
→ EVIDENCE
→ BACKEND
→ CONFIDENCE
→ INVESTIGATOR REVIEW

AI does not directly decide who the actor is.