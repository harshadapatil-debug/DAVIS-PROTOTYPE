"""Entity extraction from controlled/synthetic intelligence records.

Rule-based / regex NLP. Only entities that literally appear in the supplied text are produced
(no fabrication). Each entity carries a discoveryConfidence describing the reliability of the
extraction itself - NOT attribution confidence.
"""
import ipaddress
import logging
import re
from datetime import datetime
from typing import Dict, List, Optional, Tuple

from ..models import Entity, ExtractionResult, Mention, Record

log = logging.getLogger("aiml.entity_extraction")

EMAIL_RE = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}")
ETH_RE = re.compile(r"\b0x[0-9a-fA-F]{40}\b")
BTC_RE = re.compile(r"\b(?:bc1[a-z0-9]{25,60}|[13][a-km-zA-HJ-NP-Z1-9]{25,34})\b")
XMR_RE = re.compile(r"\b4[0-9AB][1-9A-HJ-NP-Za-km-z]{93}\b")
ONION_RE = re.compile(r"\b(?:[a-z2-7]{56}|[a-z2-7]{16})\.onion\b", re.I)
CERT_FP_RE = re.compile(r"(?<![0-9A-Fa-f:])((?:[0-9A-Fa-f]{2}:){15,31}[0-9A-Fa-f]{2})(?![0-9A-Fa-f:])")
IP_RE = re.compile(r"(?<![\d.])(?:\d{1,3}\.){3}\d{1,3}(?!\d)(?!\.\d)")
PGP_SHORT_RE = re.compile(r"\b0x[0-9A-Fa-f]{8}(?:[0-9A-Fa-f]{8})?\b")
PGP_FULL_RE = re.compile(r"\b[0-9A-Fa-f]{40}\b")
DOMAIN_RE = re.compile(
    r"\b(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\.)+(?:com|net|org|io|info|biz|xyz|ru|cc|to|co|me|su|cx)\b",
    re.I,
)
UNDERSCORE_TOKEN_RE = re.compile(
    r"(?<![A-Za-z0-9_@.\-])"
    r"(?=[A-Za-z0-9]*[a-z])"
    r"[A-Za-z0-9]+(?:_[A-Za-z0-9]+)+"
    r"(?![A-Za-z0-9_@\-])"
)
AT_HANDLE_RE = re.compile(r"(?<![A-Za-z0-9_@.])@([A-Za-z][A-Za-z0-9_-]{2,31})(?![A-Za-z0-9_@])")
CUE_HANDLE_RE = re.compile(
    r"\b(?:account|user|username|handle|alias|vendor|persona|seller)\s+(?:named\s+|called\s+)?"
    r"@?([A-Za-z][A-Za-z0-9_-]{2,31})", re.I)
AUTHOR_RE = re.compile(r"\bby\s+@?([A-Za-z0-9_-]{3,32})\s*:")

PLATFORM_CUE_RE = re.compile(
    r"(?:active on|listings? on|vendor on|sold on|registered on|registered at|posted on|selling on|"
    r"moved to|migrated to)\s+(?:the\s+)?([A-Z][A-Za-z0-9]{2,})")
MOVE_RE = re.compile(r"moved from\s+([A-Z][A-Za-z0-9]{2,})\s+to\s+([A-Z][A-Za-z0-9]{2,})")
PLATFORM_SUFFIX_RE = re.compile(r"\b([A-Z][A-Za-z0-9]{2,})\s+(marketplace|market|forum)\b")
KNOWN_MARKETS = {"darkbazaar": "DarkBazaar", "nightmart": "NightMart", "alphabay": "AlphaBay",
                 "hansa": "Hansa", "shadowbay": "ShadowBay"}
KNOWN_FORUMS = {"dread": "Dread", "breachlore": "BreachLore"}
PLATFORM_STOP = {"The", "This", "That", "Tor", "Account", "Vendor", "Wallet", "Related", "Handle",
                 "Post", "Onion", "Clearnet", "Certificate", "After", "Later", "Both"}

