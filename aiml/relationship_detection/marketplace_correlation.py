"""
marketplace_correlation.py
----------------------------
Cross-marketplace correlation.

Compares usernames/handles, PGP keys, wallets and marketplaces across
DIFFERENT controlled intelligence records to find candidate connections
between actors who may be operating on more than one marketplace.

All relationships produced here are INFERRED: they require comparing two
or more separate records, as opposed to reading a single record's stated
facts (which is what relationship_detector.detect_observed_relationships
handles).

We deliberately do NOT claim "these are the same person" - shared strong
identifiers (PGP key, wallet) produce a SHARED_* relationship; when
MULTIPLE independent strong identifiers line up, we additionally raise a
POSSIBLE_SAME_ACTOR hypothesis, always attached to its supporting
evidence so an investigator can see exactly why it was suggested.
"""

from itertools import combinations
from typing import Dict, List, Tuple

from aiml.entity_extraction.entity_extractor import make_entity_id
from aiml.relationship_detection.common import new_relationship, new_evidence


def _shared_pgp_key(record_a: Dict, record_b: Dict) -> bool:
    return bool(record_a.get("pgpKey")) and record_a.get("pgpKey") == record_b.get("pgpKey")


def _shared_wallet(record_a: Dict, record_b: Dict) -> bool:
    return bool(record_a.get("wallet")) and record_a.get("wallet") == record_b.get("wallet")


def detect_marketplace_correlations(records: List[Dict]) -> Tuple[List[Dict], List[Dict]]:
    """
    Compare every pair of records for shared PGP keys / wallets across
    (typically) different marketplaces, and escalate to
    POSSIBLE_SAME_ACTOR when more than one independent strong identifier
    is shared between the same pair of usernames.

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
        record_ids = [record_a["recordId"], record_b["recordId"]]

        shared_strong_identifiers = 0  # count of independent strong signals shared

        if _shared_pgp_key(record_a, record_b):
            shared_strong_identifiers += 1
            pgp_evidence = new_evidence(
                evidence_type="SHARED_PGP_KEY_MATCH",
                description=(
                    f"{username_a} ({record_a['recordId']}) and {username_b} "
                    f"({record_b['recordId']}) are recorded using the same "
                    f"PGP key ({record_a['pgpKey']})."
                ),
                source_record_ids=record_ids,
                score=1.0,
                direction="SUPPORTS",
            )
            evidence.append(pgp_evidence)
            relationships.append(new_relationship(
                username_a_id, username_b_id, "SHARED_PGP_KEY",
                f"{username_a} and {username_b} are linked by a shared PGP key "
                f"across {record_a['recordId']} and {record_b['recordId']}.",
                "INFERRED",
                evidence_ids=[pgp_evidence["evidenceId"]],
            ))

        if _shared_wallet(record_a, record_b):
            shared_strong_identifiers += 1
            wallet_evidence = new_evidence(
                evidence_type="SHARED_WALLET_MATCH",
                description=(
                    f"{username_a} ({record_a['recordId']}) and {username_b} "
                    f"({record_b['recordId']}) are recorded using the same "
                    f"wallet address ({record_a['wallet']})."
                ),
                source_record_ids=record_ids,
                score=1.0,
                direction="SUPPORTS",
            )
            evidence.append(wallet_evidence)
            relationships.append(new_relationship(
                username_a_id, username_b_id, "SHARED_WALLET",
                f"{username_a} and {username_b} are linked by a shared wallet "
                f"address across {record_a['recordId']} and {record_b['recordId']}.",
                "INFERRED",
                evidence_ids=[wallet_evidence["evidenceId"]],
            ))

        # Escalate to a POSSIBLE_SAME_ACTOR hypothesis only when more than
        # one independent strong identifier lines up across different
        # marketplaces. This is still a hypothesis for investigator
        # review, never a determination of identity.
        if shared_strong_identifiers >= 2 and record_a.get("marketplace") != record_b.get("marketplace"):
            evidence_ids = [
                e["evidenceId"] for e in evidence
                if set(e["supportingRecordIds"]) == set(record_ids)
            ]
            relationships.append(new_relationship(
                username_a_id, username_b_id, "POSSIBLE_SAME_ACTOR",
                f"{username_a} on {record_a.get('marketplace')} and {username_b} "
                f"on {record_b.get('marketplace')} share multiple independent "
                f"strong identifiers (PGP key and/or wallet). This is an "
                f"analytical hypothesis for investigator review, not a "
                f"determination of identity.",
                "INFERRED",
                evidence_ids=evidence_ids,
            ))

    return relationships, evidence
