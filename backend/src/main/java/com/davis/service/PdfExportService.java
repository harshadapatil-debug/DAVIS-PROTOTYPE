package com.davis.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    private final ReportService reportService;

    public PdfExportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public byte[] generatePdf(Long caseId) throws IOException {

        Map<String, Object> report =
                reportService.generateReport(caseId);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(document);

            // =========================================================
            // TITLE
            // =========================================================

            writer.writeTitle("DAVIS Investigation Report");

            writer.writeLine(
                    "Generated from the controlled investigation dataset."
            );

            writer.writeLine("");

            // =========================================================
            // 1. CASE INFORMATION
            // =========================================================

            writer.writeHeading("1. CASE INFORMATION");

            Object caseObject = report.get("case");

            if (caseObject instanceof Map<?, ?> caseData) {

                writer.writeField(
                        "Case ID",
                        caseData.get("caseId")
                );

                writer.writeField(
                        "Case Name",
                        caseData.get("caseName")
                );

                writer.writeField(
                        "Case Type",
                        caseData.get("caseType")
                );

                writer.writeField(
                        "Category",
                        caseData.get("category")
                );

                writer.writeField(
                        "Status",
                        caseData.get("status")
                );

                writer.writeField(
                        "Created At",
                        caseData.get("createdAt")
                );

                writer.writeField(
                        "Last Scan At",
                        caseData.get("lastScanAt")
                );
            }

            writer.writeLine("");

            // =========================================================
            // 2. KNOWN INDICATOR
            // =========================================================

            writer.writeHeading("2. KNOWN INDICATOR");

            Object indicator =
                    report.get("knownIndicator");

            if (indicator instanceof Map<?, ?> indicatorData) {

                writer.writeField(
                        "Indicator Type",
                        indicatorData.get("indicatorType")
                );

                writer.writeField(
                        "Indicator Value",
                        indicatorData.get("indicatorValue")
                );
            } else {
                writer.writeLine("No known indicator available.");
            }

            writer.writeLine("");

            // =========================================================
            // 3. INVESTIGATION SUMMARY
            // =========================================================

            writer.writeHeading("3. INVESTIGATION SUMMARY");

            Object summary =
                    report.get("summary");

            if (summary instanceof Map<?, ?> summaryData) {

                writer.writeField(
                        "Indicators",
                        summaryData.get("indicatorCount")
                );

                writer.writeField(
                        "Entities",
                        summaryData.get("entityCount")
                );

                writer.writeField(
                        "Relationships",
                        summaryData.get("relationshipCount")
                );

                writer.writeField(
                        "Evidence Records",
                        summaryData.get("evidenceCount")
                );

                /*
                 * The current InvestigationService does not expose
                 * timelineEventCount in the summary.
                 *
                 * Therefore we calculate it from the actual timeline
                 * instead of displaying a missing/null value.
                 */
                Object timelineObject =
                        report.get("timeline");

                int timelineCount = 0;

                if (timelineObject instanceof List<?> timelineList) {
                    timelineCount = timelineList.size();
                }

                writer.writeField(
                        "Timeline Events",
                        timelineCount
                );
            }

            writer.writeLine("");

            // =========================================================
            // 4. DISCOVERED ENTITIES
            // =========================================================

            writer.writeHeading("4. DISCOVERED ENTITIES");

            Object entities =
                    report.get("entities");

            if (entities instanceof List<?> entityList
                    && !entityList.isEmpty()) {

                for (Object item : entityList) {

                    if (item instanceof Map<?, ?> entity) {

                        writer.writeLine(
                                String.valueOf(
                                        entity.get("entityId"))
                                        + " | "
                                        + String.valueOf(
                                        entity.get("entityType"))
                                        + " | "
                                        + String.valueOf(
                                        entity.get("entityValue"))
                        );

                        Object description =
                                entity.get("description");

                        if (description != null
                                && !String.valueOf(
                                description).isBlank()) {

                            writer.writeIndented(
                                    "Description",
                                    description
                            );
                        }

                        Object discoveryConfidence =
                                entity.get(
                                        "discoveryConfidence");

                        if (discoveryConfidence != null) {

                            writer.writeIndented(
                                    "Discovery Confidence",
                                    discoveryConfidence
                            );
                        }
                    }
                }

            } else {

                writer.writeLine(
                        "No entities were discovered."
                );
            }

            writer.writeLine("");

            // =========================================================
            // 5. RELATIONSHIPS
            // =========================================================

            writer.writeHeading("5. RELATIONSHIPS");

            Object relationships =
                    report.get("relationships");

            if (relationships instanceof List<?> relationshipList
                    && !relationshipList.isEmpty()) {

                for (Object item : relationshipList) {

                    if (item instanceof Map<?, ?> relationship) {

                        writer.writeLine(
                                String.valueOf(
                                        relationship.get(
                                                "relationshipId"))
                                        + " | "
                                        + String.valueOf(
                                        relationship.get(
                                                "relationshipType"))
                                        + " | "
                                        + String.valueOf(
                                        relationship.get(
                                                "assessment"))
                        );

                        writer.writeIndented(
                                "Source Entity",
                                relationship.get("source")
                        );

                        writer.writeIndented(
                                "Target Entity",
                                relationship.get("target")
                        );

                        Object description =
                                relationship.get(
                                        "description");

                        if (description != null
                                && !String.valueOf(
                                description).isBlank()) {

                            writer.writeIndented(
                                    "Description",
                                    description
                            );
                        }
                    }
                }

            } else {

                writer.writeLine(
                        "No relationships were discovered."
                );
            }

            writer.writeLine("");

            // =========================================================
            // 6. SUPPORTING EVIDENCE
            // =========================================================

            writer.writeHeading("6. SUPPORTING EVIDENCE");

            Object evidence =
                    report.get("evidence");

            if (evidence instanceof List<?> evidenceList
                    && !evidenceList.isEmpty()) {

                for (Object item : evidenceList) {

                    if (item instanceof Map<?, ?> evidenceData) {

                        writer.writeLine(
                                "Evidence "
                                        + String.valueOf(
                                        evidenceData.get(
                                                "evidenceId"))
                                        + " | "
                                        + String.valueOf(
                                        evidenceData.get(
                                                "evidenceType"))
                                        + " | Direction: "
                                        + String.valueOf(
                                        evidenceData.get(
                                                "direction"))
                        );

                        writer.writeIndented(
                                "Source",
                                evidenceData.get("source")
                        );

                        writer.writeIndented(
                                "Strength",
                                evidenceData.get("strength")
                        );

                        writer.writeIndented(
                                "Reliability",
                                evidenceData.get("reliability")
                        );

                        Object description =
                                evidenceData.get(
                                        "description");

                        if (description != null
                                && !String.valueOf(
                                description).isBlank()) {

                            writer.writeIndented(
                                    "Description",
                                    description
                            );
                        }

                        writer.writeLine("");
                    }
                }

            } else {

                writer.writeLine(
                        "No evidence records available."
                );
            }

            // =========================================================
            // 7. ATTRIBUTION-CONFIDENCE ASSESSMENT
            // =========================================================

            writer.writeHeading(
                    "7. ATTRIBUTION-CONFIDENCE ASSESSMENT"
            );

            Object confidence =
                    report.get("confidence");

            if (confidence instanceof Map<?, ?> confidenceData) {

                writer.writeField(
                        "Score",
                        confidenceData.get("score")
                                + " / 100"
                );

                writer.writeField(
                        "Risk Level",
                        confidenceData.get(
                                "riskLevel")
                );

                writer.writeField(
                        "Supporting Evidence",
                        confidenceData.get(
                                "supportingEvidenceCount")
                );

                writer.writeField(
                        "Contradicting Evidence",
                        confidenceData.get(
                                "contradictingEvidenceCount")
                );

                writer.writeField(
                        "Calculated At",
                        confidenceData.get(
                                "calculatedAt")
                );

                writer.writeLine("");

                writer.writeLabel(
                        "Explanation"
                );

                writer.writeLine(
                        String.valueOf(
                                confidenceData.get(
                                        "explanation"))
                );

            } else {

                writer.writeLine(
                        "No confidence assessment available."
                );
            }

            writer.writeLine("");

            // =========================================================
            // 8. ANALYST REVIEW
            // =========================================================

            writer.writeHeading("8. ANALYST REVIEW");

            Object review =
                    report.get("review");

            if (review instanceof Map<?, ?> reviewData) {

                writer.writeField(
                        "Status",
                        reviewData.get("status")
                );

                writer.writeField(
                        "Note",
                        reviewData.get("note")
                );

                writer.writeField(
                        "Reviewed At",
                        reviewData.get("reviewedAt")
                );

            } else {

                writer.writeLine(
                        "No analyst review recorded."
                );
            }

            writer.writeLine("");

            // =========================================================
            // 9. TIMELINE
            // =========================================================

            writer.writeHeading("9. INVESTIGATION TIMELINE");

            Object timeline =
                    report.get("timeline");

            if (timeline instanceof List<?> timelineList
                    && !timelineList.isEmpty()) {

                for (Object item : timelineList) {

                    if (item instanceof Map<?, ?> event) {

                        /*
                         * IMPORTANT:
                         *
                         * InvestigationService currently provides:
                         * observedAt
                         * evidenceType
                         * source
                         * description
                         * strength
                         * reliability
                         * direction
                         *
                         * It does NOT provide timestamp/type.
                         */

                        writer.writeLine(
                                String.valueOf(
                                        event.get(
                                                "observedAt"))
                                        + " | "
                                        + String.valueOf(
                                        event.get(
                                                "evidenceType"))
                        );

                        writer.writeIndented(
                                "Evidence ID",
                                event.get("evidenceId")
                        );

                        writer.writeIndented(
                                "Relationship ID",
                                event.get("relationshipId")
                        );

                        writer.writeIndented(
                                "Source",
                                event.get("source")
                        );

                        writer.writeIndented(
                                "Direction",
                                event.get("direction")
                        );

                        writer.writeIndented(
                                "Strength",
                                event.get("strength")
                        );

                        writer.writeIndented(
                                "Reliability",
                                event.get("reliability")
                        );

                        Object description =
                                event.get(
                                        "description");

                        if (description != null
                                && !String.valueOf(
                                description).isBlank()) {

                            writer.writeIndented(
                                    "Description",
                                    description
                            );
                        }

                        writer.writeLine("");
                    }
                }

            } else {

                writer.writeLine(
                        "No timeline events available."
                );
            }

            // =========================================================
            // 10. GRAPH SUMMARY
            // =========================================================

            writer.writeHeading("10. RELATIONSHIP GRAPH");

            Object graph =
                    report.get("graph");

            if (graph instanceof Map<?, ?> graphData) {

                Object nodes =
                        graphData.get("nodes");

                Object edges =
                        graphData.get("edges");

                int nodeCount =
                        nodes instanceof List<?>
                                ? ((List<?>) nodes).size()
                                : 0;

                int edgeCount =
                        edges instanceof List<?>
                                ? ((List<?>) edges).size()
                                : 0;

                writer.writeField(
                        "Graph Nodes",
                        nodeCount
                );

                writer.writeField(
                        "Graph Edges",
                        edgeCount
                );

            } else {

                writer.writeLine(
                        "No graph data available."
                );
            }

            writer.writeLine("");

            // =========================================================
            // 11. DISCLAIMER
            // =========================================================

            writer.writeHeading("11. DISCLAIMER");

            writer.writeLine(
                    String.valueOf(
                            report.get("disclaimer"))
            );

            writer.writeLine("");

            writer.writeLine(
                    "DAVIS is a prototype investigation-support "
                            + "system. The attribution-confidence "
                            + "score is an explainable assessment "
                            + "derived from the controlled evidence "
                            + "available to the prototype. It is not "
                            + "identity proof and is not a calibrated "
                            + "probability."
            );

            writer.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =================================================================
    // PDF WRITER
    // =================================================================

    private static class PdfWriter {

        private final PDDocument document;

        private PDPage page;
        private PDPageContentStream contentStream;

        private float yPosition;

        private static final float MARGIN = 50;
        private static final float LINE_HEIGHT = 14;
        private static final float FONT_SIZE = 9;
        private static final float INDENT = 18;

        private final PDType1Font regularFont =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

        private final PDType1Font boldFont =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

        PdfWriter(PDDocument document)
                throws IOException {

            this.document = document;

            startNewPage();
        }

        // -------------------------------------------------------------
        // TITLE
        // -------------------------------------------------------------

        void writeTitle(String text)
                throws IOException {

            ensureSpace(35);

            contentStream.setFont(
                    boldFont,
                    18
            );

            contentStream.beginText();

            contentStream.newLineAtOffset(
                    MARGIN,
                    yPosition
            );

            contentStream.showText(
                    sanitize(text)
            );

            contentStream.endText();

            yPosition -= 28;
        }

        // -------------------------------------------------------------
        // SECTION HEADING
        // -------------------------------------------------------------

        void writeHeading(String text)
                throws IOException {

            ensureSpace(30);

            contentStream.setFont(
                    boldFont,
                    12
            );

            contentStream.beginText();

            contentStream.newLineAtOffset(
                    MARGIN,
                    yPosition
            );

            contentStream.showText(
                    sanitize(text)
            );

            contentStream.endText();

            yPosition -= 20;
        }

        // -------------------------------------------------------------
        // NORMAL LINE
        // -------------------------------------------------------------

        void writeLine(String text)
                throws IOException {

            List<String> lines =
                    wrapText(
                            sanitize(text),
                            105
                    );

            for (String line : lines) {

                ensureSpace(LINE_HEIGHT);

                contentStream.setFont(
                        regularFont,
                        FONT_SIZE
                );

                contentStream.beginText();

                contentStream.newLineAtOffset(
                        MARGIN,
                        yPosition
                );

                contentStream.showText(line);

                contentStream.endText();

                yPosition -= LINE_HEIGHT;
            }
        }

        // -------------------------------------------------------------
        // FIELD
        // -------------------------------------------------------------

        void writeField(
                String label,
                Object value)
                throws IOException {

            ensureSpace(LINE_HEIGHT);

            String text =
                    label
                            + ": "
                            + String.valueOf(
                            value == null
                                    ? "-"
                                    : value
                    );

            writeLine(text);
        }

        // -------------------------------------------------------------
        // INDENTED FIELD
        // -------------------------------------------------------------

        void writeIndented(
                String label,
                Object value)
                throws IOException {

            String text =
                    label
                            + ": "
                            + String.valueOf(
                            value == null
                                    ? "-"
                                    : value
                    );

            List<String> lines =
                    wrapText(
                            sanitize(text),
                            95
                    );

            for (String line : lines) {

                ensureSpace(LINE_HEIGHT);

                contentStream.setFont(
                        regularFont,
                        FONT_SIZE
                );

                contentStream.beginText();

                contentStream.newLineAtOffset(
                        MARGIN + INDENT,
                        yPosition
                );

                contentStream.showText(line);

                contentStream.endText();

                yPosition -= LINE_HEIGHT;
            }
        }

        // -------------------------------------------------------------
        // LABEL
        // -------------------------------------------------------------

        void writeLabel(String text)
                throws IOException {

            ensureSpace(LINE_HEIGHT);

            contentStream.setFont(
                    boldFont,
                    FONT_SIZE
            );

            contentStream.beginText();

            contentStream.newLineAtOffset(
                    MARGIN,
                    yPosition
            );

            contentStream.showText(
                    sanitize(text)
            );

            contentStream.endText();

            yPosition -= LINE_HEIGHT;
        }

        // -------------------------------------------------------------
        // SAVE
        // -------------------------------------------------------------

        void save(
                ByteArrayOutputStream outputStream)
                throws IOException {

            contentStream.close();

            document.save(outputStream);
        }

        // -------------------------------------------------------------
        // PAGE MANAGEMENT
        // -------------------------------------------------------------

        private void ensureSpace(
                float requiredSpace)
                throws IOException {

            if (yPosition - requiredSpace < MARGIN) {

                contentStream.close();

                startNewPage();
            }
        }

        private void startNewPage()
                throws IOException {

            page =
                    new PDPage(
                            PDRectangle.A4
                    );

            document.addPage(page);

            contentStream =
                    new PDPageContentStream(
                            document,
                            page
                    );

            yPosition =
                    page.getMediaBox()
                            .getHeight()
                            - MARGIN;
        }

        // -------------------------------------------------------------
        // TEXT WRAPPING
        // -------------------------------------------------------------

        private List<String> wrapText(
                String text,
                int maxCharacters) {

            if (text == null
                    || text.isBlank()) {

                return List.of("");
            }

            ArrayList<String> result =
                    new ArrayList<>();

            String[] words =
                    text.split("\\s+");

            StringBuilder current =
                    new StringBuilder();

            for (String word : words) {

                if (current.length()
                        + word.length()
                        + 1
                        > maxCharacters) {

                    if (!current.isEmpty()) {

                        result.add(
                                current.toString()
                        );

                        current =
                                new StringBuilder();
                    }
                }

                if (!current.isEmpty()) {
                    current.append(" ");
                }

                current.append(word);
            }

            if (!current.isEmpty()) {

                result.add(
                        current.toString()
                );
            }

            return result;
        }

        // -------------------------------------------------------------
        // SANITIZE
        // -------------------------------------------------------------

        private String sanitize(
                String text) {

            if (text == null) {
                return "";
            }

            return text
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .replace("\t", " ");
        }
    }
}