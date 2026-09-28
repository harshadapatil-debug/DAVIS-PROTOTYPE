"""Text similarity: TF-IDF cosine over word tokens (pure Python)."""
import math
import re
from collections import Counter
from typing import Dict, Hashable

TOKEN_RE = re.compile(r"[a-z0-9']+")


def tokenize(text: str):
    return TOKEN_RE.findall(text.lower())


def cosine(a: Dict, b: Dict) -> float:
    if not a or not b:
        return 0.0
    dot = sum(v * b.get(k, 0.0) for k, v in a.items())
    na = math.sqrt(sum(v * v for v in a.values()))
    nb = math.sqrt(sum(v * v for v in b.values()))
    return dot / (na * nb) if na and nb else 0.0


class TfidfIndex:
    def __init__(self, docs: Dict[Hashable, str]):
        toks = {k: Counter(tokenize(t)) for k, t in docs.items()}
        n = max(len(toks), 1)
        df = Counter()
        for c in toks.values():
            df.update(c.keys())
        self.vecs = {k: {w: (1 + math.log(f)) * (math.log((1 + n) / (1 + df[w])) + 1.0)
                         for w, f in c.items()} for k, c in toks.items()}

    def similarity(self, a: Hashable, b: Hashable) -> float:
        return cosine(self.vecs.get(a, {}), self.vecs.get(b, {}))