LABELS = {
    "USERNAME": "Username", "RELATED_HANDLE": "Related handle", "EMAIL": "Email address",
    "PGP_KEY": "PGP key", "WALLET": "Wallet address", "MARKETPLACE": "Marketplace",
    "FORUM": "Forum", "DOMAIN": "Domain", "IP_ADDRESS": "IP address",
    "ONION_SERVICE": "Onion service", "SSL_CERTIFICATE": "TLS certificate fingerprint",
    "ACCOUNT": "Account",
}
INDICATOR_TYPE_MAP = {
    "USERNAME": "USERNAME", "EMAIL": "EMAIL", "PGP_KEY": "PGP_KEY", "WALLET": "WALLET",
    "DOMAIN": "DOMAIN", "IP_ADDRESS": "IP_ADDRESS", "ONION_SERVICE": "ONION_SERVICE", "OTHER": "ACCOUNT",
}
HANDLE_TYPES = {"USERNAME", "RELATED_HANDLE"}


def _norm(etype: str, value: str) -> str:
    if etype == "EMAIL" or etype in ("DOMAIN", "ONION_SERVICE"):
        return value.lower()
    if etype == "PGP_KEY":
        v = value.strip()
        return "0x" + v[2:].upper() if v.lower().startswith("0x") else v.upper()
    if etype == "SSL_CERTIFICATE":
        return value.upper()
    if etype in HANDLE_TYPES:
        return value.lower()
    return value


class _Registry:
    def __init__(self):
        self.entities: List[Entity] = []
        self.index: Dict[Tuple[str, str], Entity] = {}
        self.handle_index: Dict[str, Entity] = {}

    def get(self, etype: str, value: str, source: str, conf: float, desc: Optional[str] = None) -> Entity:
        key_type = "HANDLE" if etype in HANDLE_TYPES else etype
        key = (key_type, _norm(etype, value))
        ent = self.index.get(key)
        if ent:
            ent.discoveryConfidence = max(ent.discoveryConfidence, conf)
            return ent
        shown = _norm(etype, value) if etype in ("EMAIL", "PGP_KEY", "DOMAIN", "ONION_SERVICE", "SSL_CERTIFICATE") else value
        ent = Entity(len(self.entities) + 1, etype, shown,
                     desc or f"{LABELS.get(etype, etype)} mentioned in {source}.", conf)
        self.entities.append(ent)
        self.index[key] = ent
        if etype in HANDLE_TYPES:
            self.handle_index[value.lower()] = ent
        return ent


def parse_records(payload: Dict) -> List[Record]:
    default_ts = payload.get("referenceTime")
    try:
        datetime.fromisoformat(default_ts)
    except Exception:
        default_ts = datetime.now().replace(microsecond=0).isoformat()
    records = []
    for n, item in enumerate(payload.get("intelligence") or [], 1):
        if not isinstance(item, dict) or not str(item.get("text", "")).strip():
            log.warning("skipping malformed intelligence item #%d", n)
            continue
        ts = item.get("observedAt")
        try:
            datetime.fromisoformat(ts)
        except Exception:
            ts = default_ts
        rel = str(item.get("reliability", "MEDIUM")).upper()
        if rel not in ("LOW", "MEDIUM", "HIGH"):
            rel = "MEDIUM"
        records.append(Record(len(records), str(item.get("source") or f"Intelligence Record {n}"),
                              str(item["text"]), ts, rel))
    return records


def _spans_free(taken, s, e):
    return all(e <= ts or s >= te for ts, te in taken)


