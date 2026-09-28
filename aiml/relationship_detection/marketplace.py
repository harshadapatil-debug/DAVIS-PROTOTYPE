"""Cross-marketplace / identity correlation: handle<->identifier links, shared identifiers,
alias claims, platform migration."""
import itertools
import re
from collections import defaultdict
from typing import List

from .. import config
from ..models import RelDraft
from .common import Ctx, lower, make_evidence

DIRECT = {  # identifier type -> (relationship, group)
    "EMAIL": ("USES_EMAIL", "IDENTITY_CONTACT"),
    "PGP_KEY": ("USES_PGP_KEY", "CRYPTOGRAPHIC_PGP"),
    "WALLET": ("LINKED_WALLET", "FINANCIAL_WALLET"),
}
SHARED = {  # identifier type -> (evidence type, group, sharing relationship or None)
    "EMAIL": ("EMAIL_MATCH", "IDENTITY_CONTACT", None),
    "PGP_KEY": ("SHARED_PGP_KEY", "CRYPTOGRAPHIC_PGP", "SHARES_PGP_KEY"),
    "WALLET": ("WALLET_MATCH", "FINANCIAL_WALLET", "SHARES_WALLET"),
}
LABEL = {"EMAIL": "email address", "PGP_KEY": "PGP key", "WALLET": "wallet address"}
ALIAS_RE = re.compile(
    r"@?([A-Za-z0-9_-]{3,32})\s+(?:is an alias of|is also known as|also known as|aka|a\.k\.a\.|alias of)\s+@?([A-Za-z0-9_-]{3,32})",
    re.I)
MOVE_RE = re.compile(r"moved from\s+([A-Z][A-Za-z0-9]{2,})\s+to\s+([A-Z][A-Za-z0-9]{2,})")
REGISTERED_RE = re.compile(r"registered\s+(?:on|at)\s*$", re.I)
_LEET = str.maketrans("4301157", "aeoiist")


def _stem(h: str) -> str:
    return re.split(r"[_\-]", h.lower().translate(_LEET))[0]


