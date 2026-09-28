"""ONE unified AIML entry point.

    controlled intelligence -> entity extraction -> similarity analysis -> relationship detection
    -> evidence normalisation -> contract validation -> {caseId, entities, candidateRelationships, evidence}

AIML never produces an attribution-confidence score; that is the backend confidence engine's job.
"""
import logging
from typing import Dict

from ..contract import ContractError, validate_output
from ..entity_extraction import extract
from ..evidence import normalize
from ..relationship_detection import Ctx, detect
from ..similarity import analyze

log = logging.getLogger("aiml.pipeline")


def run_pipeline(payload: Dict, strict: bool = True) -> Dict:
    if not isinstance(payload, dict) or payload.get("caseId") is None:
        raise ValueError("payload must be an object containing caseId")
    if not isinstance(payload.get("intelligence", []), list):
        raise ValueError("'intelligence' must be a list of {source, text} records")

    ex = extract(payload)
    ctx = Ctx(ex)

    try:
        signals = analyze(ex, ctx)
    except Exception:
        log.exception("similarity analysis failed; continuing without similarity signals")
        signals = []

    drafts = detect(ex, signals, ctx)
    relationships, evidence = normalize(ex.entities, drafts)

    out = {
        "caseId": payload["caseId"],
        "entities": [e.to_dict() for e in ex.entities],
        "candidateRelationships": relationships,
        "evidence": evidence,
    }
    errors = validate_output(out)
    if errors:
        log.error("contract violations: %s", errors)
        if strict:
            raise ContractError("; ".join(errors))
    return out
