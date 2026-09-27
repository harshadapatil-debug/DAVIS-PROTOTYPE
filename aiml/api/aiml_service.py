"""
aiml_service.py
-----------------
The single entry point into the AIML layer.

    CONTROLLED INTELLIGENCE (JSON)
            |
            v
    ENTITY EXTRACTION            (aiml/entity_extraction)
            |
            v
    SIMILARITY ANALYSIS          (aiml/similarity, run inside
            |                     relationship_detection/persona_correlation)
            v
    RELATIONSHIP DETECTION       (aiml/relationship_detection)
            |
            v
    SUPPORTING EVIDENCE
            |
            v
    NORMALIZED AIML OUTPUT  ->  {"entities": [...],
                                  "candidateRelationships": [...],
                                  "evidence": [...]}
            |
            v
    SPRING BOOT BACKEND (out of scope for this module - see README)

This module is intentionally a PLAIN PYTHON class/function, with no web
framework attached. That keeps the dependency footprint minimal, as
required by the project brief. A Spring Boot backend (or any caller) can
integrate with it in one of two simple ways - both documented in the
README:

    1. Invoke this file as a CLI script, which writes the normalized
       output to a JSON file the backend can read.
    2. Import `AimlService` directly if the backend calls into this code
       via a thin Python process/service wrapper.

The AIML layer never makes a final attribution decision, never assigns a
final confidence score, and never claims to prove identity. It only
produces structured entities, candidate relationships and supporting
evidence for a human investigator (and the backend's own confidence
engine) to review.
"""

import argparse
import json
import os
from typing import Dict, List

from aiml.entity_extraction.entity_extractor import extract_all_entities
from aiml.relationship_detection.relationship_detector import detect_all_relationships


class AimlService:
    """
    Orchestrates the full AIML pipeline over a controlled intelligence
    dataset and produces one normalized output structure.
    """

    def __init__(self, dataset_path: str):
        self.dataset_path = dataset_path
        self._dataset: Dict = {}

    def load_dataset(self) -> Dict:
        with open(self.dataset_path, "r", encoding="utf-8") as f:
            self._dataset = json.load(f)
        return self._dataset

    def get_records(self) -> List[Dict]:
        if not self._dataset:
            self.load_dataset()
        return self._dataset.get("records", [])

    def process(self) -> Dict:
        """
        Run the full pipeline and return the normalized AIML output:

            {
                "entities": [...],
                "candidateRelationships": [...],
                "evidence": [...]
            }

        This is the exact structure the Spring Boot backend is expected
        to consume (see docs in the README for the integration contract).
        """
        records = self.get_records()

        entities = extract_all_entities(records)
        relationships, evidence = detect_all_relationships(records)

        return {
            "entities": entities,
            "candidateRelationships": relationships,
            "evidence": evidence,
        }


def run_pipeline(dataset_path: str) -> Dict:
    """Convenience function: build a service and run it in one call."""
    service = AimlService(dataset_path)
    return service.process()


def main() -> None:
    """
    CLI entry point.

    Example:
        python -m aiml.api.aiml_service \\
            --dataset data/intelligence/synthetic_intelligence.json \\
            --output aiml_output.json
    """
    parser = argparse.ArgumentParser(
        description="Run the DAVIS AIML pipeline over a controlled intelligence dataset."
    )
    parser.add_argument(
        "--dataset",
        default="data/intelligence/synthetic_intelligence.json",
        help="Path to the controlled intelligence JSON dataset.",
    )
    parser.add_argument(
        "--output",
        default="aiml_output.json",
        help="Path to write the normalized AIML output JSON.",
    )
    args = parser.parse_args()

    output = run_pipeline(args.dataset)

    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2)

    print(f"Entities:               {len(output['entities'])}")
    print(f"Candidate relationships: {len(output['candidateRelationships'])}")
    print(f"Evidence objects:        {len(output['evidence'])}")
    print(f"Normalized output written to: {os.path.abspath(args.output)}")


if __name__ == "__main__":
    main()
