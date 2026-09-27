package com.davis.report.export;

import com.davis.report.model.ReportData;
import org.springframework.stereotype.Component;

/**
 * CSV EXPORTER
 *
 * Converts actual investigation data into CSV.
 *
 * No investigation data is hard-coded.
 */
@Component
public class CsvReportExporter {


    public String generate(ReportData data) {

        StringBuilder csv =
                new StringBuilder();


        // ========================================================
        // REQUIRED CSV HEADER
        // ========================================================

        csv.append(
                "case_id,"
                        + "case_name,"
                        + "indicator_type,"
                        + "indicator_value,"
                        + "entity_id,"
                        + "entity_type,"
                        + "entity_value,"
                        + "relationship_id,"
                        + "relationship_type,"
                        + "evidence_id,"
                        + "evidence_type,"
                        + "source,"
                        + "strength,"
                        + "reliability,"
                        + "direction,"
                        + "observed_at,"
                        + "confidence_score,"
                        + "risk_level,"
                        + "review_status\n"
        );


        /*
         * Each evidence row represents the relationship between
         * the case, entities, relationships and evidence.
         *
         * Since the exact backend entity/evidence mapping is part
         * of the integration, the prototype leader can adjust this
         * mapping to match the actual investigation model.
         */

        if (data.getEvidence() == null ||
                data.getEvidence().isEmpty()) {

            csv.append(
                    row(
                            data.getCaseId(),
                            data.getCaseName(),
                            data.getIndicatorType(),
                            data.getIndicatorValue(),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            data.getConfidenceScore(),
                            data.getRiskLevel(),
                            data.getReviewStatus()
                    )
            );

            return csv.toString();
        }


        for (ReportData.EvidenceData evidence :
                data.getEvidence()) {

            csv.append(
                    row(
                            data.getCaseId(),
                            data.getCaseName(),
                            data.getIndicatorType(),
                            data.getIndicatorValue(),

                            // ------------------------------------------------
                            // INTEGRATION POINT
                            // ------------------------------------------------
                            // These entity/relationship IDs should be populated
                            // from the actual backend evidence linkage.
                            // ------------------------------------------------
                            "",
                            "",
                            "",
                            "",
                            "",

                            evidence.getId(),
                            evidence.getType(),
                            evidence.getSource(),
                            evidence.getStrength(),
                            evidence.getReliability(),
                            evidence.getDirection(),
                            evidence.getObservedAt(),

                            data.getConfidenceScore(),
                            data.getRiskLevel(),
                            data.getReviewStatus()
                    )
            );
        }


        return csv.toString();
    }


    private String row(
            Object caseId,
            Object caseName,
            Object indicatorType,
            Object indicatorValue,
            Object entityId,
            Object entityType,
            Object entityValue,
            Object relationshipId,
            Object relationshipType,
            Object evidenceId,
            Object evidenceType,
            Object source,
            Object strength,
            Object reliability,
            Object direction,
            Object observedAt,
            Object confidenceScore,
            Object riskLevel,
            Object reviewStatus) {

        return String.join(
                ",",

                csv(caseId),
                csv(caseName),
                csv(indicatorType),
                csv(indicatorValue),

                csv(entityId),
                csv(entityType),
                csv(entityValue),

                csv(relationshipId),
                csv(relationshipType),

                csv(evidenceId),
                csv(evidenceType),
                csv(source),
                csv(strength),
                csv(reliability),
                csv(direction),
                csv(observedAt),

                csv(confidenceScore),
                csv(riskLevel),
                csv(reviewStatus)
        ) + "\n";
    }


    /**
     * Properly escapes CSV values containing:
     * comma
     * quotes
     * newline
     */
    private String csv(Object value) {

        if (value == null) {
            return "";
        }

        String text =
                String.valueOf(value);

        text = text.replace(
                "\"",
                "\"\""
        );

        if (text.contains(",")
                || text.contains("\"")
                || text.contains("\n")
                || text.contains("\r")) {

            return "\"" + text + "\"";
        }

        return text;
    }
}