"""
entity_extractor.py
--------------------
Converts controlled intelligence (structured JSON records, plus any free
text notes/writing samples inside them) into normalized entity objects.

Two extraction paths are implemented, on purpose:

1. STRUCTURED extraction (`extract_entities_from_record`)
   The synthetic intelligence dataset is structured JSON (username,
   email, pgpKey, wallet, ...). Because the fields are already labeled,
   we can build entities directly and with high confidence.

2. TEXT-based / regex extraction (`extract_entities_from_text`)
   Demonstrates the classic "raw sentence -> entities" flow described in
   the project brief, e.g.:
       "The account r4v3n_mh used PGP key 0xA1B2C3D4."
   This path is run over free-text fields such as `notes` and
   `writingSample` so the pipeline also proves it can extract entities
   from unstructured controlled text, not only from labeled fields.

Every entity produced by either path has the same normalized shape:

{
    "entityId": "ENT-XXXXXXXX",
    "entityType": "USERNAME" | "EMAIL" | "PGP_KEY" | "WALLET" |
                   "MARKETPLACE" | "DOMAIN" | "ONION_SERVICE" |
                   "INFRASTRUCTURE" | "BEHAVIOR_PATTERN" |
                   "WRITING_SIGNATURE",
    "entityValue": <str>,
    "description": <str>,
    "discoveryConfidence": <float 0-1>,
    "sourceRecordIds": [<record ids this entity was observed in>],
}

Entity IDs are DETERMINISTIC: the same (entityType, entityValue) pair
always produces the same entityId. This is essential for relationship
detection, since it lets us recognise that the same PGP key / wallet /
username appears in more than one record simply by comparing entity IDs.
"""

import hashlib
from typing import Dict, List

from aiml.entity_extraction.entity_patterns import (
    EMAIL_PATTERN,
    PGP_KEY_PATTERN,
    WALLET_PATTERN,
    ONION_PATTERN,
    DOMAIN_PATTERN,
    IPV4_PATTERN,
    SSL_FINGERPRINT_PATTERN,
    USERNAME_CONTEXT_PATTERN,
    MARKETPLACE_PATTERN,
)

# Prefix used per entity type, purely so entity IDs are readable
# (e.g. "ENT-USERNAME-1a2b3c4d").
_TYPE_PREFIX = {
    "USERNAME": "USERNAME",
    "EMAIL": "EMAIL",
    "PGP_KEY": "PGPKEY",
    "WALLET": "WALLET",
    "MARKETPLACE": "MARKET",
    "DOMAIN": "DOMAIN",
    "ONION_SERVICE": "ONION",
    "INFRASTRUCTURE": "INFRA",
    "BEHAVIOR_PATTERN": "BEHAVIOR",
    "WRITING_SIGNATURE": "WRITING",
}


def make_entity_id(entity_type: str, entity_value: str) -> str:
    """
    Build a deterministic, human-readable entity ID.

    The same (type, value) pair always maps to the same ID, which is what
    allows the relationship-detection modules to later notice that, e.g.,
    the same PGP key entity appears attached to two different usernames.
    """
    digest = hashlib.md5(f"{entity_type}:{entity_value}".encode("utf-8")).hexdigest()[:8]
    prefix = _TYPE_PREFIX.get(entity_type, "ENTITY")
    return f"ENT-{prefix}-{digest}"


def _new_entity(entity_type: str, entity_value: str, description: str,
                 confidence: float, source_record_id: str) -> Dict:
    return {
        "entityId": make_entity_id(entity_type, entity_value),
        "entityType": entity_type,
        "entityValue": entity_value,
        "description": description,
        "discoveryConfidence": round(float(confidence), 2),
        "sourceRecordIds": [source_record_id] if source_record_id else [],
    }


def _merge_entity(existing: Dict, new: Dict) -> Dict:
    """
    Merge a newly-found entity into an already-collected one (same
    entityId). We keep the highest confidence seen and accumulate the
    list of source records the entity was observed in.
    """
    existing["discoveryConfidence"] = round(
        max(existing["discoveryConfidence"], new["discoveryConfidence"]), 2
    )
    for rid in new["sourceRecordIds"]:
        if rid not in existing["sourceRecordIds"]:
            existing["sourceRecordIds"].append(rid)
    return existing


def _collect(entities_by_id: Dict[str, Dict], entity: Dict) -> None:
    eid = entity["entityId"]
    if eid in entities_by_id:
        _merge_entity(entities_by_id[eid], entity)
    else:
        entities_by_id[eid] = entity