def _non_handle_items(rec: Record):
    """Identifiers and platforms in one record: list of (etype, value, start, end, confidence)."""
    text, items, taken = rec.text, [], []

    def add(etype, value, s, e, conf, use_span=True):
        if use_span and not _spans_free(taken, s, e):
            return
        taken.append((s, e))
        items.append((etype, value, s, e, conf))

    for m in EMAIL_RE.finditer(text):
        add("EMAIL", m.group(0), m.start(), m.end(), 0.95)
    for rx in (ETH_RE, BTC_RE, XMR_RE):
        for m in rx.finditer(text):
            add("WALLET", m.group(0), m.start(), m.end(), 0.90)
    for m in ONION_RE.finditer(text):
        add("ONION_SERVICE", m.group(0), m.start(), m.end(), 0.95)
    if re.search(r"certificate|cert\b|fingerprint|tls|ssl", text, re.I):
        for m in CERT_FP_RE.finditer(text):
            add("SSL_CERTIFICATE", m.group(1), m.start(1), m.end(1), 0.90)
    for m in IP_RE.finditer(text):
        try:
            ipaddress.ip_address(m.group(0))
        except ValueError:
            continue
        add("IP_ADDRESS", m.group(0), m.start(), m.end(), 0.95)
    for m in PGP_SHORT_RE.finditer(text):
        add("PGP_KEY", m.group(0), m.start(), m.end(), 0.92)
    if re.search(r"\b(?:pgp|gpg)\b", text, re.I):
        for m in PGP_FULL_RE.finditer(text):
            add("PGP_KEY", m.group(0), m.start(), m.end(), 0.90)
    for m in DOMAIN_RE.finditer(text):
        add("DOMAIN", m.group(0), m.start(), m.end(), 0.85)

    # platforms
    def platform(name, s, e, conf, forum_hint=False):
        if name in PLATFORM_STOP:
            return
        low = name.lower()
        etype = "FORUM" if (forum_hint or low in KNOWN_FORUMS) else "MARKETPLACE"
        add(etype, name, s, e, conf)

    for m in MOVE_RE.finditer(text):
        platform(m.group(1), m.start(1), m.end(1), 0.80)
        platform(m.group(2), m.start(2), m.end(2), 0.80)
    for m in PLATFORM_CUE_RE.finditer(text):
        platform(m.group(1), m.start(1), m.end(1), 0.75)
    for m in PLATFORM_SUFFIX_RE.finditer(text):
        platform(m.group(1), m.start(1), m.end(1), 0.75, forum_hint=m.group(2).lower() == "forum")
    for word, canon in {**KNOWN_MARKETS, **KNOWN_FORUMS}.items():
        for m in re.finditer(r"\b" + word + r"\b", text, re.I):
            platform(canon, m.start(), m.end(), 0.90)
    return items, taken


def extract(payload: Dict) -> ExtractionResult:
    records = parse_records(payload)
    reg = _Registry()
    mentions: List[Mention] = []

    ind_type = str(payload.get("indicatorType", "")).upper()
    ind_val = str(payload.get("indicatorValue", "")).strip()
    indicator: Optional[Entity] = None
    per_record = [_non_handle_items(r) for r in records]

    # ---- global handle set --------------------------------------------------------------
    handles: Dict[str, Tuple[str, float]] = {}   # lower -> (display, confidence)
    if ind_type == "USERNAME" and ind_val:
        handles[ind_val.lower()] = (ind_val, 1.0)
    for rec, (_, taken) in zip(records, per_record):
        for rx, conf, need_digit in ((UNDERSCORE_TOKEN_RE, 0.80, False), (AT_HANDLE_RE, 0.80, False),
                                     (CUE_HANDLE_RE, 0.75, True), (AUTHOR_RE, 0.75, False)):
            for m in rx.finditer(rec.text):
                grp = 1 if rx is not UNDERSCORE_TOKEN_RE else 0
                tok = m.group(grp)
                s = m.start(grp)
                if not _spans_free(taken, s, s + len(tok)):
                    continue
                if need_digit and not any(c.isdigit() for c in tok):
                    continue
                if rx is AUTHOR_RE and not ("_" in tok or any(c.isdigit() for c in tok)) \
                        and tok.lower() not in handles:
                    continue
                handles.setdefault(tok.lower(), (tok, conf))
    handle_rx = {k: re.compile(r"(?<![A-Za-z0-9_@.\-])@?" + re.escape(v[0]) + r"(?![A-Za-z0-9_@\-])", re.I)
                 for k, v in handles.items()}

    def handle_type(low: str) -> str:
        if ind_val and low == ind_val.lower() and ind_type == "USERNAME":
            return "USERNAME"
        return "RELATED_HANDLE" if ind_type == "USERNAME" else "USERNAME"

    # ---- indicator entity first --------------------------------------------------------
    if ind_val:
        etype = INDICATOR_TYPE_MAP.get(ind_type, "ACCOUNT")
        indicator = reg.get(etype, ind_val, "the investigation request", 1.0,
                            desc="Known indicator supplied by investigator.")
        indicator.discoveryConfidence = 1.0

    # ---- register in document order and record mentions ----------------------------------
    for rec, (items, taken) in zip(records, per_record):
        found = list(items)
        for low, rx in handle_rx.items():
            for m in rx.finditer(rec.text):
                if _spans_free(taken, m.start(), m.end()):
                    found.append((handle_type(low), handles[low][0], m.start(), m.end(), handles[low][1]))
        found.sort(key=lambda x: x[2])
        for etype, value, s, e, conf in found:
            ent = reg.get(etype, value, rec.source, conf)
            mentions.append(Mention(ent.entityId, rec.idx, s, e))

    return ExtractionResult(records, reg.entities, mentions, indicator.entityId if indicator else None)
