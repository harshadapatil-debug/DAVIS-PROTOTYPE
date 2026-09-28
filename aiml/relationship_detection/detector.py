import logging
from typing import List

from ..models import ExtractionResult, RelDraft, SimilaritySignal
from . import infrastructure, marketplace, persona
from .common import Ctx

log = logging.getLogger("aiml.relationship_detection")


def detect(ex: ExtractionResult, signals: List[SimilaritySignal], ctx: Ctx = None) -> List[RelDraft]:
    """Run marketplace, infrastructure and persona correlation. A failing module never aborts the run."""
    ctx = ctx or Ctx(ex)
    drafts: List[RelDraft] = []
    for name, fn in (("marketplace", lambda: marketplace.detect(ctx)),
                     ("infrastructure", lambda: infrastructure.detect(ctx))):
        try:
            drafts.extend(fn())
        except Exception:
            log.exception("%s correlation failed; continuing", name)
    try:
        drafts.extend(persona.detect(ctx, signals, drafts))
    except Exception:
        log.exception("persona correlation failed; continuing")
    return drafts
