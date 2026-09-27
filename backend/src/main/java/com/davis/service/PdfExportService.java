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

            writer.writeTitle(
                    String.valueOf(
                            report.get("reportTitle")
                    )
            );

            writer.writeLine("");

            writer.writeHeading("CASE INFORMATION");

            Object caseObject = report.get("case");

            if (caseObject instanceof Map<?, ?> caseData) {

                writer.writeLine(
                        "Case ID: " +
                                String.valueOf(
                                        caseData.get("caseId"))
                );

                writer.writeLine(
                        "Case Name: " +
                                String.valueOf(
                                        caseData.get("caseName"))
                );

                writer.writeLine(
                        "Case Type: " +
                                String.valueOf(
                                        caseData.get("caseType"))
                );

                writer.writeLine(
                        "Category: " +
                                String.valueOf(
                                        caseData.get("category"))
                );

                writer.writeLine(
                        "Status: " +
                                String.valueOf(
                                        caseData.get("status"))
                );
            }

            writer.writeLine("");

            writer.writeHeading("KNOWN INDICATOR");

            Object indicator =
                    report.get("knownIndicator");

            if (indicator instanceof Map<?, ?> indicatorData) {

                writer.writeLine(
                        "Type: " +
                                String.valueOf(
                                        indicatorData.get(
                                                "indicatorType"))
                );

                writer.writeLine(
                        "Value: " +
                                String.valueOf(
                                        indicatorData.get(
                                                "indicatorValue"))
                );
            }

            writer.writeLine("");

            writer.writeHeading("SUMMARY");

            Object summary =
                    report.get("summary");

            if (summary instanceof Map<?, ?> summaryData) {

                writer.writeLine(
                        "Entities: " +
                                String.valueOf(
                                        summaryData.get(
                                                "entityCount"))
                );

                writer.writeLine(
                        "Relationships: " +
                                String.valueOf(
                                        summaryData.get(
                                                "relationshipCount"))
                );

                writer.writeLine(
                        "Evidence records: " +
                                String.valueOf(
                                        summaryData.get(
                                                "evidenceCount"))
                );

                writer.writeLine(
                        "Timeline events: " +
                                String.valueOf(
                                        summaryData.get(
                                                "timelineEventCount"))
                );
            }

            writer.writeLine("");

            writer.writeHeading("ENTITIES");

            Object entities =
                    report.get("entities");

            if (entities instanceof List<?> entityList) {

                for (Object item : entityList) {

                    if (item instanceof Map<?, ?> entity) {

                        writer.writeLine(
                                entity.get("entityId")
                                        + " | "
                                        + entity.get("entityType")
                                        + " | "
                                        + entity.get("entityValue")
                        );
                    }
                }
            }

            writer.writeLine("");

            writer.writeHeading("RELATIONSHIPS");

            Object relationships =
                    report.get("relationships");

            if (relationships instanceof List<?> relationshipList) {

                for (Object item : relationshipList) {

                    if (item instanceof Map<?, ?> relationship) {

                        writer.writeLine(
                                relationship.get(
                                        "relationshipId")
                                        + " | "
                                        + relationship.get(
                                                "relationshipType")
                                        + " | "
                                        + relationship.get(
                                                "assessment")
                        );
                    }
                }
            }

            writer.writeLine("");

            writer.writeHeading("EVIDENCE");

            Object evidence =
                    report.get("evidence");

            if (evidence instanceof List<?> evidenceList) {

                for (Object item : evidenceList) {

                    if (item instanceof Map<?, ?> evidenceData) {

                        writer.writeLine(
                                "Evidence "
                                        + evidenceData.get(
                                                "evidenceId")
                                        + " | "
                                        + evidenceData.get(
                                                "evidenceType")
                                        + " | "
                                        + evidenceData.get(
                                                "direction")
                                        + " | "
                                        + evidenceData.get(
                                                "strength")
                        );

                        writer.writeLine(
                                "Source: "
                                        + evidenceData.get(
                                                "source")
                        );
                    }
                }
            }

            writer.writeLine("");

            writer.writeHeading("ATTRIBUTION CONFIDENCE");

            Object confidence =
                    report.get("confidence");

            if (confidence instanceof Map<?, ?> confidenceData) {

                writer.writeLine(
                        "Score: "
                                + confidenceData.get("score")
                                + " / 100"
                );

                writer.writeLine(
                        "Risk Level: "
                                + confidenceData.get(
                                        "riskLevel")
                );

                writer.writeLine(
                        "Explanation: "
                                + confidenceData.get(
                                        "explanation")
                );
            }

            writer.writeLine("");

            writer.writeHeading("ANALYST REVIEW");

            Object review =
                    report.get("review");

            if (review instanceof Map<?, ?> reviewData) {

                writer.writeLine(
                        "Status: "
                                + reviewData.get("status")
                );

                writer.writeLine(
                        "Note: "
                                + reviewData.get("note")
                );
            }

            writer.writeLine("");

            writer.writeHeading("TIMELINE");

            Object timeline =
                    report.get("timeline");

            if (timeline instanceof List<?> timelineList) {

                for (Object item : timelineList) {

                    if (item instanceof Map<?, ?> event) {

                        writer.writeLine(
                                String.valueOf(
                                        event.get("timestamp"))
                                        + " | "
                                        + String.valueOf(
                                        event.get("type"))
                        );
                    }
                }
            }

            writer.writeLine("");

            writer.writeHeading("DISCLAIMER");

            writer.writeLine(
                    String.valueOf(
                            report.get("disclaimer"))
            );

            writer.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    private static class PdfWriter {

        private final PDDocument document;

        private PDPage page;
        private PDPageContentStream contentStream;

        private float yPosition;

        private static final float MARGIN = 50;
        private static final float LINE_HEIGHT = 16;
        private static final float FONT_SIZE = 9;

        private final PDType1Font regularFont =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );

        private final PDType1Font boldFont =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

        PdfWriter(PDDocument document) throws IOException {

            this.document = document;

            startNewPage();
        }

        void writeTitle(String text) throws IOException {

            ensureSpace(30);

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

            yPosition -= 30;
        }

        void writeHeading(String text) throws IOException {

            ensureSpace(25);

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

        void writeLine(String text) throws IOException {

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

        void save(
                ByteArrayOutputStream outputStream)
                throws IOException {

            contentStream.close();

            document.save(outputStream);
        }

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

            page = new PDPage(
                    PDRectangle.A4
            );

            document.addPage(page);

            contentStream =
                    new PDPageContentStream(
                            document,
                            page
                    );

            yPosition =
                    page.getMediaBox().getHeight()
                            - MARGIN;
        }

        private List<String> wrapText(
                String text,
                int maxCharacters) {

            if (text == null || text.isBlank()) {
                return List.of("");
            }

            java.util.ArrayList<String> result =
                    new java.util.ArrayList<>();

            String[] words =
                    text.split("\\s+");

            StringBuilder current =
                    new StringBuilder();

            for (String word : words) {

                if (current.length()
                        + word.length()
                        + 1
                        > maxCharacters) {

                    result.add(
                            current.toString()
                    );

                    current =
                            new StringBuilder();
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

        private String sanitize(String text) {

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