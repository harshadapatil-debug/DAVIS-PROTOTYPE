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
            ObjectMapper objectMapper
    ) {
        this.caseRepository = caseRepository;
        this.indicatorRepository = indicatorRepository;
        this.entityRepository = entityRepository;
        this.relationshipRepository = relationshipRepository;
        this.evidenceRepository = evidenceRepository;
        this.objectMapper = objectMapper;
    }


    /*
     * ============================================================
     * MAIN CONTROLLED INTELLIGENCE ANALYSIS
     * ============================================================
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
             * Counters
             * ----------------------------------------------------
             */
            int matchedRecords = 0;
            int newEntities = 0;
            int newRelationships = 0;
            int newEvidence = 0;


            /*
             * ----------------------------------------------------
             * STEP 5 — Process controlled records
             * ----------------------------------------------------
             */
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
                 * This controlled record belongs to the
                 * current investigation.
                 */
                matchedRecords++;


                /*
                 * ------------------------------------------------
                 * STEP 6 — SOURCE ENTITY
                 * ------------------------------------------------
                 */
                Map<?, ?> sourceEntityData =
                        mapValue(
                                record.get("sourceEntity")
                        );

                EntityResult sourceResult =
                        findOrCreateEntity(
                                caseEntity,
                                sourceEntityData
                        );

                Entity sourceEntity =
                        sourceResult.entity();

                if (sourceResult.created()) {
                    newEntities++;
                }


                /*
                 * ------------------------------------------------
                 * STEP 7 — TARGET ENTITY
                 * ------------------------------------------------
                 */
                Map<?, ?> targetEntityData =
                        mapValue(
                                record.get("targetEntity")
                        );

                EntityResult targetResult =
                        findOrCreateEntity(
                                caseEntity,
                                targetEntityData
                        );

                Entity targetEntity =
                        targetResult.entity();

                if (targetResult.created()) {
                    newEntities++;
                }


                /*
                 * ------------------------------------------------
                 * STEP 8 — RELATIONSHIP
                 * ------------------------------------------------
                 */
                Map<?, ?> relationshipData =
                        mapValue(
                                record.get("relationship")
                        );

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


                /*
                 * ------------------------------------------------
                 * STEP 9 — EVIDENCE DATA
                 * ------------------------------------------------
                 */
                Map<?, ?> evidenceData =
                        mapValue(
                                record.get("evidence")
                        );

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


                /*
                 * Parse timestamp.
                 */
                final LocalDateTime observedAt =
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
             * STEP 10 — Build analysis response
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
                    "Controlled intelligence analysis completed."
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
         * NEW:
         * Read discoveryConfidence from the controlled dataset.
         *
         * Example:
         * "discoveryConfidence": 0.92
         */
        BigDecimal discoveryConfidence =
                decimalValue(
                        entityData.get("discoveryConfidence")
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
         *
         * If an older run created the entity with the default
         * value 0.0000, repair it using the confidence from JSON.
         *
         * We do NOT overwrite an already meaningful confidence.
         */
        if (existing != null) {

            BigDecimal existingConfidence =
                    existing.getDiscoveryConfidence();

            boolean missingConfidence =
                    existingConfidence == null
                            || existingConfidence.compareTo(
                                    BigDecimal.ZERO
                            ) == 0;

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

        /*
         * NEW:
         * Persist discovery confidence from JSON.
         */
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
     *
     * Handles values coming from Jackson as:
     *
     * 0.92
     * 0.8500
     * "0.92"
     *
     * and converts them safely to BigDecimal.
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

        return first.equalsIgnoreCase(second);
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