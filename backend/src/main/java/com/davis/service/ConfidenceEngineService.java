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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfidenceEngineService {

    private final CaseRepository caseRepository;
    private final RelationshipRepository relationshipRepository;
    private final EvidenceRepository evidenceRepository;
    private final ConfidenceScoreRepository confidenceScoreRepository;

    public ConfidenceEngineService(
            CaseRepository caseRepository,
            RelationshipRepository relationshipRepository,
            EvidenceRepository evidenceRepository,
            ConfidenceScoreRepository confidenceScoreRepository) {

        this.caseRepository = caseRepository;
        this.relationshipRepository = relationshipRepository;
        this.evidenceRepository = evidenceRepository;
        this.confidenceScoreRepository = confidenceScoreRepository;
    }

    @Transactional
    public ConfidenceScore evaluateConfidence(Long caseId) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException("Case not found: " + caseId));

        List<Relationship> relationships = relationshipRepository.findAll()
                .stream()
                .filter(relationship ->
                        relationship.getCaseEntity()
                                .getCaseId()
                                .equals(caseId))
                .toList();

        Set<Long> relationshipIds = relationships.stream()
                .map(Relationship::getRelationshipId)
                .collect(Collectors.toSet());

        List<Evidence> evidenceList = evidenceRepository.findAll()
                .stream()
                .filter(evidence ->
                        evidence.getRelationship() != null
                                && relationshipIds.contains(
                                evidence.getRelationship()
                                        .getRelationshipId()))
                .toList();

        double score = calculateScore(evidenceList);

        String riskLevel = determineRiskLevel(score);

        String explanation = buildExplanation(
                score,
                riskLevel,
                evidenceList
        );

        String factorsJson = buildFactorsJson(evidenceList);

        Optional<ConfidenceScore> existingScore =
                confidenceScoreRepository.findAll()
                        .stream()
                        .filter(item ->
                                item.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .findFirst();

        ConfidenceScore confidenceScore =
                existingScore.orElseGet(ConfidenceScore::new);

        confidenceScore.setCaseEntity(caseEntity);

        confidenceScore.setScore(
                BigDecimal.valueOf(score)
                        .setScale(2, RoundingMode.HALF_UP)
        );

        confidenceScore.setRiskLevel(riskLevel);
        confidenceScore.setExplanation(explanation);
        confidenceScore.setFactorsJson(factorsJson);
        confidenceScore.setCalculatedAt(LocalDateTime.now());

        return confidenceScoreRepository.save(confidenceScore);
    }

    public double calculateScore(List<Evidence> evidenceList) {

        if (evidenceList.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> strongestByGroup = new HashMap<>();

        for (Evidence evidence : evidenceList) {

            double baseWeight =
                    getEvidenceTypeWeight(evidence.getEvidenceType());

            double strengthFactor =
                    getLevelFactor(evidence.getStrength());

            double reliabilityFactor =
                    getLevelFactor(evidence.getReliability());

            double recencyFactor =
                    getRecencyFactor(evidence.getObservedAt());

            double directionFactor =
                    getDirectionFactor(evidence.getDirection());

            double contribution =
                    baseWeight
                            * strengthFactor
                            * reliabilityFactor
                            * recencyFactor
                            * directionFactor;

            String group = evidence.getIndependenceGroup();

            if (group == null || group.isBlank()) {
                group = "UNSPECIFIED_" + evidence.getEvidenceId();
            }

            if (!strongestByGroup.containsKey(group)
                    || contribution > strongestByGroup.get(group)) {

                strongestByGroup.put(group, contribution);
            }
        }

        double total = strongestByGroup.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        double score = Math.min(total, 100.0);

        return Math.max(score, 0.0);
    }

    private double getEvidenceTypeWeight(String evidenceType) {

        if (evidenceType == null) {
            return 0.0;
        }

        String type = evidenceType.toUpperCase();

        if (type.contains("PGP")) {
            return 25.0;
        }

        if (type.contains("WALLET")
                || type.contains("TRANSACTION")) {
            return 20.0;
        }

        if (type.contains("EMAIL")) {
            return 20.0;
        }

        if (type.contains("INFRASTRUCTURE")
                || type.contains("SSL")
                || type.contains("CERTIFICATE")) {
            return 20.0;
        }

        if (type.contains("STYLOMETRIC")
                || type.contains("BEHAVIOURAL")
                || type.contains("BEHAVIORAL")) {
            return 10.0;
        }

        if (type.contains("HANDLE")
                || type.contains("USERNAME")
                || type.contains("MARKETPLACE")) {
            return 5.0;
        }

        return 5.0;
    }

    private double getLevelFactor(String level) {

        if (level == null) {
            return 0.0;
        }

        return switch (level.toUpperCase()) {
            case "HIGH" -> 1.0;
            case "MEDIUM" -> 0.7;
            case "LOW" -> 0.4;
            default -> 0.0;
        };
    }

    private double getDirectionFactor(String direction) {

        if (direction == null) {
            return 0.0;
        }

        return switch (direction.toUpperCase()) {
            case "SUPPORTS" -> 1.0;
            case "WEAKENS" -> -0.5;
            case "CONTRADICTS" -> -1.0;
            case "NEUTRAL" -> 0.0;
            default -> 0.0;
        };
    }

    private double getRecencyFactor(LocalDateTime observedAt) {

        if (observedAt == null) {
            return 0.5;
        }

        long daysOld =
                Math.max(
                        0,
                        ChronoUnit.DAYS.between(
                                observedAt,
                                LocalDateTime.now()
                        )
                );

        if (daysOld <= 7) {
            return 1.0;
        }

        if (daysOld <= 30) {
            return 0.9;
        }

        if (daysOld <= 90) {
            return 0.75;
        }

        return 0.6;
    }

    public String determineRiskLevel(double score) {

        if (score >= 75.0) {
            return "HIGH";
        }

        if (score >= 50.0) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String buildExplanation(
            double score,
            String riskLevel,
            List<Evidence> evidenceList) {

        long supporting =
                evidenceList.stream()
                        .filter(evidence ->
                                "SUPPORTS".equalsIgnoreCase(
                                        evidence.getDirection()))
                        .count();

        long contradicting =
                evidenceList.stream()
                        .filter(evidence ->
                                "CONTRADICTS".equalsIgnoreCase(
                                        evidence.getDirection()))
                        .count();

        return String.format(
                Locale.US,
                "Attribution confidence score: %.2f/100. "
                        + "Risk level: %s. "
                        + "%d supporting signal(s) and "
                        + "%d contradicting signal(s) were considered. "
                        + "The score is an explainable prototype assessment "
                        + "and is not a calibrated probability or identity proof.",
                score,
                riskLevel,
                supporting,
                contradicting
        );
    }

    private String buildFactorsJson(List<Evidence> evidenceList) {

        List<String> factors = new ArrayList<>();

        for (Evidence evidence : evidenceList) {

            double weight =
                    getEvidenceTypeWeight(evidence.getEvidenceType());

            factors.add(
                    String.format(
                            Locale.US,
                            "{\"evidenceId\":%d,\"type\":\"%s\",\"weight\":%.2f,\"strength\":\"%s\",\"reliability\":\"%s\",\"direction\":\"%s\"}",
                            evidence.getEvidenceId(),
                            escapeJson(evidence.getEvidenceType()),
                            weight,
                            escapeJson(evidence.getStrength()),
                            escapeJson(evidence.getReliability()),
                            escapeJson(evidence.getDirection())
                    )
            );
        }

        return "[" + String.join(",", factors) + "]";
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}