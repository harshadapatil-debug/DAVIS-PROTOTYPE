package com.davis.service;

import com.davis.model.Case;
import com.davis.model.Entity;
import com.davis.model.Evidence;
import com.davis.model.Indicator;
import com.davis.model.Relationship;
import com.davis.repository.CaseRepository;
import com.davis.repository.EntityRepository;
import com.davis.repository.EvidenceRepository;
import com.davis.repository.IndicatorRepository;
import com.davis.repository.RelationshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ControlledIntelligenceService {

    private final CaseRepository caseRepository;
    private final IndicatorRepository indicatorRepository;
    private final EntityRepository entityRepository;
    private final RelationshipRepository relationshipRepository;
    private final EvidenceRepository evidenceRepository;
    private final ObjectMapper objectMapper;
    private final AimlClient aimlClient;

    /*
     * JSON uses timestamps like:
     *
     * 2026-09-18T11:30:00
     *
     * Therefore ISO_LOCAL_DATE_TIME is used.
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    public ControlledIntelligenceService(
            CaseRepository caseRepository,
            IndicatorRepository indicatorRepository,
            EntityRepository entityRepository,
            RelationshipRepository relationshipRepository,
            EvidenceRepository evidenceRepository,
            ObjectMapper objectMapper,
            AimlClient aimlClient
    ) {
        this.caseRepository = caseRepository;
        this.indicatorRepository = indicatorRepository;
        this.entityRepository = entityRepository;
        this.relationshipRepository = relationshipRepository;
        this.evidenceRepository = evidenceRepository;
        this.objectMapper = objectMapper;
        this.aimlClient = aimlClient;
    }


    /*
     * ============================================================
     * MAIN CONTROLLED INTELLIGENCE ANALYSIS
     * ============================================================
     *
     * Flow:
     *
     * Controlled Intelligence JSON
     *          ↓
     * Build AIML input
     *          ↓
     * AIML Service
     *          ↓
     * Entities
     * Relationships
     * Evidence
     *          ↓
     * Persist into MySQL
     */
    @Transactional
    public Map<String, Object> analyzeCase(Long caseId) {

        /*
         * --------------------------------------------------------
         * STEP 1 — Load case
         * --------------------------------------------------------
         */
        Case caseEntity =
                caseRepository.findById(caseId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Case not found: " + caseId
                                )
                        );


        /*
         * --------------------------------------------------------
         * STEP 2 — Find known indicator
         * --------------------------------------------------------
         */
        Indicator indicator =
                indicatorRepository.findAll()
                        .stream()
                        .filter(item ->
                                item.getCaseEntity() != null
                                        && item.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No indicator found for case: "
                                                + caseId
                                )
                        );


        /*
         * --------------------------------------------------------
         * STEP 3 — Locate controlled intelligence JSON
         * --------------------------------------------------------
         */
        Path[] possiblePaths = {

                Path.of(
                        "..",
                        "data",
                        "intelligence",
                        "synthetic_intelligence.json"
                ),

                Path.of(
                        "data",
                        "intelligence",
                        "synthetic_intelligence.json"
                )
        };

        Path intelligencePath = null;

        for (Path path : possiblePaths) {

            if (Files.exists(path)) {
                intelligencePath = path;
                break;
            }
        }

        if (intelligencePath == null) {

            throw new RuntimeException(
                    "Controlled intelligence file not found. "
                            + "Expected: data/intelligence/"
                            + "synthetic_intelligence.json"
            );
        }


        try {

            /*
             * ----------------------------------------------------
             * STEP 4 — Read JSON dataset
             * ----------------------------------------------------
             */
            String json =
                    Files.readString(intelligencePath);

            Map<String, Object> dataset =
                    objectMapper.readValue(
                            json,
                            Map.class
                    );

            Object recordsObject =
                    dataset.get("records");

            if (!(recordsObject instanceof List<?>)) {

                throw new RuntimeException(
                        "Controlled intelligence JSON "
                                + "does not contain a valid records array."
                );
            }

            List<?> records =
                    (List<?>) recordsObject;


            /*
             * ----------------------------------------------------
             * STEP 5 — Build AIML intelligence input
             * ----------------------------------------------------
             */
            List<Map<String, Object>> intelligence =
                    new ArrayList<>();

            int matchedRecords = 0;


            for (Object recordObject : records) {

                /*
                 * Skip anything that is not a JSON object.
                 */
                if (!(recordObject instanceof Map<?, ?>)) {
                    continue;
                }

                Map<?, ?> record =
                        (Map<?, ?>) recordObject;


                /*
                 * ------------------------------------------------
                 * Match record against known indicator
                 * ------------------------------------------------
                 */
                String recordIndicatorType =
                        stringValue(
                                record.get("indicatorType")
                        );

                String recordIndicatorValue =
                        stringValue(
                                record.get("indicatorValue")
                        );


                if (!equalsIgnoreCase(
                        indicator.getIndicatorType(),
                        recordIndicatorType
                )) {
                    continue;
                }


                if (!equalsIgnoreCase(
                        indicator.getIndicatorValue(),
                        recordIndicatorValue
                )) {
                    continue;
                }


                /*
                 * This record belongs to the current investigation.
                 */
                matchedRecords++;


                /*
                 * ------------------------------------------------
                 * Extract entity information
                 * ------------------------------------------------
                 */
                Map<?, ?> sourceEntityData =
                        mapValue(
                                record.get("sourceEntity")
                        );

                Map<?, ?> targetEntityData =
                        mapValue(
                                record.get("targetEntity")
                        );

                Map<?, ?> evidenceData =
                        mapValue(
                                record.get("evidence")
                        );


                String sourceType =
                        stringValue(
                                sourceEntityData.get(
                                        "entityType"
                                )
                        );

                String sourceValue =
                        stringValue(
                                sourceEntityData.get(
                                        "entityValue"
                                )
                        );


                String targetType =
                        stringValue(
                                targetEntityData.get(
                                        "entityType"
                                )
                        );

                String targetValue =
                        stringValue(
                                targetEntityData.get(
                                        "entityValue"
                                )
                        );


                String evidenceDescription =
                        stringValue(
                                evidenceData.get(
                                        "description"
                                )
                        );


                String evidenceSource =
                        stringValue(
                                evidenceData.get(
                                        "source"
                                )
                        );


                String observedAt =
                        stringValue(
                                evidenceData.get(
                                        "observedAt"
                                )
                        );


                String reliability =
                        stringValue(
                                evidenceData.get(
                                        "reliability"
                                )
                        );


                /*
                 * ------------------------------------------------
                 * Build natural-language intelligence
                 * ------------------------------------------------
                 *
                 * IMPORTANT:
                 *
                 * We intentionally DO NOT send:
                 *
                 * relationshipType
                 * assessment
                 *
                 * to AIML.
                 *
                 * Otherwise AIML would simply be given the
                 * answer instead of detecting the relationship.
                 */
                String text =
                        "Source entity: "
                                + sourceType
                                + " "
                                + sourceValue
                                + ". Target entity: "
                                + targetType
                                + " "
                                + targetValue
                                + ". Evidence: "
                                + evidenceDescription;


                /*
                 * ------------------------------------------------
                 * Build AIML intelligence record
                 * ------------------------------------------------
                 */
                Map<String, Object> intelligenceRecord =
                        new LinkedHashMap<>();

                intelligenceRecord.put(
                        "source",
                        evidenceSource
                );

                intelligenceRecord.put(
                        "observedAt",
                        observedAt
                );

                intelligenceRecord.put(
                        "reliability",
                        reliability
                );

                intelligenceRecord.put(
                        "text",
                        text
                );


                intelligence.add(
                        intelligenceRecord
                );
            }


            /*
             * ----------------------------------------------------
             * STEP 6 — Build AIML request
             * ----------------------------------------------------
             */
            Map<String, Object> aimlPayload =
                    new LinkedHashMap<>();

            aimlPayload.put(
                    "caseId",
                    caseId
            );

            aimlPayload.put(
                    "indicatorType",
                    indicator.getIndicatorType()
            );

            aimlPayload.put(
                    "indicatorValue",
                    indicator.getIndicatorValue()
            );

            aimlPayload.put(
                    "intelligence",
                    intelligence
            );


            /*
             * ----------------------------------------------------
             * STEP 7 — Call AIML service
             * ----------------------------------------------------
             */
            Map<String, Object> aimlResult =
                    aimlClient.analyze(
                            aimlPayload
                    );


            /*
             * ----------------------------------------------------
             * STEP 8 — Read AIML entities
             * ----------------------------------------------------
             */
            Object aimlEntitiesObject =
                    aimlResult.get("entities");


            if (!(aimlEntitiesObject instanceof List<?>)) {

                throw new RuntimeException(
                        "AIML response does not contain "
                                + "a valid entities array."
                );
            }


            List<?> aimlEntities =
                    (List<?>) aimlEntitiesObject;


            /*
             * AIML entity ID
             *        ↓
             * Java Entity object
             *
             * AIML IDs are logical IDs.
             * Database IDs are generated separately.
             */
            Map<String, Entity> entityMapping =
                    new HashMap<>();


            int newEntities = 0;


            /*
             * ----------------------------------------------------
             * Persist AIML entities
             * ----------------------------------------------------
             */
            for (Object entityObject : aimlEntities) {

                if (!(entityObject instanceof Map<?, ?>)) {
                    continue;
                }


                Map<?, ?> entityData =
                        (Map<?, ?>) entityObject;


                Object aimlEntityId =
                        entityData.get("entityId");


                if (aimlEntityId == null) {
                    continue;
                }


                EntityResult entityResult =
                        findOrCreateEntity(
                                caseEntity,
                                entityData
                        );


                Entity entity =
                        entityResult.entity();


                if (entityResult.created()) {
                    newEntities++;
                }


                entityMapping.put(
                        String.valueOf(
                                aimlEntityId
                        ),
                        entity
                );
            }


            /*
             * ----------------------------------------------------
             * STEP 9 — Read AIML relationships
             * ----------------------------------------------------
             */
            Object aimlRelationshipsObject =
                    aimlResult.get(
                            "candidateRelationships"
                    );


            if (!(aimlRelationshipsObject instanceof List<?>)) {

                throw new RuntimeException(
                        "AIML response does not contain "
                                + "a valid candidateRelationships array."
                );
            }


            List<?> aimlRelationships =
                    (List<?>) aimlRelationshipsObject;


            int newRelationships = 0;


            /*
             * AIML relationship ID
             *        ↓
             * Java Relationship object
             */
            Map<String, Relationship> relationshipMapping =
                    new HashMap<>();


            /*
             * ----------------------------------------------------
             * Persist AIML relationships
             * ----------------------------------------------------
             */
            for (Object relationshipObject :
                    aimlRelationships) {

                if (!(relationshipObject instanceof Map<?, ?>)) {
                    continue;
                }


                Map<?, ?> relationshipData =
                        (Map<?, ?>) relationshipObject;


                String aimlRelationshipId =
                        stringValue(
                                relationshipData.get(
                                        "relationshipId"
                                )
                        );


                String sourceEntityId =
                        stringValue(
                                relationshipData.get(
                                        "sourceEntityId"
                                )
                        );


                String targetEntityId =
                        stringValue(
                                relationshipData.get(
                                        "targetEntityId"
                                )
                        );


                Entity sourceEntity =
                        entityMapping.get(
                                sourceEntityId
                        );


                Entity targetEntity =
                        entityMapping.get(
                                targetEntityId
                        );


                if (sourceEntity == null
                        || targetEntity == null) {

                    throw new RuntimeException(
                            "AIML relationship references "
                                    + "unknown entity. "
                                    + "source="
                                    + sourceEntityId
                                    + ", target="
                                    + targetEntityId
                    );
                }


                String relationshipType =
                        stringValue(
                                relationshipData.get(
                                        "relationshipType"
                                )
                        );


                String relationshipDescription =
                        stringValue(
                                relationshipData.get(
                                        "description"
                                )
                        );


                String relationshipAssessment =
                        stringValue(
                                relationshipData.get(
                                        "assessment"
                                )
                        );


                RelationshipResult relationshipResult =
                        findOrCreateRelationship(
                                caseEntity,
                                sourceEntity,
                                targetEntity,
                                relationshipType,
                                relationshipDescription,
                                relationshipAssessment
                        );


                Relationship relationship =
                        relationshipResult.relationship();


                if (relationshipResult.created()) {
                    newRelationships++;
                }


                relationshipMapping.put(
                        aimlRelationshipId,
                        relationship
                );
            }


            /*
             * ----------------------------------------------------
             * STEP 10 — Read AIML evidence
             * ----------------------------------------------------
             */
            Object aimlEvidenceObject =
                    aimlResult.get(
                            "evidence"
                    );


            if (!(aimlEvidenceObject instanceof List<?>)) {

                throw new RuntimeException(
                        "AIML response does not contain "
                                + "a valid evidence array."
                );
            }


            List<?> aimlEvidence =
                    (List<?>) aimlEvidenceObject;


            int newEvidence = 0;


            /*
             * ----------------------------------------------------
             * Persist AIML evidence
             * ----------------------------------------------------
             */
            for (Object evidenceObject :
                    aimlEvidence) {

                if (!(evidenceObject instanceof Map<?, ?>)) {
                    continue;
                }


                Map<?, ?> evidenceData =
                        (Map<?, ?>) evidenceObject;


                String aimlRelationshipId =
                        stringValue(
                                evidenceData.get(
                                        "relationshipId"
                                )
                        );


                Relationship relationship =
                        relationshipMapping.get(
                                aimlRelationshipId
                        );


                if (relationship == null) {

                    throw new RuntimeException(
                            "AIML evidence references "
                                    + "unknown relationship: "
                                    + aimlRelationshipId
                    );
                }


                String evidenceType =
                        stringValue(
                                evidenceData.get(
                                        "evidenceType"
                                )
                        );


                String source =
                        stringValue(
                                evidenceData.get(
                                        "source"
                                )
                        );


                String description =
                        stringValue(
                                evidenceData.get(
                                        "description"
                                )
                        );


                String strength =
                        stringValue(
                                evidenceData.get(
                                        "strength"
                                )
                        );


                String reliability =
                        stringValue(
                                evidenceData.get(
                                        "reliability"
                                )
                        );


                String direction =
                        stringValue(
                                evidenceData.get(
                                        "direction"
                                )
                        );


                String independenceGroup =
                        stringValue(
                                evidenceData.get(
                                        "independenceGroup"
                                )
                        );


                String observedAtText =
                        stringValue(
                                evidenceData.get(
                                        "observedAt"
                                )
                        );


                LocalDateTime observedAt =
                        parseObservedAt(
                                observedAtText
                        );


                /*
                 * ------------------------------------------------
                 * Check duplicate evidence
                 * ------------------------------------------------
                 */
                boolean evidenceExists =
                        evidenceRepository.findAll()
                                .stream()
                                .anyMatch(item ->

                                        item.getRelationship() != null

                                                && item.getRelationship()
                                                .getRelationshipId()
                                                .equals(
                                                        relationship
                                                                .getRelationshipId()
                                                )

                                                && equalsIgnoreCase(
                                                        item.getEvidenceType(),
                                                        evidenceType
                                                )

                                                && equalsIgnoreCase(
                                                        item.getSource(),
                                                        source
                                                )

                                                && sameObservedTime(
                                                        item.getObservedAt(),
                                                        observedAt
                                                )
                                );


                /*
                 * ------------------------------------------------
                 * Create evidence if it does not exist
                 * ------------------------------------------------
                 */
                if (!evidenceExists) {

                    Evidence evidence =
                            new Evidence();


                    evidence.setRelationship(
                            relationship
                    );


                    evidence.setEvidenceType(
                            evidenceType
                    );


                    evidence.setSource(
                            source
                    );


                    evidence.setDescription(
                            description
                    );


                    evidence.setStrength(
                            strength
                    );


                    evidence.setReliability(
                            reliability
                    );


                    evidence.setDirection(
                            direction
                    );


                    evidence.setIndependenceGroup(
                            independenceGroup
                    );


                    evidence.setObservedAt(
                            observedAt
                    );


                    evidenceRepository.save(
                            evidence
                    );


                    newEvidence++;
                }
            }


            /*
             * ----------------------------------------------------
             * STEP 11 — Build analysis response
             * ----------------------------------------------------
             */
            Map<String, Object> result =
                    new LinkedHashMap<>();


            result.put(
                    "caseId",
                    caseId
            );


            result.put(
                    "indicatorType",
                    indicator.getIndicatorType()
            );


            result.put(
                    "indicatorValue",
                    indicator.getIndicatorValue()
            );


            result.put(
                    "matchedRecords",
                    matchedRecords
            );


            result.put(
                    "newEntities",
                    newEntities
            );


            result.put(
                    "newRelationships",
                    newRelationships
            );


            result.put(
                    "newEvidence",
                    newEvidence
            );


            result.put(
                    "message",
                    "Controlled intelligence analysis completed through AIML."
            );


            return result;

        } catch (Exception exception) {

            throw new RuntimeException(
                    "Controlled intelligence analysis failed: "
                            + exception.getMessage(),
                    exception
            );
        }
    }


    /*
     * ============================================================
     * FIND OR CREATE ENTITY
     * ============================================================
     */
    private EntityResult findOrCreateEntity(
            Case caseEntity,
            Map<?, ?> entityData
    ) {

        String entityType =
                stringValue(
                        entityData.get("entityType")
                );


        String entityValue =
                stringValue(
                        entityData.get("entityValue")
                );


        String description =
                stringValue(
                        entityData.get("description")
                );


        /*
         * Read discoveryConfidence.
         */
        BigDecimal discoveryConfidence =
                decimalValue(
                        entityData.get(
                                "discoveryConfidence"
                        )
                );


        /*
         * --------------------------------------------------------
         * Find existing entity within this case
         * --------------------------------------------------------
         */
        Entity existing =
                entityRepository.findAll()
                        .stream()
                        .filter(item ->

                                item.getCaseEntity() != null

                                        && item.getCaseEntity()
                                        .getCaseId()
                                        .equals(
                                                caseEntity.getCaseId()
                                        )

                                        && equalsIgnoreCase(
                                                item.getEntityType(),
                                                entityType
                                        )

                                        && equalsIgnoreCase(
                                                item.getEntityValue(),
                                                entityValue
                                        )
                        )
                        .findFirst()
                        .orElse(null);


        /*
         * --------------------------------------------------------
         * Existing entity
         * --------------------------------------------------------
         */
        if (existing != null) {

            BigDecimal existingConfidence =
                    existing.getDiscoveryConfidence();


            boolean missingConfidence =
                    existingConfidence == null
                            || existingConfidence.compareTo(
                            BigDecimal.ZERO
                    ) == 0;


            /*
             * Repair missing discovery confidence.
             */
            if (missingConfidence
                    && discoveryConfidence != null) {

                existing.setDiscoveryConfidence(
                        discoveryConfidence
                );


                entityRepository.save(
                        existing
                );
            }


            return new EntityResult(
                    existing,
                    false
            );
        }


        /*
         * --------------------------------------------------------
         * Create new entity
         * --------------------------------------------------------
         */
        Entity newEntity =
                new Entity();


        newEntity.setCaseEntity(
                caseEntity
        );


        newEntity.setEntityType(
                entityType
        );


        newEntity.setEntityValue(
                entityValue
        );


        newEntity.setDescription(
                description
        );


        if (discoveryConfidence != null) {

            newEntity.setDiscoveryConfidence(
                    discoveryConfidence
            );
        }


        Entity savedEntity =
                entityRepository.save(
                        newEntity
                );


        return new EntityResult(
                savedEntity,
                true
        );
    }


    /*
     * ============================================================
     * FIND OR CREATE RELATIONSHIP
     * ============================================================
     */
    private RelationshipResult findOrCreateRelationship(
            Case caseEntity,
            Entity sourceEntity,
            Entity targetEntity,
            String relationshipType,
            String relationshipDescription,
            String relationshipAssessment
    ) {

        /*
         * --------------------------------------------------------
         * Find existing relationship
         * --------------------------------------------------------
         */
        Relationship existingRelationship =
                relationshipRepository.findAll()
                        .stream()
                        .filter(item ->

                                item.getCaseEntity() != null

                                        && item.getCaseEntity()
                                        .getCaseId()
                                        .equals(
                                                caseEntity.getCaseId()
                                        )

                                        && item.getSourceEntity() != null

                                        && item.getSourceEntity()
                                        .getEntityId()
                                        .equals(
                                                sourceEntity
                                                        .getEntityId()
                                        )

                                        && item.getTargetEntity() != null

                                        && item.getTargetEntity()
                                        .getEntityId()
                                        .equals(
                                                targetEntity
                                                        .getEntityId()
                                        )

                                        && equalsIgnoreCase(
                                                item.getRelationshipType(),
                                                relationshipType
                                        )
                        )
                        .findFirst()
                        .orElse(null);


        /*
         * Existing relationship.
         */
        if (existingRelationship != null) {

            return new RelationshipResult(
                    existingRelationship,
                    false
            );
        }


        /*
         * --------------------------------------------------------
         * Create new relationship
         * --------------------------------------------------------
         */
        Relationship newRelationship =
                new Relationship();


        newRelationship.setCaseEntity(
                caseEntity
        );


        newRelationship.setSourceEntity(
                sourceEntity
        );


        newRelationship.setTargetEntity(
                targetEntity
        );


        newRelationship.setRelationshipType(
                relationshipType
        );


        newRelationship.setDescription(
                relationshipDescription
        );


        newRelationship.setAssessment(
                relationshipAssessment
        );


        Relationship savedRelationship =
                relationshipRepository.save(
                        newRelationship
                );


        return new RelationshipResult(
                savedRelationship,
                true
        );
    }


    /*
     * ============================================================
     * ENTITY RESULT
     * ============================================================
     */
    private record EntityResult(
            Entity entity,
            boolean created
    ) {
    }


    /*
     * ============================================================
     * RELATIONSHIP RESULT
     * ============================================================
     */
    private record RelationshipResult(
            Relationship relationship,
            boolean created
    ) {
    }


    /*
     * ============================================================
     * PARSE OBSERVED TIMESTAMP
     * ============================================================
     */
    private LocalDateTime parseObservedAt(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }


        return LocalDateTime.parse(
                value,
                DATE_TIME_FORMATTER
        );
    }


    /*
     * ============================================================
     * PARSE DECIMAL VALUE
     * ============================================================
     */
    private BigDecimal decimalValue(
            Object value
    ) {

        if (value == null) {
            return null;
        }


        try {

            return new BigDecimal(
                    String.valueOf(value)
            );

        } catch (NumberFormatException exception) {

            throw new RuntimeException(
                    "Invalid discoveryConfidence value: "
                            + value,
                    exception
            );
        }
    }


    /*
     * ============================================================
     * STRING VALUE HELPER
     * ============================================================
     */
    private String stringValue(
            Object value
    ) {

        if (value == null) {
            return null;
        }


        return String.valueOf(value);
    }


    /*
     * ============================================================
     * MAP VALUE HELPER
     * ============================================================
     */
    private Map<?, ?> mapValue(
            Object value
    ) {

        if (value instanceof Map<?, ?> map) {
            return map;
        }


        return new HashMap<>();
    }


    /*
     * ============================================================
     * CASE-INSENSITIVE STRING COMPARISON
     * ============================================================
     */
    private boolean equalsIgnoreCase(
            String first,
            String second
    ) {

        if (first == null && second == null) {
            return true;
        }


        if (first == null || second == null) {
            return false;
        }


        return first.equalsIgnoreCase(
                second
        );
    }


    /*
     * ============================================================
     * DATETIME COMPARISON
     * ============================================================
     */
    private boolean sameObservedTime(
            LocalDateTime first,
            LocalDateTime second
    ) {

        if (first == null && second == null) {
            return true;
        }


        if (first == null || second == null) {
            return false;
        }


        return first.equals(second);
    }
}