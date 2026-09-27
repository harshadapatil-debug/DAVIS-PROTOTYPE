package com.davis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Assessment section of the final DAVIS investigation report.
 *
 * IMPORTANT FOR INTEGRATION:
 * --------------------------------
 * The explanation and limitations should be based on the actual
 * investigation data and backend assessment.
 *
 * This class does NOT calculate confidence.
 * It only carries the assessment information into the report.
 */
public class ReportAssessment {

    private String explanation;

    private List<String> limitations;

    public ReportAssessment() {
        this.limitations = new ArrayList<>();
    }

    public ReportAssessment(
            String explanation,
            List<String> limitations) {

        this.explanation = explanation;
        this.limitations = limitations;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<String> getLimitations() {
        return limitations;
    }

    public void setLimitations(List<String> limitations) {
        this.limitations = limitations;
    }
}