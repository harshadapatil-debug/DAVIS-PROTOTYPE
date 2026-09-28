from __future__ import annotations
from dataclasses import dataclass, field
from typing import Dict, List, Optional


@dataclass
class Record:
    idx: int
    source: str
    text: str
    observed_at: str
    reliability: str


@dataclass
class Entity:
    entityId: int
    entityType: str
    entityValue: str
    description: str
    discoveryConfidence: float

    def to_dict(self) -> Dict:
        return {
            "entityId": self.entityId,
            "entityType": self.entityType,
            "entityValue": self.entityValue,
            "description": self.description,
            "discoveryConfidence": round(self.discoveryConfidence, 2),
        }


@dataclass
class Mention:
    entity_id: int
    record_idx: int
    start: int
    end: int


@dataclass
class ExtractionResult:
    records: List[Record]
    entities: List[Entity]
    mentions: List[Mention]
    indicator_entity_id: Optional[int] = None


@dataclass
class SimilaritySignal:
    signalType: str            # STYLOMETRIC_SIMILARITY | BEHAVIOURAL_SIMILARITY | ACTIVITY_PATTERN_MATCH
    a: int                     # entity id (handle)
    b: int                     # entity id (handle)
    score: float               # analytical signal, NOT a probability
    direction: str             # SUPPORTS | CONTRADICTS
    strength: str              # LOW | MEDIUM | HIGH
    description: str
    records: List[Record] = field(default_factory=list)


@dataclass
class EvidenceDraft:
    evidenceType: str
    source: str
    description: str
    strength: str
    reliability: str
    direction: str
    independenceGroup: str
    observedAt: str


@dataclass
class RelDraft:
    source: int
    target: int
    relationshipType: str
    description: str
    assessment: str
    evidence: List[EvidenceDraft] = field(default_factory=list)
