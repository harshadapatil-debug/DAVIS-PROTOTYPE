package com.davis.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * FINAL REPORT DATA MODEL
 *
 * This class represents the complete investigation information
 * required by the Report & Export module.
 *
 * IMPORTANT INTEGRATION NOTE:
 *
 * This class does NOT create investigation data.
 *
 * The final backend/integration owner must populate this object
 * using the existing DAVIS backend data.
 *
 * Do NOT add hard-coded case/entity/evidence/confidence values here.
 */
public class ReportData {

    // ============================================================
    // CASE INFORMATION
    // ============================================================

    private Long caseId;
    private String caseName;
    private String caseType;
    private String caseDescription;
    private String generatedAt;

    // ============================================================
    // KNOWN INDICATOR
    // ============================================================

    private String indicatorType;
    private String indicatorValue;

    // ============================================================
    // REPORT SUMMARY
    // ============================================================

    private String investigationSummary;

    // ============================================================
    // INVESTIGATION DATA
    // ============================================================

    private List<EntityData> entities = new ArrayList<>();
    private List<RelationshipData> relationships = new ArrayList<>();
    private List<EvidenceData> evidence = new ArrayList<>();
    private List<TimelineData> timeline = new ArrayList<>();

    // ============================================================
    // CONFIDENCE
    // ============================================================

    private Double confidenceScore;
    private String riskLevel;
    private String confidenceExplanation;

    // ============================================================
    // REVIEW
    // ============================================================

    private String reviewStatus;
    private String reviewerName;
    private String reviewRemarks;

    // ============================================================
    // STRESS TEST
    // ============================================================

    private String stressTestStatus;
    private String stressTestResult;

    // ============================================================
    // LIMITATIONS
    // ============================================================

    private List<String> limitations = new ArrayList<>();

    // ============================================================
    // GETTERS AND SETTERS
    // ============================================================

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public String getCaseDescription() {
        return caseDescription;
    }

    public void setCaseDescription(String caseDescription) {
        this.caseDescription = caseDescription;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getIndicatorType() {
        return indicatorType;
    }

    public void setIndicatorType(String indicatorType) {
        this.indicatorType = indicatorType;
    }

    public String getIndicatorValue() {
        return indicatorValue;
    }

    public void setIndicatorValue(String indicatorValue) {
        this.indicatorValue = indicatorValue;
    }

    public String getInvestigationSummary() {
        return investigationSummary;
    }

    public void setInvestigationSummary(String investigationSummary) {
        this.investigationSummary = investigationSummary;
    }

    public List<EntityData> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityData> entities) {
        this.entities = entities;
    }

    public List<RelationshipData> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<RelationshipData> relationships) {
        this.relationships = relationships;
    }

    public List<EvidenceData> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<EvidenceData> evidence) {
        this.evidence = evidence;
    }

    public List<TimelineData> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineData> timeline) {
        this.timeline = timeline;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getConfidenceExplanation() {
        return confidenceExplanation;
    }

    public void setConfidenceExplanation(String confidenceExplanation) {
        this.confidenceExplanation = confidenceExplanation;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewRemarks() {
        return reviewRemarks;
    }

    public void setReviewRemarks(String reviewRemarks) {
        this.reviewRemarks = reviewRemarks;
    }

    public String getStressTestStatus() {
        return stressTestStatus;
    }

    public void setStressTestStatus(String stressTestStatus) {
        this.stressTestStatus = stressTestStatus;
    }

    public String getStressTestResult() {
        return stressTestResult;
    }

    public void setStressTestResult(String stressTestResult) {
        this.stressTestResult = stressTestResult;
    }

    public List<String> getLimitations() {
        return limitations;
    }

    public void setLimitations(List<String> limitations) {
        this.limitations = limitations;
    }

    // ============================================================
    // NESTED DATA CLASSES
    // ============================================================

    public static class EntityData {

        private String id;
        private String type;
        private String value;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public static class RelationshipData {

        private String id;
        private String sourceEntity;
        private String relationshipType;
        private String targetEntity;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSourceEntity() {
            return sourceEntity;
        }

        public void setSourceEntity(String sourceEntity) {
            this.sourceEntity = sourceEntity;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }

        public String getTargetEntity() {
            return targetEntity;
        }

        public void setTargetEntity(String targetEntity) {
            this.targetEntity = targetEntity;
        }
    }


    public static class EvidenceData {

        private String id;
        private String type;
        private String description;
        private String source;
        private String strength;
        private String reliability;
        private String direction;
        private String observedAt;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getStrength() {
            return strength;
        }

        public void setStrength(String strength) {
            this.strength = strength;
        }

        public String getReliability() {
            return reliability;
        }

        public void setReliability(String reliability) {
            this.reliability = reliability;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getObservedAt() {
            return observedAt;
        }

        public void setObservedAt(String observedAt) {
            this.observedAt = observedAt;
        }
    }


    public static class TimelineData {

        private String timestamp;
        private String event;
        private String source;

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}