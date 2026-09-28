"""Contract enums (copied from API_CONTRACT.md) and the output validator."""
import json
import re
from datetime import datetime
from typing import Dict, List

ENTITY_TYPES = {
    "USERNAME", "RELATED_HANDLE", "EMAIL", "ACCOUNT", "PGP_KEY", "WALLET", "TRANSACTION",
    "MARKETPLACE", "FORUM", "DOMAIN", "IP_ADDRESS", "ONION_SERVICE", "SERVER",
    "INFRASTRUCTURE", "SSL_CERTIFICATE", "BEHAVIOR_PATTERN", "WRITING_SIGNATURE",
}
RELATIONSHIP_TYPES = {
    "USES_EMAIL", "USES_PGP_KEY", "LINKED_WALLET", "ALIAS_OF", "POSSIBLE_SAME_ACTOR",
    "POSSIBLE_SAME_PERSONA", "ACTIVE_ON", "REGISTERED_ON", "CO_LISTED_WITH",
    "MOVED_TO_PLATFORM", "SHARES_PGP_KEY", "SHARES_WALLET", "TRANSACTED_WITH", "HOSTED_ON",
    "RESOLVES_TO", "CERTIFICATE_MATCH", "REUSES_INFRASTRUCTURE", "LINKED_TO_CLEARNET",
    "SIMILAR_WRITING", "SIMILAR_BEHAVIOR", "SIMILAR_ACTIVITY_PATTERN",
}
EVIDENCE_TYPES = {
    "USERNAME_MATCH", "EMAIL_MATCH", "HANDLE_SIMILARITY", "ACCOUNT_ASSOCIATION",
    "PGP_KEY_MATCH", "SHARED_PGP_KEY", "WALLET_MATCH", "TRANSACTION_LINK",
    "SERVER_STATUS_EXPOSURE", "SSL_CERTIFICATE_MATCH", "DEFAULT_SERVICE_BANNER",
    "DESCRIPTOR_INCONSISTENCY", "CLEARNET_INFRASTRUCTURE_MATCH", "INFRASTRUCTURE_REUSE",
    "STYLOMETRIC_SIMILARITY", "BEHAVIOURAL_SIMILARITY", "ACTIVITY_PATTERN_MATCH",
    "MARKETPLACE_OVERLAP", "MARKETPLACE_MIGRATION", "TRUST_LINK",
    "SOURCE_CORROBORATION", "CONTRADICTING_SIGNAL", "MANUAL_NOTE",
}
STRENGTHS = {"LOW", "MEDIUM", "HIGH"}
RELIABILITIES = {"LOW", "MEDIUM", "HIGH"}
DIRECTIONS = {"SUPPORTS", "WEAKENS", "CONTRADICTS", "NEUTRAL"}
ASSESSMENTS = {"OBSERVED", "INFERRED"}

# Relationship types that are analytical conclusions and therefore may never be OBSERVED.
ALWAYS_INFERRED = {
    "POSSIBLE_SAME_ACTOR", "POSSIBLE_SAME_PERSONA", "SHARES_PGP_KEY", "SHARES_WALLET",
    "REUSES_INFRASTRUCTURE", "LINKED_TO_CLEARNET", "SIMILAR_WRITING", "SIMILAR_BEHAVIOR",
    "SIMILAR_ACTIVITY_PATTERN",
}

ENTITY_FIELDS = ["entityId", "entityType", "entityValue", "description", "discoveryConfidence"]
EVIDENCE_FIELDS = ["evidenceId", "relationshipId", "evidenceType", "source", "description",
                   "strength", "reliability", "direction", "independenceGroup", "observedAt"]
REL_FIELDS = ["relationshipId", "sourceEntityId", "targetEntityId", "relationshipType",
              "description", "assessment"]
FORBIDDEN_KEYS = {"score", "confidence", "attributionConfidence", "riskLevel", "probability"}
FORBIDDEN_LANGUAGE = re.compile(
    r"same person|identified the hacker|is the hacker|\bproves?\b|proof of|probab|\d\s*%|"
    r"belongs? to the same|deanonymi[sz]",
    re.I,
)


class ContractError(Exception):
    pass


def _iso(v) -> bool:
    try:
        datetime.fromisoformat(v)
        return True
    except Exception:
        return False


