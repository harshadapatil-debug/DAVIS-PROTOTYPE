"""Evidence normalisation: merges relationship drafts, assigns IDs, links evidence <-> relationships
and emits the canonical DAVIS evidence / relationship schema."""
import logging
from typing import Dict, List, Tuple

from ..models import Entity, RelDraft

log = logging.getLogger("aiml.evidence")

SYMMETRIC = {
    "SHARES_PGP_KEY", "SHARES_WALLET", "POSSIBLE_SAME_ACTOR", "POSSIBLE_SAME_PERSONA",
    "SIMILAR_WRITING", "SIMILAR_BEHAVIOR", "SIMILAR_ACTIVITY_PATTERN", "REUSES_INFRASTRUCTURE",
}


def normalize(entities: List[Entity], drafts: List[RelDraft]) -> Tuple[List[Dict], List[Dict]]:
    valid_ids = {e.entityId for e in entities}
    merged: Dict[tuple, RelDraft] = {}
    for d in drafts:
        if d.source not in valid_ids or d.target not in valid_ids or d.source == d.target:
            log.warning("dropping relationship with invalid endpoints %s->%s", d.source, d.target)
            continue
        if not d.evidence:
            log.warning("dropping %s relationship without evidence", d.relationshipType)
            continue
        src, tgt = d.source, d.target
        if d.relationshipType in SYMMETRIC and src > tgt:
            src, tgt = tgt, src
        key = (src, tgt, d.relationshipType)
        cur = merged.get(key)
        if cur is None:
            merged[key] = RelDraft(src, tgt, d.relationshipType, d.description, d.assessment, list(d.evidence))
        else:
            cur.evidence.extend(d.evidence)
            if d.assessment == "INFERRED":
                cur.assessment = "INFERRED"

    relationships, evidence = [], []
    for rid, d in enumerate(merged.values(), 1):
        seen, ids = set(), []
        for ev in d.evidence:
            k = (ev.evidenceType, ev.source, ev.description)
            if k in seen:
                continue
            seen.add(k)
            evid = len(evidence) + 1
            ids.append(evid)
            evidence.append({
                "evidenceId": evid, "relationshipId": rid, "evidenceType": ev.evidenceType,
                "source": ev.source, "description": ev.description, "strength": ev.strength,
                "reliability": ev.reliability, "direction": ev.direction,
                "independenceGroup": ev.independenceGroup, "observedAt": ev.observedAt,
            })
        relationships.append({
            "relationshipId": rid, "sourceEntityId": d.source, "targetEntityId": d.target,
            "relationshipType": d.relationshipType, "description": d.description,
            "assessment": d.assessment, "evidenceIds": ids,
        })
    return relationships, evidence