def detect(ctx: Ctx) -> List[RelDraft]:
    drafts: List[RelDraft] = []
    by_val = {e.entityValue.lower(): e.entityId for e in ctx.entities.values()}
    ident_owners = defaultdict(list)     # identifier id -> [(handle id, record, ambiguous)]
    migration_spans = defaultdict(list)

    # ---- migration ----------------------------------------------------------------------
    for rec in ctx.records:
        for m in MOVE_RE.finditer(rec.text):
            migration_spans[rec.idx].append((m.start(), m.end()))
            src, dst = by_val.get(m.group(1).lower()), by_val.get(m.group(2).lower())
            owners, amb = ctx.owners(rec.idx, m.start())
            if not dst or src is None:
                continue
            for h in owners:
                drafts.append(RelDraft(
                    h, dst, "MOVED_TO_PLATFORM",
                    f"{ctx.name(h)} moved from {ctx.name(src)} to {ctx.name(dst)}.", "OBSERVED",
                    [make_evidence("MARKETPLACE_MIGRATION", [rec],
                                   f"Intelligence records a move of {ctx.name(h)} from {ctx.name(src)} "
                                   f"to {ctx.name(dst)}.", "MEDIUM" if not amb else "LOW", "MARKETPLACE")]))

    # ---- direct handle <-> identifier / platform links -----------------------------------
    for rec in ctx.records:
        for m in ctx.by_record[rec.idx]:
            et = ctx.etype(m.entity_id)
            if any(s <= m.start and m.end <= e for s, e in migration_spans[rec.idx]):
                continue
            owners, amb = ctx.owners(rec.idx, m.start)
            strength = "LOW" if amb else "MEDIUM"
            if et in DIRECT:
                rtype, group = DIRECT[et]
                for h in owners:
                    ident_owners[m.entity_id].append((h, rec, amb))
                    drafts.append(RelDraft(
                        h, m.entity_id, rtype,
                        f"{ctx.name(h)} is associated with the observed {LABEL[et]} {ctx.name(m.entity_id)}.",
                        "OBSERVED",
                        [make_evidence("ACCOUNT_ASSOCIATION", [rec],
                                       f"{rec.source} associates {ctx.name(h)} with {LABEL[et]} "
                                       f"{ctx.name(m.entity_id)}.", strength, group)]))
            elif et in ("MARKETPLACE", "FORUM"):
                rtype = "REGISTERED_ON" if REGISTERED_RE.search(rec.text[max(0, m.start - 20):m.start]) else "ACTIVE_ON"
                for h in owners:
                    drafts.append(RelDraft(
                        h, m.entity_id, rtype,
                        f"{ctx.name(h)} was observed {'registered' if rtype == 'REGISTERED_ON' else 'active'} "
                        f"on {ctx.name(m.entity_id)}.", "OBSERVED",
                        [make_evidence("MARKETPLACE_OVERLAP", [rec],
                                       f"{ctx.name(h)} observed on {ctx.name(m.entity_id)}.", strength,
                                       "MARKETPLACE")]))

    # ---- explicit alias claims -------------------------------------------------------------
    for rec in ctx.records:
        for m in ALIAS_RE.finditer(rec.text):
            a, b = by_val.get(m.group(1).lower()), by_val.get(m.group(2).lower())
            if a and b and a != b:
                drafts.append(RelDraft(a, b, "ALIAS_OF",
                                       f"{ctx.name(a)} is recorded as an alias of {ctx.name(b)}.", "OBSERVED",
                                       [make_evidence("SOURCE_CORROBORATION", [rec],
                                                      f"{rec.source} states {ctx.name(a)} is an alias of "
                                                      f"{ctx.name(b)}.", "MEDIUM", "IDENTITY_ALIAS")]))

    # ---- shared identifiers between different handles -------------------------------------
    actor_pairs = defaultdict(list)   # (h1,h2) -> [EvidenceDraft]
    for ident, owned in ident_owners.items():
        et = ctx.etype(ident)
        for (h1, r1, a1), (h2, r2, a2) in itertools.combinations(owned, 2):
            if h1 == h2:
                continue
            etype, group, rel = SHARED[et]
            strength = "MEDIUM" if (a1 or a2) else "HIGH"
            recs = [r1] if r1.idx == r2.idx else [r1, r2]
            desc = (f"The same {LABEL[et]} {ctx.name(ident)} is associated with both "
                    f"{ctx.name(h1)} and {ctx.name(h2)}.")
            key = tuple(sorted((h1, h2)))
            if rel:
                drafts.append(RelDraft(key[0], key[1], rel, desc, "INFERRED",
                                       [make_evidence(etype, recs, desc, strength, group)]))
            actor_pairs[key].append(make_evidence(etype, recs, desc, strength, group))

    for (h1, h2), evs in actor_pairs.items():
        s1, s2 = _stem(ctx.name(h1)), _stem(ctx.name(h2))
        if len(s1) >= config.HANDLE_STEM_MIN and s1 == s2:
            recs = [r for r in ctx.records if ctx.name(h1).lower() in r.text.lower()
                    or ctx.name(h2).lower() in r.text.lower()][:2] or ctx.records[:1]
            evs.append(make_evidence(
                "HANDLE_SIMILARITY", recs,
                f"The handles {ctx.name(h1)} and {ctx.name(h2)} share the stem '{s1}' after normalising "
                f"look-alike characters.", "LOW", "IDENTITY_HANDLE"))
        drafts.append(RelDraft(
            h1, h2, "POSSIBLE_SAME_ACTOR",
            f"Shared identifiers make {ctx.name(h1)} and {ctx.name(h2)} candidates for the same actor; "
            f"requires investigator review.", "INFERRED", evs))
    return drafts
