"""Similarity analysis: turns handle-level text/behaviour/activity data into similarity signals."""
import itertools
import logging
import re
from collections import defaultdict
from typing import Dict, List, Set

from .. import config
from ..entity_extraction.extractor import HANDLE_TYPES
from ..models import ExtractionResult, SimilaritySignal
from ..relationship_detection.common import Ctx
from .behavioural import activity_windows, behaviour_tags, jaccard
from .stylometric import stylometric_similarity
from .text_similarity import TfidfIndex, tokenize

log = logging.getLogger("aiml.similarity")

SAMPLE_RE = re.compile(
    r"(?:post|message|listing|review|writing sample|sample|thread)\s+by\s+@?([A-Za-z0-9_-]{3,32})\s*:\s*"
    r"[\"“](.+?)[\"”]", re.I | re.S)


def _strength(score: float, thresholds) -> str:
    lo, med, hi = thresholds
    return "HIGH" if score >= hi else "MEDIUM" if score >= med else "LOW" if score >= lo else ""


def analyze(ex: ExtractionResult, ctx: Ctx) -> List[SimilaritySignal]:
    handles = {e.entityId: e for e in ex.entities if e.entityType in HANDLE_TYPES}
    by_value = {e.entityValue.lower(): e.entityId for e in handles.values()}
    ent = ctx.entities

    samples: Dict[int, List[str]] = defaultdict(list)
    sample_recs = defaultdict(list)
    tags: Dict[int, Set[str]] = defaultdict(set)
    tag_recs = defaultdict(list)
    windows: Dict[int, Set[int]] = defaultdict(set)
    win_recs = defaultdict(list)

    for rec in ex.records:
        for m in SAMPLE_RE.finditer(rec.text):
            hid = by_value.get(m.group(1).lower())
            if hid:
                samples[hid].append(m.group(2))
                sample_recs[hid].append(rec)
        for pos, tag in behaviour_tags(rec.text):
            owners, _ = ctx.owners(rec.idx, pos)
            for hid in owners:
                tags[hid].add(tag)
                tag_recs[hid].append(rec)
        for pos, hours in activity_windows(rec.text):
            owners, _ = ctx.owners(rec.idx, pos)
            for hid in owners:
                windows[hid] |= hours
                win_recs[hid].append(rec)

    def uniq(recs):
        seen, out = set(), []
        for r in recs:
            if r.idx not in seen:
                seen.add(r.idx)
                out.append(r)
        return out

    signals: List[SimilaritySignal] = []

    # ---- text + stylometric ---------------------------------------------------------------
    corp = {h: " ".join(t) for h, t in samples.items() if len(tokenize(" ".join(t))) >= config.MIN_SAMPLE_WORDS}
    if len(corp) >= 2:
        idx = TfidfIndex(corp)
        for a, b in itertools.combinations(sorted(corp), 2):
            try:
                score, comp = stylometric_similarity(corp[a], corp[b], idx.similarity(a, b))
            except Exception:
                log.exception("stylometric failure for %s/%s", a, b)
                continue
            st = _strength(score, config.STYLO_THRESHOLDS)
            if st:
                signals.append(SimilaritySignal(
                    "STYLOMETRIC_SIMILARITY", a, b, score, "SUPPORTS", st,
                    f"Controlled text samples from {ent[a].entityValue} and {ent[b].entityValue} show "
                    f"stylistic similarity (signal {score:.2f}; character-pattern {comp['char_trigram']:.2f}, "
                    f"function-word {comp['function_words']:.2f}, style-features {comp['style_features']:.2f}).",
                    uniq(sample_recs[a] + sample_recs[b])))

    # ---- behavioural ----------------------------------------------------------------------
    for a, b in itertools.combinations(sorted(tags), 2):
        shared = tags[a] & tags[b]
        score = round(jaccard(tags[a], tags[b]), 3)
        st = _strength(score, config.BEHAV_THRESHOLDS)
        if len(shared) >= config.BEHAV_MIN_SHARED_TAGS and st:
            names = ", ".join(sorted(t.replace("SIGNOFF:", "sign-off '") + ("'" if t.startswith("SIGNOFF:") else "")
                                     for t in shared))
            signals.append(SimilaritySignal(
                "BEHAVIOURAL_SIMILARITY", a, b, score, "SUPPORTS", st,
                f"{ent[a].entityValue} and {ent[b].entityValue} share behavioural habits: {names} "
                f"(overlap signal {score:.2f}).", uniq(tag_recs[a] + tag_recs[b])))

    # ---- activity pattern -----------------------------------------------------------------
    for a, b in itertools.combinations(sorted(windows), 2):
        score = round(jaccard(windows[a], windows[b]), 3)
        recs = uniq(win_recs[a] + win_recs[b])
        st = _strength(score, config.ACTIVITY_THRESHOLDS)
        if st:
            signals.append(SimilaritySignal(
                "ACTIVITY_PATTERN_MATCH", a, b, score, "SUPPORTS", st,
                f"Reported daily activity windows for {ent[a].entityValue} and {ent[b].entityValue} "
                f"overlap (overlap signal {score:.2f}).", recs))
        elif score <= config.ACTIVITY_CONTRADICTION:
            signals.append(SimilaritySignal(
                "ACTIVITY_PATTERN_MATCH", a, b, score, "CONTRADICTS", "MEDIUM",
                f"Reported daily activity windows for {ent[a].entityValue} and {ent[b].entityValue} "
                f"barely overlap (overlap signal {score:.2f}).", recs))
    return signals
