# DAVIS — Unified AIML Layer (Sanskruti + Vasudha = ONE pipeline)

One modular pipeline, one entry point, one normalized output:

```
Controlled intelligence → Entity extraction → Similarity analysis (text / stylometric / behavioural / activity)
→ Relationship detection (marketplace / infrastructure / persona) → Evidence normalisation
→ Contract validation → { caseId, entities, candidateRelationships, evidence }
```

**Synthetic / controlled data only.** No crawling, no Tor deanonymisation, no identity claims, and **no
attribution-confidence score** (that belongs to the backend confidence engine). Similarity values and
`discoveryConfidence` are analytical signals, never probabilities.

## Layout
```
aiml/
  contract.py                 enums from API_CONTRACT.md + validate_output()
  models.py                   dataclasses
  entity_extraction/          extractor.py           (Sanskruti)
  similarity/                 text_similarity.py, stylometric.py, behavioural.py, analyzer.py   (Sanskruti)
  relationship_detection/     marketplace.py, infrastructure.py, persona.py, detector.py       (Vasudha)
  evidence/                   normalizer.py          single canonical evidence schema for both sides
  api/                        pipeline.py (run_pipeline), service.py (CLI + HTTP boundary)
data/synthetic_intelligence.json   demo input (case 101, indicator r4v3n_mh)
tests/test_pipeline.py             unit + end-to-end tests
```

## Run
```bash
python -m aiml.api.service --input data/synthetic_intelligence.json            # prints JSON
python -m aiml.api.service --serve --port 8090                                 # POST /aiml/analyze
python -m unittest discover -s tests -v                                        # 22 tests
```
Python entry point: `from aiml import run_pipeline; out = run_pipeline(payload)`.

## Input (API_CONTRACT §12, plus optional fields)
```json
{"caseId":101,"indicatorId":501,"indicatorType":"USERNAME","indicatorValue":"r4v3n_mh",
 "intelligence":[{"source":"Synthetic Intelligence Record S-01","text":"...",
                  "observedAt":"2026-09-18T09:00:00","reliability":"HIGH"}]}
```
`observedAt` (ISO-8601) and `reliability` (LOW/MEDIUM/HIGH) per record are optional; defaults are the
payload `referenceTime` (or "now") and MEDIUM. Evidence inherits source, timestamp and reliability from the
records it is derived from.

## What is detected
* **Entities**: USERNAME, RELATED_HANDLE, EMAIL, PGP_KEY, WALLET, MARKETPLACE, FORUM, DOMAIN, IP_ADDRESS,
  ONION_SERVICE, SSL_CERTIFICATE — only what literally appears in the text.
* **Similarity signals** → evidence `STYLOMETRIC_SIMILARITY`, `BEHAVIOURAL_SIMILARITY`,
  `ACTIVITY_PATTERN_MATCH`; a contradicting activity window becomes `CONTRADICTING_SIGNAL` (direction
  `CONTRADICTS`) attached to POSSIBLE_SAME_* candidates.
* **Relationships**: USES_EMAIL, USES_PGP_KEY, LINKED_WALLET, ACTIVE_ON, REGISTERED_ON, MOVED_TO_PLATFORM,
  ALIAS_OF (explicit claims), SHARES_PGP_KEY, SHARES_WALLET, POSSIBLE_SAME_ACTOR, CERTIFICATE_MATCH,
  RESOLVES_TO, HOSTED_ON, LINKED_TO_CLEARNET, REUSES_INFRASTRUCTURE, SIMILAR_WRITING, SIMILAR_BEHAVIOR,
  SIMILAR_ACTIVITY_PATTERN, POSSIBLE_SAME_PERSONA. Directly stated links are `OBSERVED`; anything derived by
  correlation or similarity is `INFERRED` (enforced by the validator).
* **Independence groups** are assigned per underlying fact (e.g. `CRYPTOGRAPHIC_PGP`, `IDENTITY_CONTACT`,
  `FINANCIAL_WALLET`, `INFRASTRUCTURE_CERT`) so the confidence engine can avoid double counting.

## Failure rule
Malformed records are skipped; a failing similarity or correlation module is logged and the run continues
with the remaining evidence. Only invalid input (`caseId` missing) or a contract bug raises.
