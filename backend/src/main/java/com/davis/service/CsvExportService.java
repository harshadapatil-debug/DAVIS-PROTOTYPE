package com.davis.service;

import com.davis.model.Case;
import com.davis.model.ConfidenceScore;
import com.davis.model.Entity;
import com.davis.model.Evidence;
import com.davis.model.Indicator;
import com.davis.model.Relationship;
import com.davis.model.Review;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class CsvExportService {

    private final ReportService reportService;

    public CsvExportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public byte[] generateCsv(Long caseId) {

        Map<String, Object> report =
                reportService.generateReport(caseId);

        StringBuilder csv = new StringBuilder();

        // =========================================================
        // CASE
        // =========================================================

        csv.append("SECTION,FIELD,VALUE\n");

        Object caseObject = report.get("case");

        if (caseObject instanceof Case caseData) {

            appendRow(
                    csv,
                    "CASE",
                    "Case ID",
                    caseData.getCaseId()
            );

            appendRow(
                    csv,
                    "CASE",
                    "Case Name",
                    caseData.getCaseName()
            );

            appendRow(
                    csv,
                    "CASE",
                    "Case Type",
                    caseData.getCaseType()
            );

            appendRow(
                    csv,
                    "CASE",
                    "Category",
                    caseData.getCategory()
            );

            appendRow(
                    csv,
                    "CASE",
                    "Status",
                    caseData.getStatus()
            );

            appendRow(
                    csv,
                    "CASE",
                    "Created At",
                    caseData.getCreatedAt()
            );

            appendRow(
                    csv,
                    "CASE",
                    "Last Scan At",
                    caseData.getLastScanAt()
            );

            appendRow(
                    csv,
                    "CASE",
                    "Description",
                    caseData.getDescription()
            );
        }

        // =========================================================
        // KNOWN INDICATOR
        // =========================================================

        csv.append("\n");

        Object indicatorObject =
                report.get("knownIndicator");

        if (indicatorObject instanceof Indicator indicator) {

            appendRow(
                    csv,
                    "KNOWN_INDICATOR",
                    "Indicator ID",
                    indicator.getIndicatorId()
            );

            appendRow(
                    csv,
                    "KNOWN_INDICATOR",
                    "Indicator Type",
                    indicator.getIndicatorType()
            );

            appendRow(
                    csv,
                    "KNOWN_INDICATOR",
                    "Indicator Value",
                    indicator.getIndicatorValue()
            );

            appendRow(
                    csv,
                    "KNOWN_INDICATOR",
                    "Description",
                    indicator.getDescription()
            );
        }

        // =========================================================
        // ENTITIES
        // =========================================================

        csv.append("\n");

        csv.append(
                "ENTITY_ID,ENTITY_TYPE,ENTITY_VALUE,"
                        + "DISCOVERY_CONFIDENCE,DESCRIPTION\n"
        );

        Object entitiesObject =
                report.get("entities");

        if (entitiesObject instanceof List<?> entityList) {

            for (Object item : entityList) {

                if (item instanceof Entity entity) {

                    csv.append(
                            csvValue(entity.getEntityId())
                    ).append(",");

                    csv.append(
                            csvValue(entity.getEntityType())
                    ).append(",");

                    csv.append(
                            csvValue(entity.getEntityValue())
                    ).append(",");

                    csv.append(
                            csvValue(
                                    entity.getDiscoveryConfidence()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(entity.getDescription())
                    ).append("\n");
                }
            }
        }

        // =========================================================
        // RELATIONSHIPS
        // =========================================================

        csv.append("\n");

        csv.append(
                "RELATIONSHIP_ID,SOURCE_ENTITY_ID,"
                        + "TARGET_ENTITY_ID,RELATIONSHIP_TYPE,"
                        + "ASSESSMENT,DESCRIPTION\n"
        );

        Object relationshipsObject =
                report.get("relationships");

        if (relationshipsObject instanceof List<?> relationshipList) {

            for (Object item : relationshipList) {

                if (item instanceof Relationship relationship) {

                    Long sourceEntityId = null;
                    Long targetEntityId = null;

                    if (relationship.getSourceEntity() != null) {
                        sourceEntityId =
                                relationship.getSourceEntity()
                                        .getEntityId();
                    }

                    if (relationship.getTargetEntity() != null) {
                        targetEntityId =
                                relationship.getTargetEntity()
                                        .getEntityId();
                    }

                    csv.append(
                            csvValue(
                                    relationship.getRelationshipId()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(sourceEntityId)
                    ).append(",");

                    csv.append(
                            csvValue(targetEntityId)
                    ).append(",");

                    csv.append(
                            csvValue(
                                    relationship.getRelationshipType()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    relationship.getAssessment()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    relationship.getDescription()
                            )
                    ).append("\n");
                }
            }
        }

        // =========================================================
        // EVIDENCE
        // =========================================================

        csv.append("\n");

        csv.append(
                "EVIDENCE_ID,RELATIONSHIP_ID,EVIDENCE_TYPE,"
                        + "SOURCE,STRENGTH,RELIABILITY,DIRECTION,"
                        + "INDEPENDENCE_GROUP,OBSERVED_AT,DESCRIPTION\n"
        );

        Object evidenceObject =
                report.get("evidence");

        if (evidenceObject instanceof List<?> evidenceList) {

            for (Object item : evidenceList) {

                if (item instanceof Evidence evidence) {

                    Long relationshipId = null;

                    if (evidence.getRelationship() != null) {
                        relationshipId =
                                evidence.getRelationship()
                                        .getRelationshipId();
                    }

                    csv.append(
                            csvValue(
                                    evidence.getEvidenceId()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(relationshipId)
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getEvidenceType()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getSource()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getStrength()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getReliability()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getDirection()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getIndependenceGroup()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getObservedAt()
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    evidence.getDescription()
                            )
                    ).append("\n");
                }
            }
        }

        // =========================================================
        // CONFIDENCE
        // =========================================================

        csv.append("\n");

        csv.append(
                "CONFIDENCE_SCORE,RISK_LEVEL,"
                        + "CALCULATED_AT,EXPLANATION\n"
        );

        Object confidenceObject =
                report.get("confidence");

        if (confidenceObject instanceof ConfidenceScore confidence) {

            csv.append(
                    csvValue(
                            confidence.getScore()
                    )
            ).append(",");

            csv.append(
                    csvValue(
                            confidence.getRiskLevel()
                    )
            ).append(",");

            csv.append(
                    csvValue(
                            confidence.getCalculatedAt()
                    )
            ).append(",");

            csv.append(
                    csvValue(
                            confidence.getExplanation()
                    )
            ).append("\n");
        }

        // =========================================================
        // REVIEW
        // =========================================================

        csv.append("\n");

        csv.append(
                "FINDING_ID,REVIEW_STATUS,NOTE,UPDATED_AT\n"
        );

        Object reviewObject =
                report.get("review");

        if (reviewObject instanceof Review review) {

            csv.append(
                    csvValue(
                            review.getFindingId()
                    )
            ).append(",");

            csv.append(
                    csvValue(
                            review.getStatus()
                    )
            ).append(",");

            csv.append(
                    csvValue(
                            review.getNote()
                    )
            ).append(",");

            csv.append(
                    csvValue(
                            review.getUpdatedAt()
                    )
            ).append("\n");
        }

        // =========================================================
        // TIMELINE
        // =========================================================

        csv.append("\n");

        csv.append(
                "EVIDENCE_ID,TIMESTAMP,TYPE,SOURCE,"
                        + "DIRECTION,STRENGTH,DESCRIPTION\n"
        );

        Object timelineObject =
                report.get("timeline");

        if (timelineObject instanceof List<?> timelineList) {

            for (Object item : timelineList) {

                if (item instanceof Map<?, ?> event) {

                    csv.append(
                            csvValue(
                                    event.get("evidenceId")
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    event.get("timestamp")
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    event.get("type")
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    event.get("source")
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    event.get("direction")
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    event.get("strength")
                            )
                    ).append(",");

                    csv.append(
                            csvValue(
                                    event.get("description")
                            )
                    ).append("\n");
                }
            }
        }

        // =========================================================
        // DISCLAIMER
        // =========================================================

        csv.append("\n");
        csv.append("DISCLAIMER\n");

        csv.append(
                csvValue(
                        report.get("disclaimer")
                )
        ).append("\n");

        return csv.toString()
                .getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(
            StringBuilder csv,
            String section,
            String field,
            Object value) {

        csv.append(
                csvValue(section)
        ).append(",");

        csv.append(
                csvValue(field)
        ).append(",");

        csv.append(
                csvValue(value)
        ).append("\n");
    }

    private String csvValue(Object value) {

        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);

        text = text
                .replace("\r", " ")
                .replace("\n", " ");

        text = text.replace("\"", "\"\"");

        return "\"" + text + "\"";
    }
}