import copy
import json
import os
import sys
import unittest
from unittest import mock

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from aiml import run_pipeline                      # noqa: E402
from aiml.contract import validate_output          # noqa: E402
from aiml.entity_extraction import extract         # noqa: E402
from aiml.relationship_detection import Ctx        # noqa: E402
from aiml.similarity import analyze                # noqa: E402

DATA = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "data",
                    "synthetic_intelligence.json")


def load():
    with open(DATA, encoding="utf-8") as fh:
        return json.load(fh)


def names(out):
    return {e["entityId"]: e["entityValue"] for e in out["entities"]}


def rels(out, rtype):
    n = names(out)
    return [(n[r["sourceEntityId"]], n[r["targetEntityId"]], r) for r in out["candidateRelationships"]
            if r["relationshipType"] == rtype]


class EntityExtractionTests(unittest.TestCase):
    def setUp(self):
        self.ex = extract(load())
        self.by_type = {}
        for e in self.ex.entities:
            self.by_type.setdefault(e.entityType, []).append(e.entityValue)

    def test_indicator_is_entity_one(self):
        e = self.ex.entities[0]
        self.assertEqual((e.entityId, e.entityType, e.entityValue, e.discoveryConfidence),
                         (1, "USERNAME", "r4v3n_mh", 1.0))

    def test_expected_types(self):
        for t in ("RELATED_HANDLE", "EMAIL", "PGP_KEY", "WALLET", "MARKETPLACE", "ONION_SERVICE",
                  "DOMAIN", "IP_ADDRESS", "SSL_CERTIFICATE"):
            self.assertIn(t, self.by_type, t)
        self.assertIn("0xA1B2C3D4E5F60718", self.by_type["PGP_KEY"])
        self.assertEqual(len(self.by_type["PGP_KEY"]), 1)          # de-duplicated

    def test_no_fabricated_entities(self):
        blob = " ".join(r.text for r in self.ex.records).lower()
        for e in self.ex.entities:
            if e.entityType in ("USERNAME", "EMAIL", "DOMAIN", "PGP_KEY", "IP_ADDRESS", "ONION_SERVICE"):
                self.assertIn(e.entityValue.lower(), blob)

    def test_sentence_final_ip_extracted(self):
        ex = extract({"caseId": 1, "intelligence": [{"source": "s", "text": "Host at 198.51.100.7."}]})
        self.assertEqual([e.entityValue for e in ex.entities], ["198.51.100.7"])


class SimilarityTests(unittest.TestCase):
    def test_signals(self):
        ex = extract(load())
        ctx = Ctx(ex)
        sigs = analyze(ex, ctx)
        by = {(ctx.name(s.a), ctx.name(s.b), s.signalType) for s in sigs if s.direction == "SUPPORTS"}
        for t in ("STYLOMETRIC_SIMILARITY", "BEHAVIOURAL_SIMILARITY", "ACTIVITY_PATTERN_MATCH"):
            self.assertIn(("r4v3n_mh", "raven_alt", t), by)
        for s in sigs:                                   # decoy handle must not match
            self.assertNotIn("crow_9", (ctx.name(s.a), ctx.name(s.b)) if s.direction == "SUPPORTS" else ())
            self.assertTrue(0.0 <= s.score <= 1.0)

    def test_short_samples_yield_no_stylometry(self):
        p = {"caseId": 1, "indicatorType": "USERNAME", "indicatorValue": "a_1", "intelligence": [
            {"source": "s1", "text": 'Post by a_1: "hello there"'},
            {"source": "s2", "text": 'Post by b_2: "hello there"'}]}
        ex = extract(p)
        self.assertEqual([s for s in analyze(ex, Ctx(ex)) if s.signalType == "STYLOMETRIC_SIMILARITY"], [])


class PipelineTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.out = run_pipeline(load())

    def test_unified_shape(self):
        self.assertEqual(set(self.out), {"caseId", "entities", "candidateRelationships", "evidence"})
        self.assertEqual(self.out["caseId"], 101)

    def test_contract_valid_and_json(self):
        self.assertEqual(validate_output(self.out), [])
        json.dumps(self.out)

    def test_evidence_relationship_linkage(self):
        rid = {r["relationshipId"] for r in self.out["candidateRelationships"]}
        eid = {e["evidenceId"] for e in self.out["evidence"]}
        for e in self.out["evidence"]:
            self.assertIn(e["relationshipId"], rid)
        for r in self.out["candidateRelationships"]:
            self.assertTrue(r["evidenceIds"])
            for x in r["evidenceIds"]:
                self.assertIn(x, eid)

    def test_expected_relationships(self):
        self.assertTrue(rels(self.out, "USES_PGP_KEY"))
        self.assertTrue(rels(self.out, "SHARES_PGP_KEY"))
        self.assertTrue(rels(self.out, "SHARES_WALLET"))
        self.assertTrue(rels(self.out, "MOVED_TO_PLATFORM"))
        self.assertTrue(rels(self.out, "SIMILAR_WRITING"))
        self.assertTrue(rels(self.out, "POSSIBLE_SAME_PERSONA"))
        self.assertTrue(rels(self.out, "LINKED_TO_CLEARNET"))
        self.assertTrue(rels(self.out, "REUSES_INFRASTRUCTURE"))
        self.assertTrue(rels(self.out, "RESOLVES_TO"))
        actor = rels(self.out, "POSSIBLE_SAME_ACTOR")
        self.assertEqual(len(actor), 1)
        types = {e["evidenceType"] for e in self.out["evidence"]
                 if e["relationshipId"] == actor[0][2]["relationshipId"]}
        self.assertTrue({"SHARED_PGP_KEY", "EMAIL_MATCH", "WALLET_MATCH"} <= types)

    def test_decoy_not_linked_as_same_actor_or_persona(self):
        for rtype in ("POSSIBLE_SAME_ACTOR", "POSSIBLE_SAME_PERSONA", "SIMILAR_WRITING", "SHARES_PGP_KEY"):
            for a, b, _ in rels(self.out, rtype):
                self.assertNotIn("crow_9", (a, b))

    def test_observed_vs_inferred(self):
        for r in self.out["candidateRelationships"]:
            if r["relationshipType"].startswith(("SIMILAR_", "POSSIBLE_", "SHARES_")):
                self.assertEqual(r["assessment"], "INFERRED")
        self.assertTrue(any(r["assessment"] == "OBSERVED" for r in self.out["candidateRelationships"]))

    def test_no_score_or_identity_language(self):
        blob = json.dumps(self.out).lower()
        for bad in ('"score"', '"riskLevel"'.lower(), "probability", "same person", "identified the hacker", "%"):
            self.assertNotIn(bad, blob)

    def test_evidence_uses_record_metadata(self):
        pgp = [e for e in self.out["evidence"] if e["evidenceType"] == "SHARED_PGP_KEY"][0]
        self.assertIn("S-02", pgp["source"])
        self.assertIn("S-04", pgp["source"])
        self.assertEqual(pgp["observedAt"], "2026-09-19T13:20:00")     # latest of the two records

    def test_deterministic(self):
        self.assertEqual(self.out, run_pipeline(load()))

    def test_contradicting_activity_weakens_same_persona(self):
        p = load()
        for r in p["intelligence"]:
            if r["source"].endswith("S-10"):
                r["text"] = "Handle raven_alt posts mostly between 14:00 and 20:00 UTC."
        out = run_pipeline(p)
        persona = rels(out, "POSSIBLE_SAME_PERSONA")
        actor = rels(out, "POSSIBLE_SAME_ACTOR")
        self.assertTrue(actor)
        rid = actor[0][2]["relationshipId"]
        self.assertIn("CONTRADICTS", {e["direction"] for e in out["evidence"] if e["relationshipId"] == rid})
        self.assertEqual(validate_output(out), [])
        for _, _, r in persona:
            self.assertNotIn("ACTIVITY_PATTERN_MATCH",
                             {e["evidenceType"] for e in out["evidence"] if e["relationshipId"] == r["relationshipId"]
                              and e["direction"] == "SUPPORTS"})


class FailureRuleTests(unittest.TestCase):
    def test_garbage_records_do_not_break(self):
        p = load()
        p["intelligence"] += [None, 5, {"text": ""}, {"source": "x"}]
        out = run_pipeline(p)
        self.assertEqual(validate_output(out), [])

    def test_no_intelligence(self):
        out = run_pipeline({"caseId": 7, "indicatorType": "USERNAME", "indicatorValue": "x_y", "intelligence": []})
        self.assertEqual(len(out["entities"]), 1)
        self.assertEqual(out["candidateRelationships"], [])

    def test_similarity_failure_is_survivable(self):
        with mock.patch("aiml.api.pipeline.analyze", side_effect=RuntimeError("boom")):
            out = run_pipeline(load())
        self.assertEqual(validate_output(out), [])
        self.assertFalse(rels(out, "SIMILAR_WRITING"))
        self.assertTrue(rels(out, "SHARES_PGP_KEY"))

    def test_bad_input_rejected(self):
        with self.assertRaises(ValueError):
            run_pipeline({"intelligence": []})


class ValidatorTests(unittest.TestCase):
    def test_detects_violations(self):
        out = copy.deepcopy(run_pipeline(load()))
        out["score"] = 84.0
        out["candidateRelationships"][0]["relationshipType"] = "NOT_A_TYPE"
        out["evidence"][0]["strength"] = "EXTREME"
        out["evidence"][1]["description"] = "There is a 84% probability this is the same person."
        errs = " | ".join(validate_output(out))
        for frag in ("final score", "bad type", "bad strength", "forbidden language"):
            self.assertIn(frag, errs)

    def test_inferred_rule(self):
        out = copy.deepcopy(run_pipeline(load()))
        for r in out["candidateRelationships"]:
            if r["relationshipType"] == "SIMILAR_WRITING":
                r["assessment"] = "OBSERVED"
        self.assertTrue(any("must be INFERRED" in e for e in validate_output(out)))


if __name__ == "__main__":
    unittest.main()
