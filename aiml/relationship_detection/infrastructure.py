"""Infrastructure / hidden-service correlation from controlled intelligence."""
import itertools
import re
from collections import defaultdict
from typing import List

from ..models import RelDraft
from .common import Ctx, lower, make_evidence, min_level

HOST_CUE = re.compile(r"hosted (?:on|at)|runs? on|running on|served from|operated (?:on|at)", re.I)
RESOLVE_CUE = re.compile(r"resolv\w*|points? to|at IP", re.I)
BANNER_CUE = re.compile(r"default\s+(?:apache|nginx|iis|lighttpd|tomcat)?\s*(?:welcome\s+)?(?:page|banner)|default (?:service )?banner|\bbanner\b", re.I)
STATUS_CUE = re.compile(r"server[- ]status|server status|/status\b", re.I)
DESCRIPTOR_CUE = re.compile(r"descriptor\s+(?:inconsisten\w*|mismatch\w*)", re.I)
INFRA_TYPES = {"ONION_SERVICE", "DOMAIN"}


def detect(ctx: Ctx) -> List[RelDraft]:
    drafts: List[RelDraft] = []
    links = []   # (infra_id, resource_id, record) for cert / IP sharing analysis

    for rec in ctx.records:
        ids = list(dict.fromkeys(m.entity_id for m in ctx.by_record[rec.idx]))
        o = [i for i in ids if ctx.etype(i) == "ONION_SERVICE"]
        d = [i for i in ids if ctx.etype(i) == "DOMAIN"]
        ips = [i for i in ids if ctx.etype(i) == "IP_ADDRESS"]
        certs = [i for i in ids if ctx.etype(i) == "SSL_CERTIFICATE"]
        plats = [i for i in ids if ctx.etype(i) in ("MARKETPLACE", "FORUM")]
        rec_drafts: List[RelDraft] = []

        for x in o + d:
            for c in certs:
                rec_drafts.append(RelDraft(
                    x, c, "CERTIFICATE_MATCH",
                    f"{ctx.name(x)} presented TLS certificate {ctx.name(c)[:23]}...", "OBSERVED",
                    [make_evidence("SSL_CERTIFICATE_MATCH", [rec],
                                   f"{rec.source} records {ctx.name(x)} presenting certificate fingerprint "
                                   f"{ctx.name(c)[:23]}...", "MEDIUM", "INFRASTRUCTURE_CERT")]))
                links.append((x, c, rec))
        for x in d:
            for ip in ips:
                if RESOLVE_CUE.search(rec.text):
                    rec_drafts.append(RelDraft(
                        x, ip, "RESOLVES_TO", f"{ctx.name(x)} resolves to {ctx.name(ip)}.", "OBSERVED",
                        [make_evidence("SOURCE_CORROBORATION", [rec],
                                       f"{rec.source} records {ctx.name(x)} resolving to {ctx.name(ip)}.",
                                       "MEDIUM", "INFRASTRUCTURE_IP")]))
                    links.append((x, ip, rec))
        for x in o:
            for ip in ips:
                if HOST_CUE.search(rec.text):
                    rec_drafts.append(RelDraft(
                        x, ip, "HOSTED_ON", f"{ctx.name(x)} is hosted on {ctx.name(ip)}.", "OBSERVED",
                        [make_evidence("SOURCE_CORROBORATION", [rec],
                                       f"{rec.source} records {ctx.name(x)} hosted on {ctx.name(ip)}.",
                                       "MEDIUM", "INFRASTRUCTURE_IP")]))
                    links.append((x, ip, rec))
                elif BANNER_CUE.search(rec.text):
                    rec_drafts.append(RelDraft(
                        x, ip, "HOSTED_ON",
                        f"{ctx.name(x)} and {ctx.name(ip)} expose the same default service banner, "
                        f"suggesting shared hosting.", "INFERRED",
                        [make_evidence("DEFAULT_SERVICE_BANNER", [rec],
                                       f"The same default service banner was seen on {ctx.name(x)} and "
                                       f"{ctx.name(ip)}.", "LOW", "INFRASTRUCTURE_BANNER")]))
                    links.append((x, ip, rec))
        for p in plats:
            for x in o + d:
                if HOST_CUE.search(rec.text):
                    rec_drafts.append(RelDraft(
                        p, x, "HOSTED_ON", f"{ctx.name(p)} is served from {ctx.name(x)}.", "OBSERVED",
                        [make_evidence("SOURCE_CORROBORATION", [rec],
                                       f"{rec.source} states {ctx.name(p)} is served from {ctx.name(x)}.",
                                       "MEDIUM", "INFRASTRUCTURE_HOSTING")]))

        # extra infrastructure signals attach to the first relationship touching an onion service
        target = next((r for r in rec_drafts if r.source in o or r.target in o), None)
        if target:
            if STATUS_CUE.search(rec.text):
                target.evidence.append(make_evidence(
                    "SERVER_STATUS_EXPOSURE", [rec],
                    f"{rec.source} notes an exposed server-status page on the onion service.", "MEDIUM",
                    "INFRASTRUCTURE_EXPOSURE"))
            if DESCRIPTOR_CUE.search(rec.text):
                target.evidence.append(make_evidence(
                    "DESCRIPTOR_INCONSISTENCY", [rec],
                    f"{rec.source} notes an inconsistency in the onion service descriptor.", "MEDIUM",
                    "INFRASTRUCTURE_DESCRIPTOR"))
        drafts.extend(rec_drafts)

    # ---- shared certificate / IP between infrastructure entities ----------------------------
    by_res = defaultdict(list)
    for x, res, rec in links:
        by_res[res].append((x, rec))
    for res, items in by_res.items():
        is_cert = ctx.etype(res) == "SSL_CERTIFICATE"
        for (x1, r1), (x2, r2) in itertools.combinations(items, 2):
            if x1 == x2 or ctx.etype(x1) not in INFRA_TYPES or ctx.etype(x2) not in INFRA_TYPES:
                continue
            recs = [r1] if r1.idx == r2.idx else [r1, r2]
            what = "TLS certificate" if is_cert else "IP address"
            base = f"{ctx.name(x1)} and {ctx.name(x2)} are both tied to the same {what} {ctx.name(res)[:23]}"
            types = {ctx.etype(x1), ctx.etype(x2)}
            if is_cert and types == {"ONION_SERVICE", "DOMAIN"}:
                on, dm = (x1, x2) if ctx.etype(x1) == "ONION_SERVICE" else (x2, x1)
                drafts.append(RelDraft(
                    on, dm, "LINKED_TO_CLEARNET", base + ", linking the hidden service to clearnet infrastructure.",
                    "INFERRED",
                    [make_evidence("SSL_CERTIFICATE_MATCH", recs, base + ".", "HIGH", "INFRASTRUCTURE_CERT"),
                     make_evidence("CLEARNET_INFRASTRUCTURE_MATCH", recs, base + " (clearnet counterpart).",
                                   "HIGH", "INFRASTRUCTURE_CERT")]))
            else:
                key = sorted((x1, x2))
                evs = [make_evidence("INFRASTRUCTURE_REUSE", recs, base + ".", "HIGH" if is_cert else "MEDIUM",
                                     "INFRASTRUCTURE_CERT" if is_cert else "INFRASTRUCTURE_IP")]
                if is_cert:
                    evs.append(make_evidence("SSL_CERTIFICATE_MATCH", recs, base + ".", "HIGH", "INFRASTRUCTURE_CERT"))
                drafts.append(RelDraft(key[0], key[1], "REUSES_INFRASTRUCTURE", base + ".", "INFERRED", evs))
    return drafts
