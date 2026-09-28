# INTEGRATION_REPORT — Unified AIML layer

## Status vs. success criteria
| # | Criterion | Result |
|---|-----------|--------|
| 1 | Entity extraction works | PASS (13 entities on demo data) |
| 2 | Similarity modules work | PASS (text, stylometric, behavioural, activity) |
| 3 | Relationship detection works | PASS (marketplace, infrastructure, persona) |
| 4 | Supporting evidence generated | PASS (33 evidence records, 25 relationships) |
| 5 | ONE canonical evidence schema | PASS — single `evidence/normalizer.py` |
| 6 | Entities + relationships + evidence in ONE output | PASS |
| 7 | Follows API_CONTRACT.md | PASS with 2 additive fields (see below) |
| 8 | Relationship/evidence IDs linked | PASS (validated both directions) |
| 9 | Automated tests | PASS — 22 tests, stdlib only, no network |
| 10 | Consumable by Java backend | PASS — one JSON in, one JSON out |

## Integration mechanism (backend owner decides; no new backend endpoint is created)
Preferred: the Spring Boot `POST /api/cases/{caseId}/investigate` service builds the §12 input from the
indicator + stored synthetic records and calls the AIML boundary once:
* HTTP: `python -m aiml.api.service --serve` → `POST http://127.0.0.1:8090/aiml/analyze`, or
* Process: `python -m aiml.api.service --input in.json --output out.json` via `ProcessBuilder`.

The backend then persists entities/relationships/evidence, runs the confidence engine, and returns the
`{status, entitiesCreated, relationshipsCreated, evidenceCreated, confidenceCalculated}` response.

## Items that need Integration Owner approval (contract is frozen)
1. **`candidateRelationships[].relationshipId` and `.evidenceIds`** are additive fields. The team brief asks
   that relationships "contain references to their evidence"; the frozen contract has no such field. Jackson in
   Spring Boot ignores unknown properties by default, but confirm before relying on them. `evidence[].relationshipId`
   alone is sufficient to link the two.
2. **IDs are local to the AIML output** (entities 1..n, relationships 1..n, evidence 1..n). The backend assigns
   canonical IDs on persistence (contract §13) and must remap `sourceEntityId`, `targetEntityId` and
   `evidence.relationshipId` consistently.
3. **Optional input fields** `observedAt` and `reliability` per intelligence record. Without them evidence
   timestamps default to "now" and reliability to MEDIUM, which weakens the recency factor in the scoring rule.
   Recommend adding them to `synthetic_intelligence.json`.
4. **No WRITING_SIGNATURE / BEHAVIOR_PATTERN entities are emitted.** The contract has no relationship type
   linking a handle to such an entity, so they would render as orphan graph nodes. Persona signals are carried
   as SIMILAR_* relationships between handle entities instead.

## Known limitations (honest scope)
* Extraction is regex/rule based; it recognises the patterns in the synthetic data format (emails, PGP `0x` keys,
  BTC/ETH/XMR-style wallets, `.onion`, TLS fingerprints, IPs, common TLDs, cue-phrase handles/platforms).
  Free-form real-world text will need extra rules or a pretrained NER model behind the same interface.
* Handle detection relies on underscore tokens, `@handle`, `by <handle>:` and cue words with digits.
* Attribution of an identifier to a handle uses nearest-preceding-handle in the record; ambiguous cases are
  down-graded one strength level (LOW) rather than guessed at HIGH.
* Similarity thresholds (`aiml/config.py`) were set against the small synthetic set to separate the demo pair from
  the decoy; they are **not validated** and are signal cut-offs, not probabilities. Stylometry needs ≥30 words per
  handle.
* Activity windows are read from stated text ("posts between 01:00 and 05:00 UTC"), not from raw timestamps.
* `ALIAS_OF` is produced only from explicit statements in the intelligence.
* Determinism: output is identical for identical input when records carry `observedAt` or the payload sets
  `referenceTime`.
