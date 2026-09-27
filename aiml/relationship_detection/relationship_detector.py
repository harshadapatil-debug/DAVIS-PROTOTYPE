"""
relationship_detector.py
--------------------------
Builds candidate relationships between entities.

Two categories of relationship are produced:

  OBSERVED  - directly supported by a single controlled intelligence
              record (e.g. a record explicitly states that a username
              uses a given PGP key). No correlation/analysis required.

  INFERRED  - produced by comparing MULTIPLE records/entities via the
              correlation modules (marketplace, infrastructure, persona).
              These always require supporting evidence and must never be
              described as proof of identity.

This module owns the OBSERVED relationships (built directly from a single
record) and orchestrates the three INFERRED correlation modules found in
this package (marketplace_correlation, infrastructure_correlation,
persona_correlation), merging everything into one candidate-relationship
list plus a matching evidence list.
"""

from typing import Dict, List, Tuple

from aiml.entity_extraction.entity_extractor import make_entity_id
from aiml.relationship_detection.common import new_relationship
from aiml.relationship_detection.marketplace_correlation import (
    detect_marketplace_correlations,
)
from aiml.relationship_detection.infrastructure_correlation import (
    detect_infrastructure_correlations,
)
from aiml.relationship_detection.persona_correlation import (
    detect_persona_correlations,
)


# ---------------------------------------------------------------------------
# OBSERVED relationships (single record, directly stated by the data)
# ---------------------------------------------------------------------------
def detect_observed_relationships(record: Dict) -> List[Dict]:
    """
    Build relationships that are DIRECTLY supported by a single controlled
    intelligence record - no correlation or inference is involved, so
    these are marked OBSERVED.
    """
    relationships: List[Dict] = []
    rid = record.get("recordId")
    username = record.get("username")
    if not username:
        return relationships

    username_id = make_entity_id("USERNAME", username)

    if record.get("email"):
        email_id = make_entity_id("EMAIL", record["email"])
        relationships.append(new_relationship(
            username_id, email_id, "USES_EMAIL",
            f"{username} is directly recorded as using this email address in {rid}.",
            "OBSERVED",
        ))

    if record.get("pgpKey"):
        pgp_id = make_entity_id("PGP_KEY", record["pgpKey"])
        relationships.append(new_relationship(
            username_id, pgp_id, "USES_PGP_KEY",
            f"{username} is directly recorded as using this PGP key in {rid}.",
            "OBSERVED",
        ))

    if record.get("wallet"):
        wallet_id = make_entity_id("WALLET", record["wallet"])
        relationships.append(new_relationship(
            username_id, wallet_id, "LINKED_WALLET",
            f"{username} is directly recorded as linked to this wallet in {rid}.",
            "OBSERVED",
        ))

    if record.get("marketplace"):
        marketplace_id = make_entity_id("MARKETPLACE", record["marketplace"])
        relationships.append(new_relationship(
            username_id, marketplace_id, "ACTIVE_ON",
            f"{username} is directly recorded as active on this marketplace in {rid}.",
            "OBSERVED",
        ))

    # A record explicitly listing `relatedHandles` is a DIRECT statement
    # from the controlled intelligence, so it is OBSERVED, not INFERRED -
    # unlike a correlation module noticing a similarity by comparing
    # separate records.
    for handle in record.get("relatedHandles", []) or []:
        handle_id = make_entity_id("USERNAME", handle)
        relationships.append(new_relationship(
            username_id, handle_id, "ALIAS_OF",
            f"{username} is directly recorded as an alias/related handle of "
            f"{handle} in {rid}.",
            "OBSERVED",
        ))

    return relationships


# ---------------------------------------------------------------------------
# Full pipeline for this module: observed + all three inferred correlators
# ---------------------------------------------------------------------------
def detect_all_relationships(records: List[Dict]) -> Tuple[List[Dict], List[Dict]]:
    """
    Run every relationship-detection source and return
    (candidate_relationships, evidence) as two de-duplicated lists.
    """
    relationships: List[Dict] = []
    evidence_by_id: Dict[str, Dict] = {}

    def _collect_evidence(evidence_list: List[Dict]) -> None:
        for ev in evidence_list:
            evidence_by_id[ev["evidenceId"]] = ev

    # 1. OBSERVED relationships, per record
    for record in records:
        relationships.extend(detect_observed_relationships(record))

    # 2. INFERRED - cross-marketplace correlation
    marketplace_rels, marketplace_evidence = detect_marketplace_correlations(records)
    relationships.extend(marketplace_rels)
    _collect_evidence(marketplace_evidence)

    # 3. INFERRED - infrastructure correlation
    infra_rels, infra_evidence = detect_infrastructure_correlations(records)
    relationships.extend(infra_rels)
    _collect_evidence(infra_evidence)

    # 4. INFERRED - persona correlation (writing / behaviour / activity)
    persona_rels, persona_evidence = detect_persona_correlations(records)
    relationships.extend(persona_rels)
    _collect_evidence(persona_evidence)

    # De-duplicate relationships by relationshipId (the same relationship
    # can legitimately be re-derived by more than one correlation pass;
    # keep one copy but merge evidence references).
    relationships_by_id: Dict[str, Dict] = {}
    for rel in relationships:
        rid = rel["relationshipId"]
        if rid in relationships_by_id:
            existing_evidence = set(relationships_by_id[rid]["evidenceIds"])
            existing_evidence.update(rel["evidenceIds"])
            relationships_by_id[rid]["evidenceIds"] = sorted(existing_evidence)
        else:
            relationships_by_id[rid] = dict(rel)

    return list(relationships_by_id.values()), list(evidence_by_id.values())
