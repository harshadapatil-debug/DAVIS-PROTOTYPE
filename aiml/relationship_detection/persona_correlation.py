"""
persona_correlation.py
------------------------
Persona correlation.

Combines the three similarity modules (text, stylometric, behavioural,
activity) into candidate persona-level relationships between different
usernames:

    SIMILAR_WRITING           - text + stylometric similarity combined
    SIMILAR_BEHAVIOR          - behavioural similarity
    SIMILAR_ACTIVITY_PATTERN  - activity/timing similarity

If enough of these individually-similar signals line up for the same
pair of usernames, a higher-level POSSIBLE_SAME_PERSONA hypothesis is
raised, always citing every piece of supporting evidence.

This module NEVER claims proof of common authorship/identity - it always
frames its output as an analytical hypothesis for investigator review.

Thresholds below are fixed, documented constants (not learned), so the
reasoning behind every relationship can be explained in a viva.
"""

from itertools import combinations
from typing import Dict, List, Tuple

from aiml.entity_extraction.entity_extractor import make_entity_id
from aiml.relationship_detection.common import new_relationship, evidence_from_similarity_signal
from aiml.similarity.text_similarity import build_text_similarity_evidence
from aiml.similarity.stylometric_similarity import build_stylometric_similarity_evidence
from aiml.similarity.behaviour_similarity import build_behaviour_similarity_evidence
from aiml.similarity.activity_similarity import build_activity_similarity_evidence

# Fixed similarity thresholds used to decide whether a signal is "similar
# enough" to justify a candidate relationship. Chosen to be simple,
# round numbers rather than tuned/learned values.
WRITING_SIMILARITY_THRESHOLD = 0.5     # average of text + stylometric score
BEHAVIOUR_SIMILARITY_THRESHOLD = 0.5
ACTIVITY_SIMILARITY_THRESHOLD = 0.5

# Number of individually-similar persona signals required before we raise
# the higher-level POSSIBLE_SAME_PERSONA hypothesis.
PERSONA_SIGNAL_COUNT_FOR_HYPOTHESIS = 2


def detect_persona_correlations(records: List[Dict]) -> Tuple[List[Dict], List[Dict]]:
    """
    Compare every pair of records for writing, behavioural and activity
    similarity, and build SIMILAR_WRITING / SIMILAR_BEHAVIOR /
    SIMILAR_ACTIVITY_PATTERN relationships plus a POSSIBLE_SAME_PERSONA
    hypothesis when enough of them agree.

    Returns (relationships, evidence).
    """
    relationships: List[Dict] = []
    evidence: List[Dict] = []

    for record_a, record_b in combinations(records, 2):
        username_a, username_b = record_a.get("username"), record_b.get("username")
        if not username_a or not username_b or username_a == username_b:
            continue

        username_a_id = make_entity_id("USERNAME", username_a)
        username_b_id = make_entity_id("USERNAME", username_b)

        persona_evidence_ids: List[str] = []
        signals_matched = 0

        # --- Writing similarity: TEXT + STYLOMETRIC combined ---
        text_signal = build_text_similarity_evidence(record_a, record_b)
        style_signal = build_stylometric_similarity_evidence(record_a, record_b)
        text_evidence = evidence_from_similarity_signal(text_signal)
        style_evidence = evidence_from_similarity_signal(style_signal)
        evidence.extend([text_evidence, style_evidence])

        writing_score = round((text_signal["score"] + style_signal["score"]) / 2.0, 2)
        if writing_score >= WRITING_SIMILARITY_THRESHOLD:
            signals_matched += 1
            writing_evidence_ids = [text_evidence["evidenceId"], style_evidence["evidenceId"]]
            persona_evidence_ids.extend(writing_evidence_ids)
            relationships.append(new_relationship(
                username_a_id, username_b_id, "SIMILAR_WRITING",
                f"{username_a} ({record_a['recordId']}) and {username_b} "
                f"({record_b['recordId']}) show combined text and "
                f"stylometric writing similarity of {writing_score:.2f}. "
                f"This is a stylistic signal, not proof of common authorship.",
                "INFERRED",
                evidence_ids=writing_evidence_ids,
            ))

        # --- Behavioural similarity ---
        behaviour_signal = build_behaviour_similarity_evidence(record_a, record_b)
        behaviour_evidence = evidence_from_similarity_signal(behaviour_signal)
        evidence.append(behaviour_evidence)

        if behaviour_signal["score"] >= BEHAVIOUR_SIMILARITY_THRESHOLD:
            signals_matched += 1
            persona_evidence_ids.append(behaviour_evidence["evidenceId"])
            relationships.append(new_relationship(
                username_a_id, username_b_id, "SIMILAR_BEHAVIOR",
                f"{username_a} ({record_a['recordId']}) and {username_b} "
                f"({record_b['recordId']}) show behavioural similarity of "
                f"{behaviour_signal['score']:.2f}. This is a behavioural "
                f"signal, not proof of common identity.",
                "INFERRED",
                evidence_ids=[behaviour_evidence["evidenceId"]],
            ))

        # --- Activity/timing similarity ---
        activity_signal = build_activity_similarity_evidence(record_a, record_b)
        activity_evidence = evidence_from_similarity_signal(activity_signal)
        evidence.append(activity_evidence)

        if activity_signal["score"] >= ACTIVITY_SIMILARITY_THRESHOLD:
            signals_matched += 1
            persona_evidence_ids.append(activity_evidence["evidenceId"])
            relationships.append(new_relationship(
                username_a_id, username_b_id, "SIMILAR_ACTIVITY_PATTERN",
                f"{username_a} ({record_a['recordId']}) and {username_b} "
                f"({record_b['recordId']}) show activity-timing similarity "
                f"of {activity_signal['score']:.2f}. This is a timing "
                f"signal, not proof of common identity.",
                "INFERRED",
                evidence_ids=[activity_evidence["evidenceId"]],
            ))

        # --- Escalate to POSSIBLE_SAME_PERSONA hypothesis ---
        if signals_matched >= PERSONA_SIGNAL_COUNT_FOR_HYPOTHESIS:
            relationships.append(new_relationship(
                username_a_id, username_b_id, "POSSIBLE_SAME_PERSONA",
                f"{username_a} ({record_a['recordId']}) and {username_b} "
                f"({record_b['recordId']}) show {signals_matched} "
                f"independent persona-level similarity signals (writing, "
                f"behaviour and/or activity timing). This is an analytical "
                f"hypothesis for investigator review, not a determination "
                f"of identity.",
                "INFERRED",
                evidence_ids=sorted(set(persona_evidence_ids)),
            ))

    return relationships, evidence
