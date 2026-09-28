"""Tunable thresholds. All values are analytical-signal thresholds, NOT probabilities."""

MIN_SAMPLE_WORDS = 30          # minimum words per handle before stylometry is attempted

# (low, medium, high) score cut-offs -> evidence strength. Below `low` => no signal.
STYLO_THRESHOLDS = (0.60, 0.72, 0.85)
BEHAV_THRESHOLDS = (0.50, 0.60, 0.80)
ACTIVITY_THRESHOLDS = (0.60, 0.70, 0.85)
BEHAV_MIN_SHARED_TAGS = 2
ACTIVITY_CONTRADICTION = 0.20   # overlap at or below this => contradicting signal

HANDLE_STEM_MIN = 4             # min shared leet-normalised prefix for HANDLE_SIMILARITY
