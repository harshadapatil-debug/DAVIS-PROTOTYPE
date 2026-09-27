"""
entity_patterns.py
-------------------
Regex patterns and keyword lists used by the entity extractor.

These patterns are intentionally simple and explainable (rule-based /
regex extraction), as required for this prototype. They are NOT a
general-purpose NLP model - they are tuned to the format of the
controlled synthetic intelligence used in this project.

Every pattern here is documented so it can be explained in a viva.
"""

import re

# ---------------------------------------------------------------------------
# EMAIL
# ---------------------------------------------------------------------------
# Standard, widely-used email pattern. Good enough for controlled/synthetic
# text - not meant to be a fully RFC-5322-compliant validator.
EMAIL_PATTERN = re.compile(
    r"[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"
)

# ---------------------------------------------------------------------------
# PGP KEY
# ---------------------------------------------------------------------------
# Two common ways a PGP key ID/fingerprint shows up in text:
#   1. A short key id like "0xA1B2C3D4"
#   2. The word "PGP" followed by a hex-like token
PGP_KEY_PATTERN = re.compile(
    r"0x[A-Fa-f0-9]{6,16}"
)

# ---------------------------------------------------------------------------
# WALLET (cryptocurrency address)
# ---------------------------------------------------------------------------
# Simplified pattern covering common Bitcoin-style address shapes
# (legacy P2PKH/P2SH, base58, 26-42 chars). This is a controlled synthetic
# prototype, so we do not attempt to support every chain/format.
WALLET_PATTERN = re.compile(
    r"\b[13][a-km-zA-HJ-NP-Z1-9]{25,39}\b"
)

# ---------------------------------------------------------------------------
# ONION SERVICE
# ---------------------------------------------------------------------------
ONION_PATTERN = re.compile(
    r"\b[a-z2-7]{16,56}\.onion\b", re.IGNORECASE
)

# ---------------------------------------------------------------------------
# DOMAIN (clearnet)
# ---------------------------------------------------------------------------
# Matches simple domains like "example.com" but NOT ".onion" addresses
# (those are handled separately above as ONION_SERVICE).
DOMAIN_PATTERN = re.compile(
    r"\b(?!\S+\.onion\b)([a-zA-Z0-9-]+\.)+(com|net|org|io|co|info|biz)\b",
    re.IGNORECASE,
)

# ---------------------------------------------------------------------------
# IP ADDRESS (used as part of INFRASTRUCTURE signals)
# ---------------------------------------------------------------------------
IPV4_PATTERN = re.compile(
    r"\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}"
    r"(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b"
)

# ---------------------------------------------------------------------------
# SSL CERTIFICATE FINGERPRINT (colon-separated hex bytes)
# ---------------------------------------------------------------------------
SSL_FINGERPRINT_PATTERN = re.compile(
    r"\b(?:[A-Fa-f0-9]{2}:){5,}[A-Fa-f0-9]{2}\b"
)

# ---------------------------------------------------------------------------
# USERNAME
# ---------------------------------------------------------------------------
# Usernames have no fixed structure, so we use a heuristic: look for a
# keyword ("account", "user", "username", "handle", "alias") followed by a
# token that looks like a handle (letters/digits/underscore, 3-32 chars).
USERNAME_CONTEXT_PATTERN = re.compile(
    r"\b(?:account|user(?:name)?|handle|alias)\s+[\"']?([A-Za-z0-9_\-]{3,32})[\"']?",
    re.IGNORECASE,
)

# ---------------------------------------------------------------------------
# MARKETPLACE
# ---------------------------------------------------------------------------
# Marketplaces are named entities without a regular structure, so for this
# controlled prototype we match against a known list drawn from the
# synthetic dataset. In a production system this list would come from a
# maintained gazetteer rather than being hard-coded.
KNOWN_MARKETPLACES = [
    "DarkBazaar",
    "ShadowMart",
    "NightExchange",
]

MARKETPLACE_PATTERN = re.compile(
    r"\b(" + "|".join(re.escape(m) for m in KNOWN_MARKETPLACES) + r")\b",
    re.IGNORECASE,
)

# ---------------------------------------------------------------------------
# Infrastructure keyword hints (used for descriptive text, not extraction)
# ---------------------------------------------------------------------------
INFRASTRUCTURE_KEYWORDS = [
    "server status exposure",
    "ssl certificate",
    "default service banner",
    "descriptor inconsistency",
    "clearnet infrastructure",
    "infrastructure reuse",
]
