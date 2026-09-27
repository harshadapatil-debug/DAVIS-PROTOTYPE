package com.davis.report.export;

import com.davis.report.model.ReportData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JSON EXPORTER
 *
 * Exports the complete ReportData structure as JSON.
 *
 * The object is generated from backend investigation data.
 * Nothing case-specific is hard-coded.
 */
@Component
public class JsonReportExporter {

    private final ObjectMapper objectMapper;


    public JsonReportExporter() {

        objectMapper =
                new ObjectMapper();

        objectMapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );
    }


    public byte[] generate(
            ReportData data)
            throws IOException {

        return objectMapper
                .writeValueAsBytes(data);
    }
}