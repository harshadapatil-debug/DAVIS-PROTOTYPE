"""
stylometric_similarity.py
--------------------------
Lightweight, explainable stylometric similarity.

Instead of WHAT is being said (that's text_similarity.py / TF-IDF), this
module looks at HOW it is being said - the writer's stylistic
"fingerprint":

    - average sentence length (words per sentence)
    - average word length (characters per word)
    - punctuation usage rate (per 100 characters)
    - vocabulary richness (type-token ratio: unique words / total words)
    - function-word usage rate (common short connective words)

Each feature is compared using a fixed, documented "typical range" scale
(the same normalized-difference approach used in behaviour_similarity.py)
rather than min-max scaling the two samples against each other - with
only two points to compare, min-max scaling always maps them to exactly
0 and 1 on every differing feature, which destroys the actual magnitude
of the difference. Using a fixed scale keeps the comparison meaningful
and fully explainable.

As with text similarity, this produces a SIMILARITY SIGNAL, never an
identity probability or proof of common authorship.
"""

import re
from typing import Dict, List
import numpy as np

_SENTENCE_SPLIT = re.compile(r"[.!?]+")
_WORD_SPLIT = re.compile(r"\b[a-zA-Z']+\b")
_PUNCTUATION = re.compile(r"[,;:\-\(\)\"']")

# A small, fixed list of common English function words. Their relative
# frequency is a classic, well-explained stylometric signal (people tend
# to reuse connective/function words in a fairly stable way).
_FUNCTION_WORDS = {
    "the", "a", "an", "and", "but", "or", "so", "because", "if", "then",
    "of", "to", "in", "on", "at", "for", "with", "as", "is", "are",
    "was", "were", "be", "been", "i", "you", "we", "they", "it",
}


def _extract_features(text: str) -> np.ndarray:
    """
    Turn a raw text sample into a fixed-length numeric feature vector.
    Returns an all-zero vector for empty input.
    """
    if not text or not text.strip():
        return np.zeros(5)

    sentences = [s for s in _SENTENCE_SPLIT.split(text) if s.strip()]
    words: List[str] = _WORD_SPLIT.findall(text.lower())

    if not words:
        return np.zeros(5)

    num_sentences = max(len(sentences), 1)
    num_words = len(words)

    avg_sentence_len = num_words / num_sentences
    avg_word_len = sum(len(w) for w in words) / num_words
    punctuation_rate = (len(_PUNCTUATION.findall(text)) / max(len(text), 1)) * 100
    vocab_richness = len(set(words)) / num_words  # type-token ratio
    function_word_rate = sum(1 for w in words if w in _FUNCTION_WORDS) / num_words

    return np.array([
        avg_sentence_len,
        avg_word_len,
        punctuation_rate,
        vocab_richness,
        function_word_rate,
    ])


# Fixed "typical range" scale for each feature, in the same order as
# _extract_features(). A difference of 0 on a feature -> similarity 1.0
# on that feature; a difference >= the scale -> similarity 0.0 on that
# feature. These are simple, documented constants (not learned), chosen
# to reflect a reasonable spread for short controlled-intelligence text
# samples.
_FEATURE_SCALES = np.array([
    8.0,   # avg_sentence_len (words per sentence)
    2.0,   # avg_word_len (characters per word)
    5.0,   # punctuation_rate (per 100 characters)
    0.3,   # vocab_richness (type-token ratio)
    0.2,   # function_word_rate
])


def compute_stylometric_similarity(text_a: str, text_b: str) -> float:
    """
    Return a stylometric similarity score between 0.0 and 1.0.

    Each of the 5 style features is compared with a fixed-scale
    normalized difference (1.0 = identical, 0.0 = differs by at least
    one full "typical range" or more), and the 5 per-feature similarities
    are averaged into one overall score.
    """
    features_a = _extract_features(text_a)
    features_b = _extract_features(text_b)

    if not features_a.any() or not features_b.any():
        return 0.0

    abs_diff = np.abs(features_a - features_b)
    per_feature_similarity = 1.0 - np.minimum(abs_diff / _FEATURE_SCALES, 1.0)

    score = float(np.mean(per_feature_similarity))
    return round(min(max(score, 0.0), 1.0), 2)


def build_stylometric_similarity_evidence(record_a: Dict, record_b: Dict) -> Dict:
    """
    Compare the `writingSample` field of two records using stylometric
    features and return a normalized similarity/evidence signal.
    """
    text_a = record_a.get("writingSample", "")
    text_b = record_b.get("writingSample", "")
    score = compute_stylometric_similarity(text_a, text_b)

    return {
        "type": "STYLOMETRIC_SIMILARITY",
        "score": score,
        "description": (
            f"The controlled writing samples from {record_a.get('recordId')} "
            f"and {record_b.get('recordId')} show a stylometric similarity "
            f"of {score:.2f} (sentence length, word length, punctuation, "
            f"vocabulary richness, function-word usage). This is a "
            f"stylistic similarity signal only - it does not prove common "
            f"authorship."
        ),
        "sourceRecordIds": [record_a.get("recordId"), record_b.get("recordId")],
    }
