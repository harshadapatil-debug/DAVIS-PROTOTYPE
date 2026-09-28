"""Behavioural and activity-pattern similarity."""
import re
from typing import Dict, Iterable, List, Set

TAG_PATTERNS = {
    "PREFERS_ESCROW": re.compile(r"\bescrow\b", re.I),
    "PREFERS_MONERO": re.compile(r"\b(?:monero|xmr)\b", re.I),
    "PREFERS_BITCOIN": re.compile(r"\b(?:bitcoin|btc)\b", re.I),
    "OFFERS_BULK_DISCOUNT": re.compile(r"\bbulk (?:discounts?|orders?)\b", re.I),
    "AVOIDS_FINALIZE_EARLY": re.compile(r"\b(?:no|refuses|rejects)\s+(?:fe|finalize early)\b", re.I),
}
SIGNOFF_RE = re.compile(r"sign(?:s|ed)?\s+off\s+(?:with|as)\s+[\"“]([^\"”]{2,60})[\"”]", re.I)
WINDOW_RE = re.compile(
    r"(?:between|from)\s+(\d{1,2})(?::(\d{2}))?\s*(?:to|and|-|–)\s*(\d{1,2})(?::(\d{2}))?\s*(?:UTC|GMT)?", re.I)
ACTIVITY_CUE_RE = re.compile(r"\b(?:post(?:s|ed)?|active|online|activity)\b", re.I)


def behaviour_tags(text: str) -> List[tuple]:
    """Return [(position, tag)] found in text."""
    out = []
    for tag, rx in TAG_PATTERNS.items():
        for m in rx.finditer(text):
            out.append((m.start(), tag))
    for m in SIGNOFF_RE.finditer(text):
        out.append((m.start(), "SIGNOFF:" + m.group(1).strip().lower()))
    return out


def activity_windows(text: str) -> List[tuple]:
    """Return [(position, hour_set)] for 'posts between HH:MM and HH:MM' phrases."""
    if not ACTIVITY_CUE_RE.search(text):
        return []
    out = []
    for m in WINDOW_RE.finditer(text):
        h1, m1, h2, m2 = int(m.group(1)), int(m.group(2) or 0), int(m.group(3)), int(m.group(4) or 0)
        if h1 > 24 or h2 > 24 or m1 > 59 or m2 > 59:
            continue
        start, end = h1 % 24, h2 % 24
        if start == end:
            continue
        hours: Set[int] = set()
        h = start
        last = end if m2 == 0 else (end + 1) % 24
        while h != last:
            hours.add(h)
            h = (h + 1) % 24
        out.append((m.start(), hours))
    return out


def jaccard(a: Iterable, b: Iterable) -> float:
    a, b = set(a), set(b)
    return len(a & b) / len(a | b) if (a or b) else 0.0
