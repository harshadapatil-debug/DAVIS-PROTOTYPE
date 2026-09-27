"""
behaviour_similarity.py
------------------------
Compares structured behavioural indicators between two controlled
intelligence records:

    - postingFrequencyPerWeek  (how often the actor posts)
    - avgResponseTimeHours     (how quickly the actor replies)
    - activeHoursUTC           (which hours of day the actor is active)

Each feature is compared with a simple, explainable method, and the
three sub-scores are averaged into one behavioural similarity score.
This is a SIGNAL to support analysis, not proof of shared identity.
"""

from typing import Dict, List


def _normalized_difference(a: float, b: float, scale: float) -> float:
    """
    Return a similarity in [0, 1] based on how close two numbers are,
    relative to `scale` (an expected typical range for that feature).
    A difference of 0 -> similarity 1.0. A difference >= scale -> 0.0.
    """
    if a is None or b is None:
        return 0.0
    diff = abs(a - b)
    similarity = 1.0 - min(diff / scale, 1.0)
    return similarity


def _jaccard(set_a: List[int], set_b: List[int]) -> float:
    """Set overlap between two lists of active hours (0-23)."""
    sa, sb = set(set_a or []), set(set_b or [])
    if not sa and not sb:
        return 0.0
    union = sa | sb
    intersection = sa & sb
    if not union:
        return 0.0
    return len(intersection) / len(union)


def compute_behaviour_similarity(behaviour_a: Dict, behaviour_b: Dict) -> float:
    """
    Return an overall behavioural similarity score in [0, 1], combining:
      - posting-frequency closeness
      - average-response-time closeness
      - active-hours overlap (Jaccard index)
    """
    if not behaviour_a or not behaviour_b:
        return 0.0

    freq_sim = _normalized_difference(
        behaviour_a.get("postingFrequencyPerWeek"),
        behaviour_b.get("postingFrequencyPerWeek"),
        scale=20.0,  # typical spread seen in the synthetic dataset
    )
    response_sim = _normalized_difference(
        behaviour_a.get("avgResponseTimeHours"),
        behaviour_b.get("avgResponseTimeHours"),
        scale=12.0,
    )
    hours_sim = _jaccard(
        behaviour_a.get("activeHoursUTC"),
        behaviour_b.get("activeHoursUTC"),
    )

    overall = (freq_sim + response_sim + hours_sim) / 3.0
    return round(float(overall), 2)


def build_behaviour_similarity_evidence(record_a: Dict, record_b: Dict) -> Dict:
    """
    Compare the `behaviour` field of two records and return a normalized
    similarity/evidence signal.
    """
    behaviour_a = record_a.get("behaviour", {})
    behaviour_b = record_b.get("behaviour", {})
    score = compute_behaviour_similarity(behaviour_a, behaviour_b)

    return {
        "type": "BEHAVIOURAL_SIMILARITY",
        "score": score,
        "description": (
            f"The observed behavioural patterns (posting frequency, "
            f"response time, active hours) for {record_a.get('recordId')} "
            f"and {record_b.get('recordId')} show a similarity of "
            f"{score:.2f}. This is a behavioural signal only."
        ),
        "sourceRecordIds": [record_a.get("recordId"), record_b.get("recordId")],
    }
