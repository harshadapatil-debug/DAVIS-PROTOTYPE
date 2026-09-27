package com.davis.service;

import com.davis.model.Case;
import com.davis.model.ConfidenceScore;
import com.davis.model.Evidence;
import com.davis.model.Relationship;
import com.davis.repository.CaseRepository;
import com.davis.repository.ConfidenceScoreRepository;
import com.davis.repository.EvidenceRepository;
import com.davis.repository.RelationshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StressTestService {

    private final CaseRepository caseRepository;
    private final RelationshipRepository relationshipRepository;
    private final EvidenceRepository evidenceRepository;
    private final ConfidenceScoreRepository confidenceScoreRepository;
    private final ConfidenceEngineService confidenceEngineService;

    public StressTestService(
            CaseRepository caseRepository,
            RelationshipRepository relationshipRepository,
            EvidenceRepository evidenceRepository,
            ConfidenceScoreRepository confidenceScoreRepository,
            ConfidenceEngineService confidenceEngineService) {

        this.caseRepository = caseRepository;
        this.relationshipRepository = relationshipRepository;
        this.evidenceRepository = evidenceRepository;
        this.confidenceScoreRepository = confidenceScoreRepository;
        this.confidenceEngineService = confidenceEngineService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> runStressTest(
            Long caseId,
            Long evidenceId) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Case not found: " + caseId));

        List<Relationship> relationships =
                relationshipRepository.findAll()
                        .stream()
                        .filter(relationship ->
                                relationship.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .toList();

        Set<Long> relationshipIds = relationships.stream()
                .map(Relationship::getRelationshipId)
                .collect(Collectors.toSet());

        List<Evidence> caseEvidence =
                evidenceRepository.findAll()
                        .stream()
                        .filter(evidence ->
                                evidence.getRelationship() != null
                                        && relationshipIds.contains(
                                        evidence.getRelationship()
                                                .getRelationshipId()))
                        .toList();

        Evidence challengedEvidence =
                caseEvidence.stream()
                        .filter(evidence ->
                                evidence.getEvidenceId()
                                        .equals(evidenceId))
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Evidence not found in case: "
                                                + evidenceId));

        double baselineScore =
                confidenceEngineService.calculateScore(
                        caseEvidence);

        List<Evidence> challengedEvidenceList =
                caseEvidence.stream()
                        .filter(evidence ->
                                !evidence.getEvidenceId()
                                        .equals(evidenceId))
                        .toList();

        double challengedScore =
                confidenceEngineService.calculateScore(
                        challengedEvidenceList);

        String baselineRisk =
                confidenceEngineService.determineRiskLevel(
                        baselineScore);

        String challengedRisk =
                confidenceEngineService.determineRiskLevel(
                        challengedScore);

        double scoreChange =
                challengedScore - baselineScore;

        double robustnessIndicator =
                calculateRobustnessIndicator(baselineScore, challengedScore);

        String explanation =
                buildExplanation(
                        challengedEvidence,
                        baselineScore,
                        challengedScore,
                        scoreChange,
                        baselineRisk,
                        challengedRisk,
                        robustnessIndicator
                );

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("caseId", caseEntity.getCaseId());
        result.put("baselineScore", round(baselineScore));
        result.put("challengedScore", round(challengedScore));
        result.put("scoreChange", round(scoreChange));

        result.put("baselineRiskLevel", baselineRisk);
        result.put("challengedRiskLevel", challengedRisk);

        result.put("removedEvidenceId",
                challengedEvidence.getEvidenceId());

        result.put("removedEvidenceType",
                challengedEvidence.getEvidenceType());

        result.put("robustnessIndicator",
                round(robustnessIndicator));

        result.put("explanation", explanation);

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> resetStressTest(Long caseId) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Case not found: " + caseId));

        ConfidenceScore confidenceScore =
                confidenceScoreRepository
                        .findConfidenceByCaseId(caseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No confidence score exists for case: "
                                                + caseId));

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("caseId", caseEntity.getCaseId());
        result.put("currentScore", confidenceScore.getScore());
        result.put("currentRiskLevel", confidenceScore.getRiskLevel());
        result.put("status", "RESET");
        result.put(
                "message",
                "Stress test reset. Original confidence assessment restored."
        );

        return result;
    }

    private double calculateRobustnessIndicator(
            double baselineScore,
            double challengedScore) {

        if (baselineScore <= 0.0) {
            return 0.0;
        }

        return (challengedScore / baselineScore) * 100.0;
    }

    private String buildExplanation(
            Evidence evidence,
            double baselineScore,
            double challengedScore,
            double scoreChange,
            String baselineRisk,
            String challengedRisk,
            double robustnessIndicator) {

        return String.format(
                "Stress test temporarily removed evidence %d (%s). "
                        + "The attribution-confidence score changed from %.2f "
                        + "to %.2f (change: %.2f). "
                        + "Risk level changed from %s to %s. "
                        + "Robustness indicator: %.2f. "
                        + "The evidence was not permanently deleted.",
                evidence.getEvidenceId(),
                evidence.getEvidenceType(),
                baselineScore,
                challengedScore,
                scoreChange,
                baselineRisk,
                challengedRisk,
                robustnessIndicator
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}