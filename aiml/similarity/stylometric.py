"""Stylometric similarity: char-trigram profile, function-word profile, scalar style features,
plus the word-level text similarity as one component. Output is an analytical signal in [0,1]."""
import re
from collections import Counter
from typing import Dict, Tuple

from .text_similarity import cosine

FUNCTION_WORDS = """the a an and or but if then so because as of to in on at by for with from up down out
i me my we you your u he she it they them this that these those is are was were be been am do does did
have has had not no dont cant wont isnt its thats will would can could should just also very really
too all any some more most only than when which who what how why here there""".split()


def _words(text: str):
    return re.findall(r"[a-z']+", text.lower())


def _trigrams(text: str) -> Counter:
    t = re.sub(r"\s+", " ", text.lower())
    return Counter(t[i:i + 3] for i in range(len(t) - 2))


def features(text: str) -> Dict:
    words = _words(text)
    n = max(len(words), 1)
    sentences = [s.strip() for s in re.split(r"[.!?]+(?:\s+|$)", text) if s.strip()]
    ns = max(len(sentences), 1)
    letters = [c for c in text if c.isalpha()]
    fw = Counter(w.replace("'", "") for w in words)
    fw_vec = {w: fw[w] / n for w in FUNCTION_WORDS if fw[w]}
    return {
        "n_words": len(words),
        "tri": _trigrams(text),
        "fw": fw_vec,
        "scalars": {
            "avg_word_len": (sum(len(w) for w in words) / n, 3.0),
            "avg_sent_len": (n / ns, 20.0),
            "comma_rate": (text.count(",") / n, 0.15),
            "ellipsis_rate": (text.count("...") / n, 0.05),
            "upper_ratio": (sum(c.isupper() for c in letters) / max(len(letters), 1), 0.05),
            "lower_start": (sum(1 for s in sentences if s[0].islower()) / ns, 1.0),
            "apostrophe_rate": (text.count("'") / n, 0.10),
        },
    }


def _scalar_similarity(fa: Dict, fb: Dict) -> float:
    sims = []
    for k, (va, scale) in fa["scalars"].items():
        vb = fb["scalars"][k][0]
        sims.append(max(0.0, 1.0 - abs(va - vb) / scale))
    return sum(sims) / len(sims)


def _rescale(x: float, lo: float, hi: float) -> float:
    return max(0.0, min(1.0, (x - lo) / (hi - lo)))


def stylometric_similarity(text_a: str, text_b: str, lexical: float = 0.0) -> Tuple[float, Dict]:
    """Return (score, components). `lexical` is the word-level TF-IDF similarity."""
    fa, fb = features(text_a), features(text_b)
    # Raw char-trigram cosine between any two English texts is high (~0.6-0.8); rescale.
    char = _rescale(cosine(fa["tri"], fb["tri"]), 0.55, 0.95)
    func = _rescale(cosine(fa["fw"], fb["fw"]), 0.55, 0.95)
    scal = _scalar_similarity(fa, fb)
    score = 0.30 * char + 0.25 * func + 0.30 * scal + 0.15 * lexical
    return round(score, 3), {"char_trigram": round(char, 3), "function_words": round(func, 3),
                             "style_features": round(scal, 3), "lexical": round(lexical, 3)}
