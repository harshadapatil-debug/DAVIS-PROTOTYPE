"""
activity_similarity.py
------------------------
Compares activity TIMING patterns between two controlled intelligence
records, using their `activityTimestamps` lists (ISO-8601 strings).

Method (explainable, deterministic):
    1. Convert each timestamp to an hour-of-day (0-23) and a
       day-of-week (0-6).
    2. Build a normalized histogram over hour-of-day for each record.
    3. Compare the two histograms with cosine similarity.
    4. Blend in day-of-week overlap (Jaccard) as a secondary signal.

This produces a SIMILAR_ACTIVITY_PATTERN signal - a timing-pattern
similarity, not proof that the two records belong to the same actor.
"""

from datetime import datetime
from typing import Dict, List
import numpy as np


def _parse_timestamps(timestamps: List[str]) -> List[datetime]:
    parsed = []
    for ts in timestamps or []:
        try:
            parsed.append(datetime.fromisoformat(ts.replace("Z", "+00:00")))
        except (ValueError, AttributeError):
            continue
    return parsed


def _hour_histogram(timestamps: List[datetime]) -> np.ndarray:
    hist = np.zeros(24)
    for dt in timestamps:
        hist[dt.hour] += 1
    total = hist.sum()
    if total == 0:
        return hist
    return hist / total


def _cosine(a: np.ndarray, b: np.ndarray) -> float:
    norm_a = np.linalg.norm(a)
    norm_b = np.linalg.norm(b)
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return float(np.dot(a, b) / (norm_a * norm_b))


def _weekday_jaccard(timestamps_a: List[datetime], timestamps_b: List[datetime]) -> float:
    days_a = {dt.weekday() for dt in timestamps_a}
    days_b = {dt.weekday() for dt in timestamps_b}
    if not days_a and not days_b:
        return 0.0
    union = days_a | days_b
    if not union:
        return 0.0
    return len(days_a & days_b) / len(union)


def compute_activity_similarity(timestamps_a: List[str], timestamps_b: List[str]) -> float:
    """
    Return an activity-timing similarity score in [0, 1], combining
    hour-of-day histogram similarity with day-of-week overlap.
    """
    parsed_a = _parse_timestamps(timestamps_a)
    parsed_b = _parse_timestamps(timestamps_b)

    if not parsed_a or not parsed_b:
        return 0.0

    hour_sim = _cosine(_hour_histogram(parsed_a), _hour_histogram(parsed_b))
    weekday_sim = _weekday_jaccard(parsed_a, parsed_b)

    overall = (hour_sim * 0.7) + (weekday_sim * 0.3)
    return round(float(overall), 2)


def build_activity_similarity_evidence(record_a: Dict, record_b: Dict) -> Dict:
    """
    Compare the `activityTimestamps` field of two records and return a
    normalized similarity/evidence signal.
    """
    ts_a = record_a.get("activityTimestamps", [])
    ts_b = record_b.get("activityTimestamps", [])
    score = compute_activity_similarity(ts_a, ts_b)

    return {
        "type": "SIMILAR_ACTIVITY_PATTERN",
        "score": score,
        "description": (
            f"The observed activity timestamps for {record_a.get('recordId')} "
            f"and {record_b.get('recordId')} show a temporal similarity of "
            f"{score:.2f} (hour-of-day and day-of-week patterns). This is "
            f"a timing signal only."
        ),
        "sourceRecordIds": [record_a.get("recordId"), record_b.get("recordId")],
    }
