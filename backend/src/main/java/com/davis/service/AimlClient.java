package com.davis.service;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class AimlClient {

    private static final String AIML_URL =
            "http://127.0.0.1:8090/aiml/analyze";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AimlClient(ObjectMapper objectMapper) {

        this.objectMapper = objectMapper;

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();
    }

    /**
     * Sends an investigation payload to the Python AIML service.
     *
     * Expected input:
     *
     * {
     *   "caseId": 1,
     *   "indicatorId": 1,
     *   "indicatorType": "USERNAME",
     *   "indicatorValue": "r4v3n_mh",
     *   "intelligence": [
     *      {
     *          "source": "...",
     *          "observedAt": "...",
     *          "reliability": "HIGH",
     *          "text": "..."
     *      }
     *   ]
     * }
     *
     * Expected output:
     *
     * {
     *   "caseId": 1,
     *   "entities": [...],
     *   "candidateRelationships": [...],
     *   "evidence": [...]
     * }
     */
    public Map<String, Object> analyze(
            Map<String, Object> payload
    ) {

        try {

            String requestBody =
                    objectMapper.writeValueAsString(payload);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(AIML_URL))
                            .timeout(Duration.ofSeconds(30))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            requestBody
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                throw new RuntimeException(
                        "AIML service returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            Map<?, ?> rawResponse =
                    objectMapper.readValue(
                            response.body(),
                            Map.class
                    );

            return convertToStringObjectMap(rawResponse);

        } catch (IOException exception) {

            throw new RuntimeException(
                    "Could not communicate with AIML service: "
                            + exception.getMessage(),
                    exception
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "AIML service request was interrupted.",
                    exception
            );
        }
    }

    /**
     * Converts the generic Jackson map into the Map<String,Object>
     * expected by the rest of the backend.
     */
    private Map<String, Object> convertToStringObjectMap(
            Map<?, ?> source
    ) {

        java.util.LinkedHashMap<String, Object> result =
                new java.util.LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {

            result.put(
                    String.valueOf(entry.getKey()),
                    entry.getValue()
            );
        }

        return result;
    }
}