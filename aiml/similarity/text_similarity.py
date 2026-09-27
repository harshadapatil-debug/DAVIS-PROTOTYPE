"""
text_similarity.py
-------------------
Explainable text similarity using TF-IDF + cosine similarity
(scikit-learn). Deliberately lightweight - no embeddings, no
transformer models - so the method can be fully explained in a viva:

    1. Turn each text into a vector of term frequencies weighted by
       inverse document frequency (TF-IDF).
    2. Measure the cosine of the angle between the two vectors.
    3. A score of 1.0 means identical word usage; 0.0 means no
       vocabulary overlap at all.

IMPORTANT: a similarity score is a SIGNAL, not an identity probability.
A score of 0.88 means "these two controlled text samples are similar
according to this metric" - it must never be reported as "88% probability
these were written by the same person".
"""

from typing import Dict
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


def compute_text_similarity(text_a: str, text_b: str) -> float:
    """
    Return a TF-IDF cosine similarity score between 0.0 and 1.0 for two
    text samples. Returns 0.0 for empty/missing input rather than
    raising, since controlled records may sometimes lack a text sample.
    """
    if not text_a or not text_b:
        return 0.0

    vectorizer = TfidfVectorizer(stop_words="english")
    try:
        tfidf_matrix = vectorizer.fit_transform([text_a, text_b])
    except ValueError:
        # Happens if both texts are made up entirely of stop-words/empty
        # after cleaning - treat as no measurable similarity.
        return 0.0

    score = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])[0][0]
    return round(float(score), 2)


def build_text_similarity_evidence(record_a: Dict, record_b: Dict) -> Dict:
    """
    Compare the `writingSample` field of two records and return a
    normalized similarity/evidence signal.
    """
    text_a = record_a.get("writingSample", "")
    text_b = record_b.get("writingSample", "")
    score = compute_text_similarity(text_a, text_b)

    return {
        "type": "TEXT_SIMILARITY",
        "score": score,
        "description": (
            f"The controlled text samples from {record_a.get('recordId')} "
            f"and {record_b.get('recordId')} show a TF-IDF cosine "
            f"similarity of {score:.2f}. This is a lexical similarity "
            f"signal only, not an identity probability."
        ),
        "sourceRecordIds": [record_a.get("recordId"), record_b.get("recordId")],
    }
