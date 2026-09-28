import re
from collections import defaultdict
from typing import Dict, List, Tuple

from ..entity_extraction.extractor import HANDLE_TYPES
from ..models import Entity, EvidenceDraft, ExtractionResult, Record

_ORDER = {"LOW": 0, "MEDIUM": 1, "HIGH": 2}
GROUP_CUE_RE = re.compile(r"\b(?:both|shared|same|common)\b", re.I)


def min_level(levels: List[str]) -> str:
    return min(levels, key=lambda x: _ORDER[x]) if levels else "MEDIUM"


def lower(strength: str) -> str:
    return {"HIGH": "MEDIUM", "MEDIUM": "LOW", "LOW": "LOW"}[strength]


class Ctx:
    """Read-only view over extraction output with owner resolution for identifiers."""

    def __init__(self, ex: ExtractionResult):
        self.ex = ex
        self.records = ex.records
        self.entities: Dict[int, Entity] = {e.entityId: e for e in ex.entities}
        self.by_record = defaultdict(list)
        for m in ex.mentions:
            self.by_record[m.record_idx].append(m)
        for lst in self.by_record.values():
            lst.sort(key=lambda m: m.start)

    def etype(self, eid: int) -> str:
        return self.entities[eid].entityType

    def name(self, eid: int) -> str:
        return self.entities[eid].entityValue

    def handle_mentions(self, ridx: int):
        return [m for m in self.by_record[ridx] if self.etype(m.entity_id) in HANDLE_TYPES]

    def owners(self, ridx: int, pos: int) -> Tuple[List[int], bool]:
        """Handle entity ids an item at `pos` belongs to, and whether that attribution is ambiguous."""
        hs = self.handle_mentions(ridx)
        ids = list(dict.fromkeys(m.entity_id for m in hs))
        if not ids:
            return [], False
        if len(ids) == 1:
            return ids, False
        if GROUP_CUE_RE.search(self.records[ridx].text):
            return ids, False
        before = [m for m in hs if m.end <= pos]
        if before:
            return [before[-1].entity_id], True
        return [min(hs, key=lambda m: abs(m.start - pos)).entity_id], True


def make_evidence(etype: str, recs: List[Record], description: str, strength: str,
                  group: str, direction: str = "SUPPORTS") -> EvidenceDraft:
    seen, sources = set(), []
    for r in recs:
        if r.source not in seen:
            seen.add(r.source)
            sources.append(r.source)
    return EvidenceDraft(
        evidenceType=etype,
        source="; ".join(sources),
        description=description,
        strength=strength,
        reliability=min_level([r.reliability for r in recs]),
        direction=direction,
        independenceGroup=group,
        observedAt=max(r.observed_at for r in recs),
    )
