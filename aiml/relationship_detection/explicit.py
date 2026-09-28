"""Explicit relationship detection from controlled intelligence text."""

import re
from typing import List

from ..models import ExtractionResult, RelDraft
from .common import Ctx, make_evidence


RELATED_HANDLE_RE = re.compile(
    r"\b(?:related\s+handle|alias)\s+@?([A-Za-z][A-Za-z0-9_-]{2,31})"
    r".{0,120}?"
    r"\b(?:naming\s+pattern|name|handle)\b"
    r".{0,80}?"
    r"\b(?:with|to)\s+@?([A-Za-z][A-Za-z0-9_-]{2,31})\b",
    re.I,
)


def detect(ctx: Ctx) -> List[RelDraft]:
    """Detect directly described handle/alias associations."""
    drafts: List[RelDraft] = []

    for rec in ctx.records:
        text = rec.text

        for match in RELATED_HANDLE_RE.finditer(text):
            first_value = match.group(1)
            second_value = match.group(2)

            first_id = _find_handle(ctx, first_value)
            second_id = _find_handle(ctx, second_value)

            if first_id is None or second_id is None or first_id == second_id:
                continue

            evidence = make_evidence(
                "HANDLE_SIMILARITY",
                [rec],
                f"The intelligence record describes a naming-pattern similarity "
                f"between {ctx.name(first_id)} and {ctx.name(second_id)}.",
                "MEDIUM",
                "IDENTIFIER_PATTERN",
                "SUPPORTS",
            )

            drafts.append(
                RelDraft(
                    first_id,
                    second_id,
                    "ALIAS_OF",
                    f"{ctx.name(first_id)} shows a related naming pattern with "
                    f"{ctx.name(second_id)}; treated as a candidate alias relationship "
                    f"requiring investigator review.",
                    "INFERRED",
                    [evidence],
                )
            )

    return drafts


def _find_handle(ctx: Ctx, value: str):
    value_lower = value.lower()

    for entity in ctx.entities.values():
        if entity.entityType in ("USERNAME", "RELATED_HANDLE"):
            if entity.entityValue.lower() == value_lower:
                return entity.entityId

    return None