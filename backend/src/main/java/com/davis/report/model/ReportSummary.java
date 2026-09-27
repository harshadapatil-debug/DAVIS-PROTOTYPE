package com.davis.model;

/**
 * Summary section of the final DAVIS investigation report.
 *
 * IMPORTANT FOR INTEGRATION:
 * --------------------------------
 * This class must contain ONLY data received from the existing backend.
 *
 * Do NOT hard-code:
 * - indicator values
 * - entity count
 * - relationship count
 * - evidence count
 * - confidence score
 * - risk level
 * - review status
 *
 * The integration member should populate these values from the existing
 * DAVIS investigation data.
 */
public class ReportSummary {

    private String knownIndicator;

    private int entityCount;

    private int relationshipCount;

    private int evidenceCount;

    private double confidenceScore;

    private String riskLevel;

    private String reviewStatus;

    public ReportSummary() {
    }

    public ReportSummary(
            String knownIndicator,
            int entityCount,
            int relationshipCount,
            int evidenceCount,
            double confidenceScore,
            String riskLevel,
            String reviewStatus) {

        this.knownIndicator = knownIndicator;
        this.entityCount = entityCount;
        this.relationshipCount = relationshipCount;
        this.evidenceCount = evidenceCount;
        this.confidenceScore = confidenceScore;
        this.riskLevel = riskLevel;
        this.reviewStatus = reviewStatus;
    }

    public String getKnownIndicator() {
        return knownIndicator;
    }

    public void setKnownIndicator(String knownIndicator) {
        this.knownIndicator = knownIndicator;
    }

    public int getEntityCount() {
        return entityCount;
    }

    public void setEntityCount(int entityCount) {
        this.entityCount = entityCount;
    }

    public int getRelationshipCount() {
        return relationshipCount;
    }

    public void setRelationshipCount(int relationshipCount) {
        this.relationshipCount = relationshipCount;
    }

    public int getEvidenceCount() {
        return evidenceCount;
    }

    public void setEvidenceCount(int evidenceCount) {
        this.evidenceCount = evidenceCount;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }
}