package com.davis.service;

import com.davis.model.Case;
import com.davis.model.ConfidenceScore;
import com.davis.model.Entity;
import com.davis.model.Evidence;
import com.davis.model.Indicator;
import com.davis.model.Relationship;
import com.davis.model.Review;
import com.davis.repository.CaseRepository;
import com.davis.repository.ConfidenceScoreRepository;
import com.davis.repository.EntityRepository;
import com.davis.repository.EvidenceRepository;
import com.davis.repository.IndicatorRepository;
import com.davis.repository.RelationshipRepository;
import com.davis.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvestigationService {

    private final CaseRepository caseRepository;
    private final IndicatorRepository indicatorRepository;
    private final EntityRepository entityRepository;
    private final RelationshipRepository relationshipRepository;
    private final EvidenceRepository evidenceRepository;
    private final ConfidenceScoreRepository confidenceScoreRepository;
    private final ReviewRepository reviewRepository;
    private final ConfidenceEngineService confidenceEngineService;
    private final ControlledIntelligenceService controlledIntelligenceService;

    public InvestigationService(
            CaseRepository caseRepository,
            IndicatorRepository indicatorRepository,
            EntityRepository entityRepository,
            RelationshipRepository relationshipRepository,
            EvidenceRepository evidenceRepository,
            ConfidenceScoreRepository confidenceScoreRepository,
            ReviewRepository reviewRepository,
            ConfidenceEngineService confidenceEngineService,
            ControlledIntelligenceService controlledIntelligenceService
    ) {

        this.caseRepository = caseRepository;
        this.indicatorRepository = indicatorRepository;
        this.entityRepository = entityRepository;
        this.relationshipRepository = relationshipRepository;
        this.evidenceRepository = evidenceRepository;
        this.confidenceScoreRepository = confidenceScoreRepository;
        this.reviewRepository = reviewRepository;
        this.confidenceEngineService = confidenceEngineService;
        this.controlledIntelligenceService = controlledIntelligenceService;
    }

    /*
     * ============================================================
     * RUN COMPLETE INVESTIGATION
     * ============================================================
     *
     * Order:
     *
     * 1. Load case
     * 2. Mark case as processed
     * 3. Run controlled intelligence analysis
     * 4. Calculate attribution-confidence
     * 5. Build final investigation response
     *
     * This is the main end-to-end backend flow.
     */
    @Transactional
    public Map<String, Object> runInvestigation(Long caseId) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException("Case not found: " + caseId));

        /*
         * --------------------------------------------------------
         * STEP 1 — Update case status
         * --------------------------------------------------------
         */
        caseEntity.setStatus("PROCESSED");
        caseEntity.setLastScanAt(LocalDateTime.now());

        caseRepository.save(caseEntity);

        /*
         * --------------------------------------------------------
         * STEP 2 — Controlled intelligence expansion
         * --------------------------------------------------------
         *
         * This reads the synthetic intelligence dataset,
         * matches the known indicator and creates:
         *
         * - entities
         * - relationships
         * - evidence
         *
         * inside this case.
         */
        Map<String, Object> controlledIntelligenceResult =
                controlledIntelligenceService.analyzeCase(caseId);

        /*
         * --------------------------------------------------------
         * STEP 3 — Calculate confidence
         * --------------------------------------------------------
         *
         * IMPORTANT:
         * This happens AFTER intelligence expansion so that
         * newly created evidence is included in the score.
         */
        ConfidenceScore confidenceScore =
                confidenceEngineService.evaluateConfidence(caseId);

        /*
         * --------------------------------------------------------
         * STEP 4 — Build complete investigation response
         * --------------------------------------------------------
         */
        Map<String, Object> investigation =
                buildInvestigation(caseEntity);

        /*
         * Add controlled-intelligence result explicitly
         * so the frontend can see that expansion actually ran.
         */
        investigation.put(
                "controlledIntelligence",
                controlledIntelligenceResult
        );

        /*
         * Make sure the newest confidence result is returned.
         */
        investigation.put("confidence", confidenceScore);

        return investigation;
    }


    /*
     * ============================================================
     * GET EXISTING INVESTIGATION DATA
     * ============================================================
     *
     * Used by:
     *
     * GET /api/cases/{caseId}/investigation
     *
     * This does NOT run intelligence again.
     * It only reads the current case state.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInvestigation(Long caseId) {

        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new RuntimeException("Case not found: " + caseId));

        return buildInvestigation(caseEntity);
    }


    /*
     * ============================================================
     * BUILD INVESTIGATION RESPONSE
     * ============================================================
     */
    private Map<String, Object> buildInvestigation(
            Case caseEntity
    ) {

        Long caseId = caseEntity.getCaseId();

        /*
         * --------------------------------------------------------
         * CASE
         * --------------------------------------------------------
         */
        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("case", caseEntity);


        /*
         * --------------------------------------------------------
         * INDICATORS
         * --------------------------------------------------------
         */
        List<Indicator> indicators =
                indicatorRepository.findAll()
                        .stream()
                        .filter(indicator ->
                                indicator.getCaseEntity() != null
                                        && indicator.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .sorted(
                                Comparator.comparing(
                                        Indicator::getIndicatorId
                                )
                        )
                        .toList();

        result.put("indicators", indicators);


        /*
         * Known indicator
         *
         * For this prototype the first registered indicator
         * is treated as the primary known indicator.
         */
        Indicator knownIndicator =
                indicators.isEmpty()
                        ? null
                        : indicators.get(0);

        result.put(
                "knownIndicator",
                knownIndicator
        );


        /*
         * --------------------------------------------------------
         * ENTITIES
         * --------------------------------------------------------
         */
        List<Entity> entities =
                entityRepository.findAll()
                        .stream()
                        .filter(entity ->
                                entity.getCaseEntity() != null
                                        && entity.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .sorted(
                                Comparator.comparing(
                                        Entity::getEntityId
                                )
                        )
                        .toList();

        result.put("entities", entities);


        /*
         * --------------------------------------------------------
         * RELATIONSHIPS
         * --------------------------------------------------------
         */
        List<Relationship> relationships =
                relationshipRepository.findAll()
                        .stream()
                        .filter(relationship ->
                                relationship.getCaseEntity() != null
                                        && relationship.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .sorted(
                                Comparator.comparing(
                                        Relationship::getRelationshipId
                                )
                        )
                        .toList();

        result.put(
                "relationships",
                relationships
        );


        /*
         * --------------------------------------------------------
         * EVIDENCE
         * --------------------------------------------------------
         */
        List<Evidence> evidence =
                evidenceRepository.findAll()
                        .stream()
                        .filter(item ->
                                item.getRelationship() != null
                                        && item.getRelationship()
                                        .getCaseEntity() != null
                                        && item.getRelationship()
                                        .getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .sorted(
                                Comparator.comparing(
                                        Evidence::getEvidenceId
                                )
                        )
                        .toList();

        result.put(
                "evidence",
                evidence
        );


        /*
         * --------------------------------------------------------
         * CONFIDENCE
         * --------------------------------------------------------
         */
        ConfidenceScore confidence =
                confidenceScoreRepository.findAll()
                        .stream()
                        .filter(item ->
                                item.getCaseEntity() != null
                                        && item.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .findFirst()
                        .orElse(null);

        result.put(
                "confidence",
                confidence
        );


        /*
         * --------------------------------------------------------
         * REVIEW
         * --------------------------------------------------------
         */
        Review review =
                reviewRepository.findAll()
                        .stream()
                        .filter(item ->
                                item.getCaseEntity() != null
                                        && item.getCaseEntity()
                                        .getCaseId()
                                        .equals(caseId))
                        .findFirst()
                        .orElse(null);

        result.put(
                "review",
                review
        );


        /*
         * --------------------------------------------------------
         * GRAPH
         * --------------------------------------------------------
         */
        result.put(
                "graph",
                buildGraph(
                        entities,
                        relationships
                )
        );


        /*
         * --------------------------------------------------------
         * TIMELINE
         * --------------------------------------------------------
         */
        result.put(
                "timeline",
                buildTimeline(evidence)
        );


        /*
         * --------------------------------------------------------
         * SUMMARY
         * --------------------------------------------------------
         */
        Map<String, Object> summary =
                new LinkedHashMap<>();

        summary.put(
                "entityCount",
                entities.size()
        );

        summary.put(
                "relationshipCount",
                relationships.size()
        );

        summary.put(
                "evidenceCount",
                evidence.size()
        );

        summary.put(
                "indicatorCount",
                indicators.size()
        );

        result.put(
                "summary",
                summary
        );


        /*
         * --------------------------------------------------------
         * DISCLAIMER
         * --------------------------------------------------------
         */
        result.put(
                "disclaimer",
                "DAVIS is a controlled intelligence "
                        + "prototype. The attribution-confidence "
                        + "score is an explainable assessment "
                        + "for investigator review and is not "
                        + "a calibrated probability or identity proof."
        );

        return result;
    }


    /*
     * ============================================================
     * BUILD GRAPH
     * ============================================================
     *
     * Creates graph-ready nodes and edges from database records.
     */
    private Map<String, Object> buildGraph(
            List<Entity> entities,
            List<Relationship> relationships
    ) {

        Map<String, Object> graph =
                new LinkedHashMap<>();


        /*
         * --------------------------------------------------------
         * NODES
         * --------------------------------------------------------
         */
        List<Map<String, Object>> nodes =
                new ArrayList<>();

        for (Entity entity : entities) {

            Map<String, Object> node =
                    new LinkedHashMap<>();

            node.put(
                    "id",
                    String.valueOf(
                            entity.getEntityId()
                    )
            );

            node.put(
                    "entityId",
                    entity.getEntityId()
            );

            node.put(
                    "label",
                    entity.getEntityValue()
            );

            node.put(
                    "entityType",
                    entity.getEntityType()
            );

            node.put(
                    "description",
                    entity.getDescription()
            );

            node.put(
                    "discoveryConfidence",
                    entity.getDiscoveryConfidence()
            );

            nodes.add(node);
        }


        /*
         * --------------------------------------------------------
         * EDGES
         * --------------------------------------------------------
         */
        List<Map<String, Object>> edges =
                new ArrayList<>();

        for (Relationship relationship : relationships) {

            Map<String, Object> edge =
                    new LinkedHashMap<>();

            edge.put(
                    "id",
                    String.valueOf(
                            relationship.getRelationshipId()
                    )
            );

            edge.put(
                    "relationshipId",
                    relationship.getRelationshipId()
            );

            if (relationship.getSourceEntity() != null) {

                edge.put(
                        "source",
                        String.valueOf(
                                relationship
                                        .getSourceEntity()
                                        .getEntityId()
                        )
                );

                edge.put(
                        "sourceEntityId",
                        relationship
                                .getSourceEntity()
                                .getEntityId()
                );
            }

            if (relationship.getTargetEntity() != null) {

                edge.put(
                        "target",
                        String.valueOf(
                                relationship
                                        .getTargetEntity()
                                        .getEntityId()
                        )
                );

                edge.put(
                        "targetEntityId",
                        relationship
                                .getTargetEntity()
                                .getEntityId()
                );
            }

            edge.put(
                    "relationshipType",
                    relationship.getRelationshipType()
            );

            edge.put(
                    "description",
                    relationship.getDescription()
            );

            edge.put(
                    "assessment",
                    relationship.getAssessment()
            );

            edges.add(edge);
        }


        graph.put(
                "nodes",
                nodes
        );

        graph.put(
                "edges",
                edges
        );

        return graph;
    }


    /*
     * ============================================================
     * BUILD TIMELINE
     * ============================================================
     *
     * Timeline is derived directly from evidence timestamps.
     */
    private List<Map<String, Object>> buildTimeline(
            List<Evidence> evidence
    ) {

        List<Map<String, Object>> timeline =
                new ArrayList<>();

        for (Evidence item : evidence) {

            Map<String, Object> event =
                    new LinkedHashMap<>();

            event.put(
                    "evidenceId",
                    item.getEvidenceId()
            );

            event.put(
                    "relationshipId",
                    item.getRelationship() != null
                            ? item.getRelationship()
                            .getRelationshipId()
                            : null
            );

            event.put(
                    "observedAt",
                    item.getObservedAt()
            );

            event.put(
                    "evidenceType",
                    item.getEvidenceType()
            );

            event.put(
                    "source",
                    item.getSource()
            );

            event.put(
                    "description",
                    item.getDescription()
            );

            event.put(
                    "strength",
                    item.getStrength()
            );

            event.put(
                    "reliability",
                    item.getReliability()
            );

            event.put(
                    "direction",
                    item.getDirection()
            );

            timeline.add(event);
        }

        /*
         * Chronological ordering.
         */
        timeline.sort(
                Comparator.comparing(
                        item -> {

                            Object value =
                                    item.get("observedAt");

                            if (value instanceof LocalDateTime) {
                                return (LocalDateTime) value;
                            }

                            return LocalDateTime.MIN;
                        }
                )
        );

        return timeline;
    }
}