# ---------------------------------------------------------------------------
# TEXT-BASED (regex) EXTRACTION
# ---------------------------------------------------------------------------
def extract_entities_from_text(text: str, source_record_id: str = None) -> List[Dict]:
    """
    Run regex/rule-based extraction over a raw text string.

    This mirrors the example given in the project brief:
        "The account r4v3n_mh used PGP key 0xA1B2C3D4."
    ->  USERNAME  r4v3n_mh
        PGP_KEY   0xA1B2C3D4

    Confidence values are fixed, explainable constants per entity type
    (not learned) - this keeps the system simple and defensible for a
    prototype/viva: a human reviewer can see exactly why a score was
    assigned.
    """
    if not text:
        return []

    found: List[Dict] = []

    for match in USERNAME_CONTEXT_PATTERN.finditer(text):
        found.append(_new_entity(
            "USERNAME", match.group(1),
            "Username mentioned in controlled intelligence text.",
            0.90, source_record_id,
        ))

    for match in EMAIL_PATTERN.finditer(text):
        found.append(_new_entity(
            "EMAIL", match.group(0),
            "Email address mentioned in controlled intelligence text.",
            0.95, source_record_id,
        ))

    for match in PGP_KEY_PATTERN.finditer(text):
        found.append(_new_entity(
            "PGP_KEY", match.group(0),
            "PGP key mentioned in the intelligence record.",
            0.92, source_record_id,
        ))

    for match in WALLET_PATTERN.finditer(text):
        found.append(_new_entity(
            "WALLET", match.group(0),
            "Cryptocurrency wallet address mentioned in intelligence text.",
            0.85, source_record_id,
        ))

    for match in ONION_PATTERN.finditer(text):
        found.append(_new_entity(
            "ONION_SERVICE", match.group(0),
            "Onion service address mentioned in intelligence text.",
            0.90, source_record_id,
        ))

    for match in DOMAIN_PATTERN.finditer(text):
        found.append(_new_entity(
            "DOMAIN", match.group(0),
            "Clearnet domain mentioned in intelligence text.",
            0.80, source_record_id,
        ))

    for match in MARKETPLACE_PATTERN.finditer(text):
        found.append(_new_entity(
            "MARKETPLACE", match.group(0),
            "Marketplace mentioned in intelligence text.",
            0.88, source_record_id,
        ))

    return found


# ---------------------------------------------------------------------------
# STRUCTURED EXTRACTION (from labeled JSON fields)
# ---------------------------------------------------------------------------
def extract_entities_from_record(record: Dict) -> List[Dict]:
    """
    Build normalized entities directly from a structured synthetic
    intelligence record. Structured fields are already labeled by the
    (synthetic) data source, so these entities are assigned a high,
    fixed confidence.

    Also runs `extract_entities_from_text` over the record's free-text
    fields (`notes`, `writingSample`) so unstructured content is not
    ignored.
    """
    rid = record.get("recordId")
    entities: List[Dict] = []

    if record.get("username"):
        entities.append(_new_entity(
            "USERNAME", record["username"],
            "Observed username in controlled intelligence record.",
            0.95, rid,
        ))

    for handle in record.get("relatedHandles", []) or []:
        entities.append(_new_entity(
            "USERNAME", handle,
            "Related handle/alias observed alongside the primary username.",
            0.85, rid,
        ))

    if record.get("email"):
        entities.append(_new_entity(
            "EMAIL", record["email"],
            "Observed email address in controlled intelligence record.",
            0.95, rid,
        ))

    if record.get("pgpKey"):
        entities.append(_new_entity(
            "PGP_KEY", record["pgpKey"],
            "PGP key observed in controlled intelligence record.",
            0.95, rid,
        ))

    if record.get("wallet"):
        entities.append(_new_entity(
            "WALLET", record["wallet"],
            "Cryptocurrency wallet address observed in intelligence record.",
            0.93, rid,
        ))

    if record.get("marketplace"):
        entities.append(_new_entity(
            "MARKETPLACE", record["marketplace"],
            "Marketplace on which the username was observed as active.",
            0.95, rid,
        ))

    if record.get("domain"):
        entities.append(_new_entity(
            "DOMAIN", record["domain"],
            "Clearnet domain associated with the intelligence record.",
            0.85, rid,
        ))

    if record.get("onionService"):
        entities.append(_new_entity(
            "ONION_SERVICE", record["onionService"],
            "Onion service address associated with the intelligence record.",
            0.90, rid,
        ))

    infra = record.get("infrastructure") or {}
    # We key the INFRASTRUCTURE entity off the SSL fingerprint when present
    # (the strongest, least-spoofable signal); otherwise fall back to the
    # clearnet IP; otherwise the raw service banner string.
    infra_value = infra.get("sslCertFingerprint") or infra.get("clearnetIP") or infra.get("serviceBanner")
    if infra_value:
        entities.append(_new_entity(
            "INFRASTRUCTURE", infra_value,
            "Infrastructure fingerprint (SSL certificate / IP / service "
            "banner) associated with the intelligence record.",
            0.80, rid,
        ))

    behaviour = record.get("behaviour") or {}
    if behaviour:
        # One BEHAVIOR_PATTERN entity per record - this is a derived
        # analytical entity (not a "fact" like a PGP key), so it is scoped
        # to this record only and given a slightly lower confidence.
        behaviour_value = f"{rid}-behaviour"
        entities.append(_new_entity(
            "BEHAVIOR_PATTERN", behaviour_value,
            "Behavioural/activity pattern derived from posting frequency, "
            "response time and active-hours data in this record.",
            0.70, rid,
        ))

    if record.get("writingSample"):
        writing_value = f"{rid}-writing"
        entities.append(_new_entity(
            "WRITING_SIGNATURE", writing_value,
            "Writing-style signature derived from the free-text sample in "
            "this record.",
            0.70, rid,
        ))

    # Also run the regex/text extractor over any free-text fields, to
    # demonstrate the raw-text extraction path and pick up anything the
    # structured fields might have missed (e.g. an incidental mention of
    # another actor's username inside a note).
    for text_field in ("notes", "writingSample"):
        entities.extend(
            extract_entities_from_text(record.get(text_field, ""), rid)
        )

    return entities


def extract_all_entities(records: List[Dict]) -> List[Dict]:
    """
    Run structured extraction across every record in the dataset and
    de-duplicate/merge entities that appear more than once (e.g. the same
    PGP key used in two records becomes ONE entity with two source
    records, which is exactly what relationship detection needs).
    """
    entities_by_id: Dict[str, Dict] = {}

    for record in records:
        for entity in extract_entities_from_record(record):
            _collect(entities_by_id, entity)

    return list(entities_by_id.values())
