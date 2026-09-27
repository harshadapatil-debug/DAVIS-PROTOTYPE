package com.davis.report.export;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import com.davis.report.model.ReportData;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * FINAL INVESTIGATION REPORT PDF
 *
 * Generates a structured, low-colour, professional PDF.
 *
 * Design goals:
 * - Easy to read
 * - Tables wherever structured data is appropriate
 * - Minimal colour
 * - Clear section hierarchy
 * - DAVIS watermark
 * - Synthetic-data disclaimer
 *
 * IMPORTANT:
 * All investigation values are obtained from ReportData.
 * No case-specific data is hard-coded here.
 */
@Component
public class PdfReportExporter {


    // ------------------------------------------------------------
    // COLOURS
    // ------------------------------------------------------------

    private static final Color DARK = new Color(35, 35, 35);
    private static final Color GREY = new Color(100, 100, 100);
    private static final Color LIGHT_GREY = new Color(235, 235, 235);
    private static final Color BORDER = new Color(190, 190, 190);


    // ------------------------------------------------------------
    // FONTS
    // ------------------------------------------------------------

    private Font titleFont =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    18,
                    DARK
            );

    private Font headingFont =
            FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    12,
                    DARK
            );

    private Font normalFont =
            FontFactory.getFont(
                    FontFactory.HELVETICA,
                    9,
                    DARK
            );

    private Font smallFont =
            FontFactory.getFont(
                    FontFactory.HELVETICA,
                    8,
                    GREY
            );


    public byte[] generate(ReportData data) throws IOException {

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4,
                        42,
                        42,
                        55,
                        45
                );

        PdfWriter writer =
                PdfWriter.getInstance(
                        document,
                        output
                );

        /*
         * Add watermark/header/footer to every page.
         */
        writer.setPageEvent(new ReportPageEvent());

        document.open();


        // ========================================================
        // TITLE
        // ========================================================

        Paragraph title =
                new Paragraph(
                        "DAVIS",
                        titleFont
                );

        title.setAlignment(Element.ALIGN_CENTER);

        document.add(title);


        Paragraph subtitle =
                new Paragraph(
                        "Digital Attribution & Verification Intelligence System",
                        smallFont
                );

        subtitle.setAlignment(Element.ALIGN_CENTER);

        document.add(subtitle);

        document.add(Chunk.NEWLINE);


        Paragraph reportTitle =
                new Paragraph(
                        "FINAL INVESTIGATION REPORT",
                        headingFont
                );

        reportTitle.setAlignment(Element.ALIGN_CENTER);

        document.add(reportTitle);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // DISCLAIMER
        // ========================================================

        addDisclaimer(document);


        // ========================================================
        // CASE INFORMATION
        // ========================================================

        addSectionHeading(document, "1. CASE INFORMATION");

        PdfPTable caseTable =
                createTable(2);

        addKeyValue(
                caseTable,
                "Case ID",
                value(data.getCaseId())
        );

        addKeyValue(
                caseTable,
                "Case Name",
                value(data.getCaseName())
        );

        addKeyValue(
                caseTable,
                "Case Type",
                value(data.getCaseType())
        );

        addKeyValue(
                caseTable,
                "Description",
                value(data.getCaseDescription())
        );

        addKeyValue(
                caseTable,
                "Report Generated",
                value(data.getGeneratedAt())
        );

        document.add(caseTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // KNOWN INDICATOR
        // ========================================================

        addSectionHeading(document, "2. KNOWN INDICATOR");

        PdfPTable indicatorTable =
                createTable(2);

        addKeyValue(
                indicatorTable,
                "Indicator Type",
                value(data.getIndicatorType())
        );

        addKeyValue(
                indicatorTable,
                "Indicator Value",
                value(data.getIndicatorValue())
        );

        document.add(indicatorTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // INVESTIGATION SUMMARY
        // ========================================================

        addSectionHeading(document, "3. INVESTIGATION SUMMARY");

        document.add(
                new Paragraph(
                        value(data.getInvestigationSummary()),
                        normalFont
                )
        );

        document.add(Chunk.NEWLINE);


        // ========================================================
        // ENTITIES
        // ========================================================

        addSectionHeading(document, "4. DISCOVERED ENTITIES");

        PdfPTable entityTable =
                createTable(3);

        addHeader(entityTable, "ID");
        addHeader(entityTable, "TYPE");
        addHeader(entityTable, "VALUE");

        if (data.getEntities() != null) {

            for (ReportData.EntityData entity :
                    data.getEntities()) {

                addCell(entityTable, entity.getId());
                addCell(entityTable, entity.getType());
                addCell(entityTable, entity.getValue());
            }
        }

        document.add(entityTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // RELATIONSHIPS
        // ========================================================

        addSectionHeading(document, "5. IMPORTANT RELATIONSHIPS");

        PdfPTable relationshipTable =
                createTable(4);

        addHeader(relationshipTable, "ID");
        addHeader(relationshipTable, "SOURCE");
        addHeader(relationshipTable, "RELATIONSHIP");
        addHeader(relationshipTable, "TARGET");

        if (data.getRelationships() != null) {

            for (ReportData.RelationshipData relationship :
                    data.getRelationships()) {

                addCell(
                        relationshipTable,
                        relationship.getId()
                );

                addCell(
                        relationshipTable,
                        relationship.getSourceEntity()
                );

                addCell(
                        relationshipTable,
                        relationship.getRelationshipType()
                );

                addCell(
                        relationshipTable,
                        relationship.getTargetEntity()
                );
            }
        }

        document.add(relationshipTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // EVIDENCE
        // ========================================================

        addSectionHeading(document, "6. SUPPORTING EVIDENCE");

        PdfPTable evidenceTable =
                createTable(7);

        addHeader(evidenceTable, "ID");
        addHeader(evidenceTable, "TYPE");
        addHeader(evidenceTable, "SOURCE");
        addHeader(evidenceTable, "STRENGTH");
        addHeader(evidenceTable, "RELIABILITY");
        addHeader(evidenceTable, "DIRECTION");
        addHeader(evidenceTable, "OBSERVED AT");

        if (data.getEvidence() != null) {

            for (ReportData.EvidenceData evidence :
                    data.getEvidence()) {

                addCell(evidenceTable, evidence.getId());
                addCell(evidenceTable, evidence.getType());
                addCell(evidenceTable, evidence.getSource());
                addCell(evidenceTable, evidence.getStrength());
                addCell(evidenceTable, evidence.getReliability());
                addCell(evidenceTable, evidence.getDirection());
                addCell(evidenceTable, evidence.getObservedAt());
            }
        }

        document.add(evidenceTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // EVIDENCE DESCRIPTION
        // ========================================================

        addSectionHeading(document, "7. EVIDENCE DETAILS");

        if (data.getEvidence() != null) {

            for (ReportData.EvidenceData evidence :
                    data.getEvidence()) {

                Paragraph evidenceParagraph =
                        new Paragraph();

                evidenceParagraph.add(
                        new Chunk(
                                value(evidence.getId())
                                        + " — ",
                                headingFont
                        )
                );

                evidenceParagraph.add(
                        new Chunk(
                                value(evidence.getDescription()),
                                normalFont
                        )
                );

                document.add(evidenceParagraph);

                document.add(
                        new Paragraph(
                                "Source: "
                                        + value(evidence.getSource()),
                                smallFont
                        )
                );

                document.add(Chunk.NEWLINE);
            }
        }


        // ========================================================
        // CONFIDENCE
        // ========================================================

        addSectionHeading(document, "8. ATTRIBUTION-CONFIDENCE ASSESSMENT");

        PdfPTable confidenceTable =
                createTable(2);

        addKeyValue(
                confidenceTable,
                "Confidence Score",
                data.getConfidenceScore() == null
                        ? "Not available"
                        : data.getConfidenceScore() + " / 100"
        );

        addKeyValue(
                confidenceTable,
                "Risk Level",
                value(data.getRiskLevel())
        );

        addKeyValue(
                confidenceTable,
                "Explanation",
                value(data.getConfidenceExplanation())
        );

        document.add(confidenceTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // REVIEW
        // ========================================================

        addSectionHeading(document, "9. ANALYST REVIEW");

        PdfPTable reviewTable =
                createTable(2);

        addKeyValue(
                reviewTable,
                "Review Status",
                value(data.getReviewStatus())
        );

        addKeyValue(
                reviewTable,
                "Reviewer",
                value(data.getReviewerName())
        );

        addKeyValue(
                reviewTable,
                "Remarks",
                value(data.getReviewRemarks())
        );

        document.add(reviewTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // STRESS TEST
        // ========================================================

        addSectionHeading(document, "10. STRESS-TEST RESULT");

        PdfPTable stressTable =
                createTable(2);

        addKeyValue(
                stressTable,
                "Status",
                value(data.getStressTestStatus())
        );

        addKeyValue(
                stressTable,
                "Result",
                value(data.getStressTestResult())
        );

        document.add(stressTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // TIMELINE
        // ========================================================

        addSectionHeading(document, "11. INVESTIGATION TIMELINE");

        PdfPTable timelineTable =
                createTable(3);

        addHeader(timelineTable, "TIMESTAMP");
        addHeader(timelineTable, "EVENT");
        addHeader(timelineTable, "SOURCE");

        if (data.getTimeline() != null) {

            for (ReportData.TimelineData item :
                    data.getTimeline()) {

                addCell(timelineTable, item.getTimestamp());
                addCell(timelineTable, item.getEvent());
                addCell(timelineTable, item.getSource());
            }
        }

        document.add(timelineTable);

        document.add(Chunk.NEWLINE);


        // ========================================================
        // LIMITATIONS
        // ========================================================

        addSectionHeading(document, "12. PROTOTYPE LIMITATIONS");

        if (data.getLimitations() != null) {

            for (String limitation :
                    data.getLimitations()) {

                document.add(
                        new Paragraph(
                                "• " + value(limitation),
                                normalFont
                        )
                );
            }
        }

        document.add(Chunk.NEWLINE);


        // ========================================================
        // FINAL DISCLAIMER
        // ========================================================

        PdfPTable finalBox =
                new PdfPTable(1);

        finalBox.setWidthPercentage(100);

        PdfPCell disclaimerCell =
                new PdfPCell(
                        new Paragraph(
                                "SYNTHETIC / CONTROLLED DATASET — "
                                        + "PROTOTYPE DEMONSTRATION\n\n"
                                        + "The result is an "
                                        + "attribution-confidence assessment, "
                                        + "not identity proof.",
                                normalFont
                        )
                );

        disclaimerCell.setPadding(10);
        disclaimerCell.setBorderColor(BORDER);

        finalBox.addCell(disclaimerCell);

        document.add(finalBox);


        document.close();

        return output.toByteArray();
    }


    // ============================================================
    // HELPER METHODS
    // ============================================================

    private void addSectionHeading(
            Document document,
            String heading) throws DocumentException {

        Paragraph paragraph =
                new Paragraph(
                        heading,
                        headingFont
                );

        paragraph.setSpacingBefore(8);
        paragraph.setSpacingAfter(6);

        document.add(paragraph);
    }


    private PdfPTable createTable(int columns) {

        PdfPTable table =
                new PdfPTable(columns);

        table.setWidthPercentage(100);
        table.setSpacingAfter(5);

        return table;
    }


    private void addHeader(
            PdfPTable table,
            String text) {

        PdfPCell cell =
                new PdfPCell(
                        new Paragraph(
                                value(text),
                                FontFactory.getFont(
                                        FontFactory.HELVETICA_BOLD,
                                        8,
                                        DARK
                                )
                        )
                );

        cell.setBackgroundColor(LIGHT_GREY);
        cell.setBorderColor(BORDER);
        cell.setPadding(5);

        table.addCell(cell);
    }


    private void addCell(
            PdfPTable table,
            String text) {

        PdfPCell cell =
                new PdfPCell(
                        new Paragraph(
                                value(text),
                                smallFont
                        )
                );

        cell.setBorderColor(BORDER);
        cell.setPadding(5);

        table.addCell(cell);
    }


    private void addKeyValue(
            PdfPTable table,
            String key,
            String value) {

        PdfPCell keyCell =
                new PdfPCell(
                        new Paragraph(
                                key,
                                FontFactory.getFont(
                                        FontFactory.HELVETICA_BOLD,
                                        8,
                                        DARK
                                )
                        )
                );

        keyCell.setBackgroundColor(LIGHT_GREY);
        keyCell.setBorderColor(BORDER);
        keyCell.setPadding(6);

        PdfPCell valueCell =
                new PdfPCell(
                        new Paragraph(
                                value(value),
                                smallFont
                        )
                );

        valueCell.setBorderColor(BORDER);
        valueCell.setPadding(6);

        table.addCell(keyCell);
        table.addCell(valueCell);
    }


    private void addDisclaimer(
            Document document)
            throws DocumentException {

        PdfPTable table =
                new PdfPTable(1);

        table.setWidthPercentage(100);

        PdfPCell cell =
                new PdfPCell(
                        new Paragraph(
                                "SYNTHETIC / CONTROLLED DATASET — "
                                        + "PROTOTYPE DEMONSTRATION",
                                FontFactory.getFont(
                                        FontFactory.HELVETICA_BOLD,
                                        8,
                                        DARK
                                )
                        )
                );

        cell.setPadding(7);
        cell.setBorderColor(BORDER);
        cell.setBackgroundColor(LIGHT_GREY);

        table.addCell(cell);

        document.add(table);

        document.add(Chunk.NEWLINE);
    }


    private String value(Object value) {

        if (value == null) {
            return "Not available";
        }

        String text = String.valueOf(value);

        if (text.trim().isEmpty()) {
            return "Not available";
        }

        return text;
    }


    // ============================================================
    // PAGE WATERMARK + FOOTER
    // ============================================================

    private static class ReportPageEvent
            extends PdfPageEventHelper {

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document) {

            PdfContentByte canvas =
                    writer.getDirectContentUnder();

            /*
             * DAVIS watermark.
             *
             * This is a visual prototype watermark only.
             * It does not represent a government seal.
             */

            Font watermarkFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            42,
                            new Color(225, 225, 225)
                    );

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "DAVIS",
                            watermarkFont
                    ),
                    PageSize.A4.getWidth() / 2,
                    PageSize.A4.getHeight() / 2,
                    35
            );


            // Footer

            ColumnText.showTextAligned(
                    writer.getDirectContent(),
                    Element.ALIGN_CENTER,
                    new Phrase(
                            "DAVIS • Confidential Investigation Report",
                            FontFactory.getFont(
                                    FontFactory.HELVETICA,
                                    7,
                                    GREY
                            )
                    ),
                    PageSize.A4.getWidth() / 2,
                    22,
                    0
            );
        }
    }
}