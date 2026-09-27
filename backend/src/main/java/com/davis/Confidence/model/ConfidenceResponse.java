package com.davis.model;

import java.time.LocalDateTime;
import java.util.List;

public class ConfidenceResponse {

    private Long caseId;
    private double score;
    private String riskLevel;
    private String explanation;
    private LocalDateTime calculatedAt;
    private List<ConfidenceFactor> factors;

    public ConfidenceResponse() {
    }

    public ConfidenceResponse(
            Long caseId,
            double score,
            String riskLevel,
            String explanation,
            LocalDateTime calculatedAt,
            List<ConfidenceFactor> factors
    ) {
        this.caseId = caseId;
        this.score = score;
        this.riskLevel = riskLevel;
        this.explanation = explanation;
        this.calculatedAt = calculatedAt;
        this.factors = factors;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public void setCalculatedAt(LocalDateTime calculatedAt) {
        this.calculatedAt = calculatedAt;
    }

    public List<ConfidenceFactor> getFactors() {
        return factors;
    }

    public void setFactors(List<ConfidenceFactor> factors) {
        this.factors = factors;
    }
}