def validate_output(out: Dict) -> List[str]:
    """Return a list of contract violations (empty list == valid)."""
    errs: List[str] = []
    if not isinstance(out, dict):
        return ["output is not an object"]
    for k in ("caseId", "entities", "candidateRelationships", "evidence"):
        if k not in out:
            errs.append(f"missing top-level key {k}")
    if errs:
        return errs
    for k in out:
        if k in FORBIDDEN_KEYS:
            errs.append(f"AIML must not emit final score key '{k}'")
    try:
        json.dumps(out)
    except Exception as exc:
        errs.append(f"not JSON serialisable: {exc}")

    ent_ids = set()
    for e in out["entities"]:
        for f in ENTITY_FIELDS:
            if e.get(f) in (None, ""):
                errs.append(f"entity {e.get('entityId')} missing {f}")
        if e.get("entityType") not in ENTITY_TYPES:
            errs.append(f"entity {e.get('entityId')} bad type {e.get('entityType')}")
        dc = e.get("discoveryConfidence")
        if not isinstance(dc, (int, float)) or not 0 <= dc <= 1:
            errs.append(f"entity {e.get('entityId')} discoveryConfidence out of range")
        if e.get("entityId") in ent_ids:
            errs.append(f"duplicate entityId {e.get('entityId')}")
        ent_ids.add(e.get("entityId"))
        for k in e:
            if k in FORBIDDEN_KEYS:
                errs.append(f"entity has forbidden key {k}")
        if FORBIDDEN_LANGUAGE.search(str(e.get("description", ""))):
            errs.append(f"entity {e.get('entityId')} description contains forbidden language")

    ev_ids, ev_by_rel = set(), {}
    for v in out["evidence"]:
        for f in EVIDENCE_FIELDS:
            if v.get(f) in (None, ""):
                errs.append(f"evidence {v.get('evidenceId')} missing {f}")
        if v.get("evidenceType") not in EVIDENCE_TYPES:
            errs.append(f"evidence {v.get('evidenceId')} bad evidenceType")
        if v.get("strength") not in STRENGTHS:
            errs.append(f"evidence {v.get('evidenceId')} bad strength")
        if v.get("reliability") not in RELIABILITIES:
            errs.append(f"evidence {v.get('evidenceId')} bad reliability")
        if v.get("direction") not in DIRECTIONS:
            errs.append(f"evidence {v.get('evidenceId')} bad direction")
        if not _iso(v.get("observedAt", "")):
            errs.append(f"evidence {v.get('evidenceId')} observedAt not ISO-8601")
        if v.get("evidenceId") in ev_ids:
            errs.append(f"duplicate evidenceId {v.get('evidenceId')}")
        ev_ids.add(v.get("evidenceId"))
        ev_by_rel.setdefault(v.get("relationshipId"), []).append(v["evidenceId"])
        if FORBIDDEN_LANGUAGE.search(str(v.get("description", ""))):
            errs.append(f"evidence {v.get('evidenceId')} description contains forbidden language")
        for k in v:
            if k in FORBIDDEN_KEYS:
                errs.append(f"evidence has forbidden key {k}")

    rel_ids = set()
    for r in out["candidateRelationships"]:
        rid = r.get("relationshipId")
        for f in REL_FIELDS:
            if r.get(f) in (None, ""):
                errs.append(f"relationship {rid} missing {f}")
        if r.get("relationshipType") not in RELATIONSHIP_TYPES:
            errs.append(f"relationship {rid} bad type {r.get('relationshipType')}")
        if r.get("assessment") not in ASSESSMENTS:
            errs.append(f"relationship {rid} bad assessment")
        if r.get("relationshipType") in ALWAYS_INFERRED and r.get("assessment") != "INFERRED":
            errs.append(f"relationship {rid} ({r.get('relationshipType')}) must be INFERRED")
        for side in ("sourceEntityId", "targetEntityId"):
            if r.get(side) not in ent_ids:
                errs.append(f"relationship {rid} {side} references unknown entity")
        if r.get("sourceEntityId") == r.get("targetEntityId"):
            errs.append(f"relationship {rid} is a self-loop")
        if rid in rel_ids:
            errs.append(f"duplicate relationshipId {rid}")
        rel_ids.add(rid)
        listed = r.get("evidenceIds", [])
        if not listed:
            errs.append(f"relationship {rid} has no supporting evidence")
        for evid in listed:
            if evid not in ev_ids:
                errs.append(f"relationship {rid} references unknown evidence {evid}")
        if sorted(listed) != sorted(ev_by_rel.get(rid, [])):
            errs.append(f"relationship {rid} evidenceIds do not match evidence.relationshipId links")
        if FORBIDDEN_LANGUAGE.search(str(r.get("description", ""))):
            errs.append(f"relationship {rid} description contains forbidden language")
        for k in r:
            if k in FORBIDDEN_KEYS:
                errs.append(f"relationship has forbidden key {k}")
    for rid in ev_by_rel:
        if rid not in rel_ids:
            errs.append(f"evidence references unknown relationship {rid}")
    return errs
