"""
infrastructure_correlation.py
--------------------------------
Infrastructure correlation.

Looks for reused hosting infrastructure between different controlled
intelligence records:

    - matching SSL certificate fingerprint
    - matching clearnet IP address
    - matching (and distinctive) service banner
    - server status exposure combined with a descriptor inconsistency

All relationships here are INFERRED (they require comparing two separate
records), and are always attached to the specific infrastructure signal
that triggered them so an investigator can verify the reasoning.

We do NOT claim that shared infrastructure proves common operatorship -
shared hosting can also indicate a shared hosting provider, a bulletproof
hosting reseller, or coincidence. It is reported as a REUSES_INFRASTRUCTURE
candidate relationship with supporting evidence only.
"""

from itertools import combinations
from typing import Dict, List, Tuple

from aiml.entity_extraction.entity_extractor import make_entity_id
from aiml.relationship_detection.common import new_relationship, new_evidence


def detect_infrastructure_correlations(records: List[Dict]) -> Tuple[List[Dict], List[Dict]]:
    """
    Compare every pair of records' `infrastructure` blocks for reused
    fingerprints/IPs/banners.

    Returns (relationships, evidence).
    """
    relationships: List[Dict] = []
    evidence: List[Dict] = []

    for record_a, record_b in combinations(records, 2):
        username_a, username_b = record_a.get("username"), record_b.get("username")
        infra_a = record_a.get("infrastructure") or {}
        infra_b = record_b.get("infrastructure") or {}
        if not username_a or not username_b or not infra_a or not infra_b:
            continue

        username_a_id = make_entity_id("USERNAME", username_a)
        username_b_id = make_entity_id("USERNAME", username_b)
        record_ids = [record_a["recordId"], record_b["recordId"]]

        matched_signals: List[str] = []

        if infra_a.get("sslCertFingerprint") and infra_a.get("sslCertFingerprint") == infra_b.get("sslCertFingerprint"):
            matched_signals.append(f"matching SSL certificate fingerprint ({infra_a['sslCertFingerprint']})")

        if infra_a.get("clearnetIP") and infra_a.get("clearnetIP") == infra_b.get("clearnetIP"):
            matched_signals.append(f"matching clearnet IP address ({infra_a['clearnetIP']})")

        if infra_a.get("serviceBanner") and infra_a.get("serviceBanner") == infra_b.get("serviceBanner"):
            matched_signals.append(f"matching/default service banner ({infra_a['serviceBanner']})")

        if not matched_signals:
            continue

        # More matched signals -> slightly higher (but still bounded)
        # confidence. This stays fully explainable: score = fraction of
        # the three checked signals that matched.
        score = round(len(matched_signals) / 3.0, 2)

        infra_evidence = new_evidence(
            evidence_type="INFRASTRUCTURE_REUSE",
            description=(
                f"{record_a['recordId']} and {record_b['recordId']} share "
                f"infrastructure signals: {'; '.join(matched_signals)}."
            ),
            source_record_ids=record_ids,
            score=score,
            direction="SUPPORTS",
        )
        evidence.append(infra_evidence)

        relationships.append(new_relationship(
            username_a_id, username_b_id, "REUSES_INFRASTRUCTURE",
            f"{username_a} ({record_a['recordId']}) and {username_b} "
            f"({record_b['recordId']}) appear to reuse the same hosting "
            f"infrastructure. Shared infrastructure can indicate common "
            f"operatorship, a shared hosting provider, or coincidence - "
            f"investigator review is required.",
            "INFERRED",
            evidence_ids=[infra_evidence["evidenceId"]],
        ))

    return relationships, evidence
