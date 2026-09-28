"""Persona / behaviour correlation built on similarity signals."""
from collections import defaultdict
from typing import List

from ..models import RelDraft, SimilaritySignal
from .common import Ctx, make_evidence

SIGNAL_MAP = {
    "STYLOMETRIC_SIMILARITY": ("SIMILAR_WRITING", "BEHAVIORAL"),
    "BEHAVIOURAL_SIMILARITY": ("SIMILAR_BEHAVIOR", "BEHAVIORAL_HABITS"),
    "ACTIVITY_PATTERN_MATCH": ("SIMILAR_ACTIVITY_PATTERN", "ACTIVITY_PATTERN"),
}


def detect(ctx: Ctx, signals: List[SimilaritySignal], prior: List[RelDraft]) -> List[RelDraft]:
    drafts: List[RelDraft] = []
    support = defaultdict(list)
    contra = defaultdict(list)
    for s in signals:
        key = tuple(sorted((s.a, s.b)))
        (support if s.direction == "SUPPORTS" else contra)[key].append(s)

    def ev(s: SimilaritySignal):
        return make_evidence(s.signalType, s.records, s.description, s.strength, SIGNAL_MAP[s.signalType][1],
                             "SUPPORTS")

    def contra_ev(s: SimilaritySignal):
        return make_evidence("CONTRADICTING_SIGNAL", s.records, s.description, s.strength,
                             "ACTIVITY_PATTERN_CONTRA", "CONTRADICTS")

    for (a, b), sigs in support.items():
        for s in sigs:
            rtype, _ = SIGNAL_MAP[s.signalType]
            drafts.append(RelDraft(a, b, rtype, s.description, "INFERRED", [ev(s)]))
        if len({s.signalType for s in sigs}) >= 2:
            drafts.append(RelDraft(
                a, b, "POSSIBLE_SAME_PERSONA",
                f"Multiple independent persona signals link {ctx.name(a)} and {ctx.name(b)} as candidate "
                f"same-persona accounts; requires investigator review.", "INFERRED", [ev(s) for s in sigs]))

    # contradicting signals weaken any same-actor / same-persona candidate for the pair
    for pair, sigs in contra.items():
        for d in drafts + prior:
            if d.relationshipType in ("POSSIBLE_SAME_PERSONA", "POSSIBLE_SAME_ACTOR") \
                    and tuple(sorted((d.source, d.target))) == pair:
                d.evidence.extend(contra_ev(s) for s in sigs)
    return drafts
