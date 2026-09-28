package com.davis.model;

public class ConfidenceFactor {

    private Long evidenceId;
    private String effect;
    private double contribution;
    private String reason;

    public ConfidenceFactor() {
    }

    public ConfidenceFactor(
            Long evidenceId,
            String effect,
            double contribution,
            String reason
    ) {
        this.evidenceId = evidenceId;
        this.effect = effect;
        this.contribution = contribution;
        this.reason = reason;
    }

    public Long getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(Long evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public double getContribution() {
        return contribution;
    }

    public void setContribution(double contribution) {
        this.contribution = contribution;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}