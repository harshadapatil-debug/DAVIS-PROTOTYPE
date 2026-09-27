"""
common.py
----------
Shared helpers for the relationship_detection package. Kept in one place
so relationship_detector.py and the three correlation modules
(marketplace / infrastructure / persona) all build relationship and
evidence objects the exact same way, with deterministic IDs.
"""

import hashlib
from typing import Dict, List, Optional


def make_relationship_id(source_id: str, target_id: str, rel_type: str) -> str:
    """Deterministic relationship ID from (source, target, type)."""
    digest = hashlib.md5(f"{source_id}:{target_id}:{rel_type}".encode("utf-8")).hexdigest()[:8]
    return f"REL-{digest}"


def make_evidence_id(evidence_type: str, source_record_ids: List[str]) -> str:
    """Deterministic evidence ID from (type, sorted source record ids)."""
    key = f"{evidence_type}:" + ",".join(sorted(source_record_ids or []))
    digest = hashlib.md5(key.encode("utf-8")).hexdigest()[:8]
    return f"EVD-{digest}"


def new_relationship(source_entity_id: str, target_entity_id: str,
                      relationship_type: str, description: str,
                      assessment: str, evidence_ids: Optional[List[str]] = None) -> Dict:
    """
    Build one normalized candidate relationship.

    assessment must be "OBSERVED" (directly stated by a single record) or
    "INFERRED" (produced via correlation/analysis across records).
    """
    assert assessment in ("OBSERVED", "INFERRED"), "assessment must be OBSERVED or INFERRED"
    return {
        "relationshipId": make_relationship_id(source_entity_id, target_entity_id, relationship_type),
        "sourceEntityId": source_entity_id,
        "targetEntityId": target_entity_id,
        "relationshipType": relationship_type,
        "description": description,
        "assessment": assessment,
        "evidenceIds": evidence_ids or [],
    }


def new_evidence(evidence_type: str, description: str,
                  source_record_ids: List[str], score: Optional[float] = None,
                  direction: str = "SUPPORTS") -> Dict:
    """
    Build one normalized evidence object.

    direction must be one of SUPPORTS / WEAKENS / CONTRADICTS / NEUTRAL.
    """
    assert direction in ("SUPPORTS", "WEAKENS", "CONTRADICTS", "NEUTRAL"), (
        "direction must be SUPPORTS, WEAKENS, CONTRADICTS or NEUTRAL"
    )
    return {
        "evidenceId": make_evidence_id(evidence_type, source_record_ids),
        "evidenceType": evidence_type,
        "description": description,
        "score": score,
        "sourceReference": ", ".join(source_record_ids or []),
        "direction": direction,
        "supportingRecordIds": source_record_ids or [],
    }


def evidence_from_similarity_signal(signal: Dict, supports_threshold: float = 0.5) -> Dict:
    """
    Convert a raw similarity-module output (the {"type", "score",
    "description", "sourceRecordIds"} shape produced by
    aiml/similarity/*.py) into a fully normalized evidence object.
    """
    score = signal.get("score", 0.0) or 0.0
    direction = "SUPPORTS" if score >= supports_threshold else "NEUTRAL"
    return new_evidence(
        evidence_type=signal["type"],
        description=signal["description"],
        source_record_ids=signal.get("sourceRecordIds", []),
        score=score,
        direction=direction,
    )
